<template>
  <div class="button-group">
    <el-button ref="solo" :loading="login_flag" class="button1" type="primary" @click="inviteSolo">
      单人pk(裁判)
    </el-button>
    <el-button ref="group" :loading="login_flag" class="button2" type="primary">组队pk(裁判)</el-button>
  </div>
  <el-dialog v-model="invite_solo_visibility" title="Shipping address">
    <el-form ref="form" :model="solo_form" :rules="rules">
      <el-form-item label="第一位选手" prop="first">
        <el-autocomplete
            v-model="solo_form.first"
            :fetch-suggestions="querySearch"
            class="inline-input w-50"
            clearable
            placeholder="请输入用户名称"
            value-key="nickName"
            @select="handleSelectOne"
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
            @select="handleSelectTwo"
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
    return {
      login_flag: false,
      wsStore: undefined,
      invite_solo_visibility: false,
      solo_form: {
        first: "",
        second: ""
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
      users: []
    }
  },
  created() {
    this.zg = useZgEngineStore()
    this.zg.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
    this.zg.engine.setDebugVerbose(false)
    this.zg.checkBrowser()
    this.wsStore = useWebSocket()
    this.wsStore.ws.onopen = (_) => {
      this.wsStore.sendObject({"handler": "get_player"})
      this.wsStore.sendObject({"handler": "get_token"})
    }
    this.wsStore.ws.onmessage = (e) => {
      let mes = JSON.parse(e.data)
      if (mes.code !== 0) {
        switch (mes.code) {
          case 601:
            this.$notify.error("用户不在线")
        }
      }
      if (mes.msg === "join") {
        this.zg._config.roomId = mes.data.roomId
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
        for (let user of mes.data) {
          onlineUsers.push(user.user)
        }
      }
    }
  },
  methods: {
    solo() {
      this.$refs.form.validate((res) => {
        if (!res)
          return
        let data = {
          "handler": "invite_solo",
          "users": JSON.parse(JSON.stringify(this.users))
        }
        this.wsStore.sendObject(data)
        this.invite_solo_visibility = false
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
    handleSelectOne(item) {
      this.users[0] = item.userId
    },
    handleSelectTwo(item) {
      this.users[1] = item.userId
    },
    inviteSolo() {
      this.wsStore.sendObject({"handler": "get_player"})
      this.invite_solo_visibility = true
    }
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
