<template>
  <div class="button-group">
    <el-button ref="solo" :loading="login_flag" class="button1" type="primary" @click="invite_solo_visibility = true">
      单人pk(裁判)
    </el-button>
    <el-button ref="group" :loading="login_flag" class="button2" type="primary">组队pk(裁判)</el-button>
  </div>
  <el-dialog v-model="invite_solo_visibility" title="Shipping address">
    <el-form :model="solo_form">
      <el-form-item label="first">
        <el-input v-model="solo_form.first" autocomplete="off" type="number"/>
      </el-form-item>
      <el-form-item label="second">
        <el-input v-model="solo_form.second" autocomplete="off" type="number"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="invite_solo_visibility = false">Cancel</el-button>
        <el-button type="primary" @click="solo">
          Confirm
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import {useWebSocket} from "@/store/modules/webSocket";
import {getUserProfile} from "@/api/system/user";

export default {
  name: "index",
  data() {
    return {
      login_flag: false,
      wsStore: undefined,
      invite_solo_visibility: false,
      solo_form: {
        first: 0,
        second: 0
      },
      zg: undefined
    }
  },
  created() {
    this.zg = useZgEngineStore()
    this.zg.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
    this.zg.engine.setDebugVerbose(false)
    this.zg.checkBrowser()
    getUserProfile().then(res => {
      this.zg.init(res.data)
      let tokenData = {
        "seq": 1,
        "timestamp": Math.ceil(new Date().getTime() / 1000),
        "app_id": this.zg._config.appid,
        "user_id": "ruoyi" + this.zg._config.userId,
        "user_name": this.zg._config.nickName,
        "device_id": "device" + this.zg._config.userId,
        "queue_role": 1 || 10, // 队列 1 座席， 10 客户
        "room_role": 0,
        "net_type": 2
      }
      axios.post("/token/logintoken", tokenData).then(res => {
        this.zg.login(res.data.login_token).then((res) => {
          if (res === true) {
            this.$notify.success({
              title: "登录成功",
              message: "您可以等待别人邀请或者作为裁判邀请他人",
              showClose: false
            })
            this.login_flag = false
          }
        })
      })

    })
    this.wsStore = useWebSocket()
    this.wsStore.ws.onmessage = (e) => {
      let mes = JSON.parse(e.data)
      if (mes.code !== 0) {
        switch (mes.code) {
          case 601:
            this.$notify.error("用户不在线")
        }
      }
      if (mes.msg === "join") {
        zg._config.roomId = mes.data.roomId
        router.push({path: "/race/online"})
      }
    }
  },
  methods: {
    solo() {
      let data = {
        "handler": "invite_solo",
        "users": [this.solo_form.first, this.solo_form.second]
      }
      this.wsStore.sendObject(data)
      this.invite_solo_visibility = false
      this.zg._config.roleId = 1
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
