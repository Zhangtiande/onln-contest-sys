<template>
  <div>
    <el-row style="margin-top:10px; margin-left: 5%;">
      <el-col :span="4">
        <el-select v-model="audioChoose" placeholder="麦克风" @change="audioChange">
          <el-option
              v-for="item in audioDevice"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select v-model="videoChoose" placeholder="视频设备" @change="videoChange">
          <el-option
              v-for="item in videoDevice"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select v-model="microphoneChoose" placeholder="扬声器">
          <el-option
              v-for="item in outputDevice"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
        </el-select>
      </el-col>
      <el-col v-if="this.config.roleId === 1" :span="8"
              style="display: flex;flex-direction: row;justify-content: space-between;">
        <el-select v-model="chooseUser" placeholder="指定选手回答" value-key="nickName" @change="mutePlayer">
          <el-option
              v-for="item in users"
              :key="item.userId"
              :label="item.nickName"
              :value="item.userId"></el-option>
        </el-select>
        <el-button :disabled="startState" type="success"
                   @click="startCompetition">开始
        </el-button>
        <el-badge :value="20-this.questionIdx" class="item" type="primary">
          <el-button :disabled="endState" type="primary" @click="nextQuestion">下一题
          </el-button>
        </el-badge>
        <el-button type="danger" @click="endGame">结束比赛</el-button>

      </el-col>

    </el-row>
  </div>

  <div id="videoBox">
    <div id="judge">
      <span>{{ this.userInfo.judge }}</span>
      <video ref="judge" autoplay controls muted playsinline></video>
    </div>
    <div class="player">
      <div class="playerA">
        <div class="videoContent" style="">
          <span>{{ this.userInfo.player1 }}</span>
          <video ref="player1" autoplay controls muted playsinline></video>
        </div>
        <div class="videoContent">
          <span>{{ this.userInfo.player2 }}</span>
          <video ref="player2" autoplay controls muted playsinline></video>
        </div>
      </div>
      <div class="playerB">
        <div class="videoContent">
          <span>{{ this.userInfo.player3 }}</span>
          <video ref="player3" autoplay controls muted playsinline></video>
        </div>
        <div class="videoContent">
          <span>{{ this.userInfo.player4 }}</span>
          <video ref="player4" autoplay controls muted playsinline></video>
        </div>
      </div>
    </div>
  </div>

</template>

<!--suppress JSUnusedLocalSymbols, JSUnresolvedFunction -->
<script>
import {useZgEngineStore} from "@/store/modules/ZgEngine";
import {useWebSocket} from "@/store/modules/webSocket";
import {getRoom, updateRoom} from "@/api/race/room";
import {reactive} from '@vue/reactivity'


// noinspection JSUnusedLocalSymbols
export default {
  name: "index",
  setup() {
    const users = reactive(useWebSocket().user)
    const chooseUser = ref("")
    return {
      users, chooseUser
    }
  },
  data() {
    return {
      engine: null,
      config: undefined,
      stream: null,
      audioDevice: [],
      videoDevice: [],
      outputDevice: [],
      playStreamList: [],
      startState: false,
      endState: false,
      audioChoose: "",
      videoChoose: "",
      microphoneChoose: "",
      sendText: "",
      userInfo: {
        judge: "",
        player1: "",
        player2: "",
        player3: "",
        player4: ""
      },
      questionIdx: 0,
      playIdx: 1,
      sendObject: undefined,
    }
  },
  methods: {
    enterRoom() {
      this.engine.enterRoom(this.config.roomId.toString(), this.config.roleId).then((res) => {
        if (res.errorCode === 0) {
          this.$message.success("进入房间成功！")
          this.engine.createStream().then((res) => {
            this.stream = res
            if (this.config.roleId === 1) {
              this.$refs.judge.srcObject = res
              this.engine.startPublishingStream(this.config.streamID, res, {extraInfo: "judge"})
              this.userInfo.judge = this.config.nickName
            } else {
              let idx = this.users.findIndex(item => item.userId === this.config.userId)
              this.$refs["player" + (idx + 1)].srcObject = res
              this.engine.startPublishingStream(this.config.streamID, res, {extraInfo: "player" + (idx + 1)})
            }
          }).catch(err => {
            console.log(err)
            this.$message.error(err.msg)
          })
        } else {
          this.$message.error(res.extendedData)
        }
      })
    },
    leaveRoom() {
      this.playStreamList.forEach((item) => {
        this.engine.stopPlayingStream(item)
      })
      this.engine.stopPublishingStream(this.config.streamID)
      this.engine.destroyStream(this.stream)
    },
    muteMicrophone(state) {
      let res = this.engine.mutePublishStreamAudio(this.stream, state)
      if (res) {
        this.$message.success(state !== true ? "解禁麦克风" : "关闭麦克风")
      }
    },
    audioChange(val) {
      this.engine.useAudioDevice(this.stream, val).then((res) => {
        let option = {title: "切换麦克风", showClose: false, message: res.extendedData}
        res.errorCode === 0 ? this.$notify.success(option) : this.$notify.error(option)
      })
    },
    videoChange(val) {
      this.engine.useVideoDevice(this.stream, val).then((res) => {
        let option = {title: "切换摄像头", showClose: false, message: res.extendedData}
        res.errorCode === 0 ? this.$notify.success(option) : this.$notify.error(option)
      })
    },
    startCompetition() {
      this.sendObject({
        "index": this.questionIdx,
        "roomId": this.config.roomId,
        "handler": "get_question"
      })
      this.startState = true
    },
    nextQuestion() {
      this.questionIdx++
      this.startCompetition()
      if (this.questionIdx === 20) {
        this.endState = true
      }
    },
    endGame() {
      this.sendObject({
        "handler": "end_game",
        "roomId": this.config.roomId
      })
      getRoom(this.config.roomId).then(res => {
        res.data.status = 0
        updateRoom(res.data).then(res => {
          console.log(res)
          this.leaveRoom()
        })
      })
    },
    mutePlayer(val) {
      console.log(val)
      this.sendObject({
        "roomId": this.config.roomId,
        "users": [val],
        "handler": "mute_player"
      })
      this.$notify.info("已发送开麦指令")
    }
  },
  created() {
    let zg = useZgEngineStore()
    let wsStore = useWebSocket()

    this.sendObject = wsStore.sendObject
    this.engine = zg.engine
    this.config = zg._config
    this.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
    this.engine.setDebugVerbose(false)
    this.engine.on('publisherStateUpdate', result => {
      console.log(result)
    })
    this.engine.on('publishQualityUpdate', (streamID, stats) => {
      console.log(stats)
    })
    this.engine.on('roomStreamUpdate', async (roomID, updateType, streamList, extendedData) => {
      if (updateType === 'ADD') {
        let stream = streamList.filter(v => {
          return this.playStreamList.every(e => e.streamID !== v.streamID);
        })
        for (let streamElement of stream) {
          this.engine.startPlayingStream(streamElement.streamID).then(res => {
            this.$refs[streamElement.extraInfo].srcObject = res
            this.userInfo[streamElement.extraInfo] = streamElement.user.userName
            this.playStreamList.push(streamElement)
          })
        }
      }
    })
    wsStore.ws.onmessage = (e) => {
      let mes = JSON.parse(e.data)
      switch (mes.msg) {
        case "mute": {
          if (this.config.roleId !== 1) {
            this.muteMicrophone(mes.data)
            mes.data || setTimeout(() => {
              this.$notify.warning({
                title: "三秒后闭麦",
                duration: 3000
              })
              setTimeout(() => {
                this.$notify.warning({
                  title: "闭麦",
                  duration: 3000
                })
              }, 3000)
            }, 7000)
          }
          this.questionIdx++
          break;
        }
        case "question" : {
          if (this.config.roleId !== 1) {
            this.muteMicrophone(true)
          }
          let title = (this.questionIdx + 1) + "." + mes.data.question
          let content = "A." + mes.data.a + '\n' + "B." + mes.data.b + '\n' + "C." + mes.data.c + '\n' + "D." + mes.data.d
          this.$notify.info({
            title: title,
            message: content,
            duration: 0,
            showClose: true
          })
          if (this.config.roleId === 1) {
            this.$message.success({
              message: "正确答案：" + mes.data.answer,
              duration: 0,
              showClose: true
            })
          }
          break;
        }
        case 'end_game': {
          this.leaveRoom()
          break;
        }
        default: {
          this.$notify.warning("发生异常，请查看控制台")
          console.log(mes)
        }
      }
    }
    this.$notify.warning({
      title: "注意事项",
      message: "1.题目共二十题，A、B队轮流答题\n2.裁判指定者回答，其余闭麦\n",
      duration: 5000,
      showClose: true
    })
    zg.listDevices().then((res) => {
      res.cameras.forEach((item) => {
        this.videoDevice.push({value: item.deviceID, label: item.deviceName})
      })
      res.microphones.forEach((item) => {
        this.audioDevice.push({value: item.deviceID, label: item.deviceName})
      })
      res.speakers.forEach((item) => {
        this.outputDevice.push({value: item.deviceID, label: item.deviceName})
      })
      this.audioChoose = this.audioDevice[0]
      this.videoChoose = this.videoDevice[0]
      this.microphoneChoose = this.outputDevice[0]
      this.enterRoom()
    })
  },
  beforeDestroy() {
    this.engine && this.engine.leaveRoom()
  }
}
</script>

<style scoped>
#videoBox {
  margin-top: 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

#judge {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.player {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
}

.playerA {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: flex-start;
}

.playerB {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: flex-end;
}

.videoContent {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

span {
  margin-top: 5px;
}

video {
  height: 180px;
  width: 240px;
}
</style>