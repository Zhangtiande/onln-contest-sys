<template>
  <div class="button-group">
    <el-button ref="solo" :loading="login_flag" class="button1" type="primary" @click="inviteSolo">
      单人pk(裁判)
    </el-button>
    <el-button ref="group" :loading="login_flag" class="button2" type="primary" @click="invite_group">组队pk(裁判)
    </el-button>
  </div>
  <el-dialog v-model="invite_solo_visibility" title="单人pk邀请">
    <el-form ref="form" :model="solo_form" :rules="rules">
      <el-form-item label="第一位选手" prop="first">
        <el-autocomplete
            v-model="solo_form.first"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="selectItem => handleSelect(0, selectItem)"
        />
      </el-form-item>
      <el-form-item label="第二位选手" prop="second">
        <el-autocomplete
            v-model="solo_form.second"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="selectItem => handleSelect(1, selectItem)"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="invite_solo_visibility = false">取消</el-button>
        <el-button type="primary" @click="solo">
          邀请
        </el-button>
      </span>
    </template>
  </el-dialog>

  <el-dialog v-model="invite_group_visibility" title="组队pk邀请">
    <el-form ref="form2" :model="group_form" :rules="groupRules">
      <el-form-item label="A队1号" prop="A1">
        <el-autocomplete
            v-model="group_form.A1"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="selectItem => handleSelect(0, selectItem)"
        />
      </el-form-item>
      <el-form-item label="A队2号" prop="A2">
        <el-autocomplete
            v-model="group_form.A2"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="selectItem => handleSelect(1, selectItem)"
        />
      </el-form-item>
      <el-form-item label="B队1号" prop="B1">
        <el-autocomplete
            v-model="group_form.B1"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="selectItem => handleSelect(2, selectItem)"
        />
      </el-form-item>
      <el-form-item label="B队2号" prop="B2">
        <el-autocomplete
            v-model="group_form.B2"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="selectItem => handleSelect(3, selectItem)"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="invite_group_visibility = false">取消</el-button>
        <el-button type="primary" @click="group">
          邀请
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import {useWebSocket} from "@/store/modules/webSocket";
import router from "@/router";
import {useZgEngineStore} from "@/store/modules/ZgEngine";

let onlineUsers = []

export default {
  name: "index",
  data() {
    const checkExist = (rule, value, callback) => {
      if (this.solo_form.first === this.solo_form.second) {
        return callback(new Error('玩家不能相同'))
      }
      if (!onlineUsers.some(item => item.nickName === value)) {
        return callback(new Error("没有该玩家或该玩家不在线"))
      }
      return callback()
    };
    const groupCheckExist = (rule, value, callback) => {
      if (this.users.some((val, index, arr) => arr.indexOf(val) !== index)) {
        return new Error("玩家不能相同")
      }
      if (!onlineUsers.some(item => item.nickName === value)) {
        return callback(new Error("没有该玩家或该玩家不在线"))
      }
      return callback()
    };
    return {
      login_flag: false,
      wsStore: undefined,
      invite_solo_visibility: false,
      invite_group_visibility: false,
      solo_form: {
        first: "",
        second: ""
      },
      group_form: {
        A1: "",
        A2: "",
        B1: "",
        B2: ""
      },
      zg: undefined,
      rules: {
        first: [
          {required: true, message: '请输入第一个玩家', trigger: 'blur'},
          {validator: checkExist, trigger: 'blur'}
        ],
        second: [
          {required: true, message: '请输入第二个玩家', trigger: 'blur'},
          {validator: checkExist, trigger: 'blur'}
        ]
      },
      groupRules: {
        A1: [
          {required: true, message: '请输入A队1号', trigger: 'blur'},
          {validator: groupCheckExist, trigger: 'blur'}
        ],
        A2: [
          {required: true, message: '请输入A队2号', trigger: 'blur'},
          {validator: groupCheckExist, trigger: 'blur'}
        ],
        B1: [
          {required: true, message: '请输入B队1号', trigger: 'blur'},
          {validator: groupCheckExist, trigger: 'blur'}
        ],
        B2: [
          {required: true, message: '请输入B队2号', trigger: 'blur'},
          {validator: groupCheckExist, trigger: 'blur'}
        ]
      },
      users: [],
      sendObject: undefined
    }
  },
  created() {
    this.zg = useZgEngineStore()
    this.zg.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
    this.zg.engine.setDebugVerbose(false)
    this.zg.checkBrowser()
    let wsStore = useWebSocket()
    this.sendObject = wsStore.sendObject
    wsStore.ws.onopen = (_) => {
      wsStore.sendObject({"handler": "get_player"})
      wsStore.sendObject({"handler": "get_token"})
    }
    wsStore.ws.onmessage = (e) => {
      let mes = JSON.parse(e.data)
      if (mes.code !== 0) {
        switch (mes.code) {
          case 601:
            this.$notify.error("用户不在线")
        }
      }
      if (mes.msg === "join") {
        this.zg._config.roomId = mes.data.roomId
        if (mes.data.a !== null) {
          this.zg._config.a = mes.data.a
          this.zg._config.b = mes.data.b
          this.zg._config.group = true
        }
        router.push({path: "/race/online"})
      } else if (mes.msg === "info") {
        this.zg.init(mes.data).then((res) => {
          if (res === true) {
            this.$notify.success({
              title: "登录成功",
              message: "您可以等待别人邀请或者作为裁判邀请他人",
              showClose: false
            })
            this.login_flag = false
          }
        })
      } else {
        onlineUsers = []
        if (mes.data[0].nickName !== "") {
          mes.data.forEach(item => {
            onlineUsers.push(item)
          })
        } else {
          for (let user of mes.data) {
            onlineUsers.push(user.user)
          }
        }
      }
    }
  },
  methods: {
    solo() {
      this.$refs.form.validate((res) => {
        if (!res)
          return
        this.sendObject({
          "handler": "invite_solo",
          "users": this.users
        })
        this.invite_solo_visibility = false
        this.zg._config.roleId = 1
      })
    },
    group() {
      this.$refs.form2.validate((res) => {
        if (!res)
          return
        this.sendObject({
          "handler": "invite_group",
          "users": this.users
        })
        this.invite_group_visibility = false
        this.zg._config.roleId = 1
      })
    },
    querySearch(queryString, cb) {
      const results = queryString
          ? onlineUsers.filter(this.createFilter(queryString))
          : onlineUsers
      cb(results)
    },
    createFilter(queryString) {
      return (restaurant) => {
        return (
            restaurant.nickName.indexOf(queryString) === 0
        )
      }
    },
    handleSelect(idx, item) {
      this.users[idx] = item.userId
    },
    inviteSolo() {
      this.sendObject({"handler": "get_player"})
      this.invite_solo_visibility = true
    },
    invite_group() {
      this.sendObject({"handler": "get_test_players"})
      this.invite_group_visibility = true
    },
  }
}
</script>

<style scoped>
.button-group {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-image: url("@/assets/images/race.png");
  background-repeat: no-repeat;
  background-position: center;
}

.button1 {
  margin-top: 5%;
}

.button2 {
  margin-top: 12px;
  margin-left: 0;
}


</style>
