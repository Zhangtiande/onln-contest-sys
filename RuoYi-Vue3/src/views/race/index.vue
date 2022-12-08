<template>
  <div class="button-group">
    <el-button ref="solo" :loading="login_flag" class="button1" type="primary"
               @click="invite_solo_visibility = !invite_solo_visibility">
      单人pk(裁判)
    </el-button>
    <el-button ref="group" :loading="login_flag" class="button2" type="primary"
               @click="invite_group_visibility = !invite_group_visibility">
      组队pk(裁判)
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
        <el-button type="primary" @click="invite_solo">
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
        <el-button type="primary" @click="invite_group">
          邀请
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {useWebSocket} from "@/store/modules/webSocket";
import {useZgEngineStore} from "@/store/modules/ZgEngine";
import {getCurrentInstance, onMounted, ref, watch} from "vue";
import {reactive} from "@vue/reactivity";
import router from "@/router";

const {proxy} = getCurrentInstance();
const wsStore = useWebSocket()
const zegoStore = useZgEngineStore()

const login_flag = ref(false)
let invite_solo_visibility = ref(false)
let invite_group_visibility = ref(false)
const solo_form = reactive({first: "", second: ""})
const group_form = reactive({A1: "", A2: "", B1: "", B2: ""})
const users = reactive([])
const onlineUsers = reactive([])

const checkExist = (rule, value, callback) => {
  if (solo_form.first === solo_form.second) {
    return callback(new Error('玩家不能相同'))
  }
  if (!onlineUsers.some(item => item.nickName === value)) {
    return callback(new Error("没有该玩家或该玩家不在线"))
  }
  return callback()
}
const groupCheckExist = (rule, value, callback) => {
  if (users.some((val, index, arr) => arr.indexOf(val) !== index)) {
    return new Error("玩家不能相同")
  }
  if (!onlineUsers.some(item => item.nickName === value)) {
    return callback(new Error("没有该玩家或该玩家不在线"))
  }
  return callback()
}
const rules = {
  first: [
    {validator: checkExist, trigger: 'blur'}
  ],
  second: [
    {validator: checkExist, trigger: 'blur'}
  ]
}
const groupRules = {
  A1: [
    {validator: groupCheckExist, trigger: 'blur'}
  ],
  A2: [
    {validator: groupCheckExist, trigger: 'blur'}
  ],
  B1: [
    {validator: groupCheckExist, trigger: 'blur'}
  ],
  B2: [
    {validator: groupCheckExist, trigger: 'blur'}
  ]
}

onMounted(() => {
  zegoStore.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
  zegoStore.engine.setDebugVerbose(false)
  zegoStore.checkBrowser()
  wsStore.ws.onopen = (_) => {
    wsStore.sendObject({"handler": "get_player"})
    wsStore.sendObject({"handler": "get_token"})
  }
  wsStore.ws.onmessage = (e) => {
    let mes = JSON.parse(e.data)
    if (mes.code !== 0) {
      switch (mes.code) {
        case 601:
          proxy.$notify.error("用户不在线")
      }
    }
    if (mes.msg === "join_solo" || mes.msg === "join_group") {
      zegoStore.config.roomId = mes.data.roomId
      wsStore.user = onlineUsers.filter(item => mes.data.users.includes(item.userId))
      if (mes.msg === "join_solo") {
        router.push({path: "/race/online_solo"})
      } else {
        router.push({path: "/race/online_group"})
      }
    } else if (mes.msg === "info") {
      zegoStore.initEngine(mes.data).then((res) => {
        if (res === true) {
          proxy.$notify.success({
            title: "登录成功",
            message: "您可以等待别人邀请或者作为裁判邀请他人",
            showClose: false
          })
          login_flag.value = false
        }
      })
    } else {
      onlineUsers.length = 0
      if (mes.data[0].nickName !== "") {
        onlineUsers.push(...mes.data)
      } else {
        for (let user of mes.data) {
          onlineUsers.push(user.user)
        }
      }
    }
  }
})

watch(invite_group_visibility, (val) => {
  if (val) {
    users.length = 0
    group_form.A1 = ""
    group_form.A2 = ""
    group_form.B1 = ""
    group_form.B2 = ""
    get_player("get_test_players")
  }
})

watch(invite_solo_visibility, (val) => {
  if (val) {
    users.length = 0
    solo_form.first = ""
    solo_form.second = ""
    get_player("get_player")
  }
})

function invite_solo() {
  proxy.$refs.form.validate((res) => {
    if (!res)
      return
    wsStore.sendObject({
      "handler": "invite_solo",
      "users": users
    })
    invite_solo_visibility.value = false
    zegoStore.config.roleId = 1
  })
}

function invite_group() {
  proxy.$refs.form2.validate((res) => {
    if (!res)
      return
    wsStore.sendObject({
      "handler": "invite_group",
      "users": users
    })
    invite_group_visibility.value = false
    zegoStore.config.roleId = 1
  })
}

function querySearch(queryString, cb) {
  const results = queryString
      ? onlineUsers.filter(createFilter(queryString))
      : onlineUsers
  cb(results)
}

function createFilter(queryString) {
  return (restaurant) => {
    return (
        restaurant.nickName.indexOf(queryString) === 0
    )
  }
}

function handleSelect(idx, item) {
  users[idx] = item.userId
}

function get_player(param) {
  wsStore.sendObject({"handler": param})
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
