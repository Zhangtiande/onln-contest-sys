<template>
  <div>
    <el-row style="margin-top:2%; justify-content: center">
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
      <el-col v-if="this.config.roleId === 1" :span="4"
              style="display: flex;flex-direction: row;justify-content: space-between;">
        <el-button :disabled="startState" type="success"
                   @click="startCompetition">开始
        </el-button>
        <el-badge :value="10-this.questionIdx" class="item" type="primary">
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
      <video ref="judge_video" autoplay controls muted playsinline></video>
    </div>
    <div id="player">
      <div style="display: flex; flex-direction: column;align-items: center;">
        <span>{{ this.userInfo.player1 }}</span>
        <video ref="player1" autoplay controls muted playsinline></video>
      </div>
      <div style="display: flex; flex-direction: column;align-items: center;">
        <span>{{ this.userInfo.player2 }}</span>
        <video ref="player2" autoplay controls muted playsinline></video>
      </div>
    </div>
  </div>

</template>

<!--suppress JSUnusedLocalSymbols, JSUnresolvedFunction -->
<script>
import {useZgEngineStore} from "@/store/modules/ZgEngine";
import {useWebSocket} from "@/store/modules/webSocket";
import {getRoom, updateRoom} from "@/api/race/room";
// noinspection JSUnusedLocalSymbols
export default {
  name: "index",
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
      microphoneState: true,
      audioChoose: "",
      videoChoose: "",
      microphoneChoose: "",
      sendText: "",
      sharing: false,
      userInfo: {
        judge: "",
        player1: "",
        player2: ""
      },
      keyStart: false,
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
              this.$refs.judge_video.srcObject = res
              this.engine.startPublishingStream(this.config.streamID, res, {extraInfo: "judge"})

            } else {
              this.$refs.player1.srcObject = res
              this.engine.startPublishingStream(this.config.streamID, res)
            }

          }).catch(err => {
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
    send() {
      if (!this.sendText) {
        return;
      }
      this.engine.sendBroadcastMessage(this.roomId.toString(), this.sendText).then(() => {
        this.sendText = ""
      }).catch(() => {
        this.$message.warning("消息发送失败")
      })
    },
    createMsg(content) {
      console.log(content)
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
      if (this.questionIdx === 10) {
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
    }
  },
  created() {
    let zg = useZgEngineStore()
    let wsStore = useWebSocket()
    let ws = wsStore.ws
    this.sendObject = wsStore.sendObject
    this.engine = zg.engine
    this.config = zg._config
    this.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
    this.engine.setDebugVerbose(false)
    this.engine.on('roomStreamUpdate', async (roomID, updateType, streamList, extendedData) => {
      if (updateType === 'ADD') {
        let stream = streamList.filter(v => {
          return this.playStreamList.every(e => e.streamID !== v.streamID);
        })
        for (let streamElement of stream) {
          this.engine.startPlayingStream(streamElement.streamID).then(res => {
            if (streamElement.extraInfo === "judge") {
              this.$refs.judge_video.srcObject = res
              this.userInfo.judge = streamElement.user.userName
            } else {
              if (this.config.roleId === 1) {
                if (this.playIdx === 2) {
                  this.$refs.player2.srcObject = res
                  this.userInfo.player2 = streamElement.user.userName
                } else {
                  this.$refs.player1.srcObject = res
                  this.userInfo.player1 = streamElement.user.userName
                  this.playIdx++
                }
              } else {
                this.$refs.player2.srcObject = res
                this.userInfo.player2 = streamElement.user.userName
              }

            }
          })
          this.playStreamList.push(streamElement)
        }
      }
    });
    this.engine.on('IMRecvBroadcastMessage', (roomId, charData) => {
      this.createMsg(charData)
    })
    wsStore.ws.onmessage = (e) => {
      let mes = JSON.parse(e.data)
      switch (mes.msg) {
        case "get" : {
          this.muteMicrophone(false)
          setTimeout(() => {
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
          this.questionIdx++
          break;
        }
        case "lost": {
          if (mes.data === this.questionIdx) {
            this.$message.info("抢答失败，不开启麦克风")
            this.muteMicrophone(true)
            this.questionIdx++
          }
          break;
        }
        case "question" : {
          if (this.config.roleId !== 1) {
            this.muteMicrophone(true)
          }
          console.log(mes)
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
          this.keyStart = true
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
    window.onkeydown = (e) => {
      if (e.key === "Enter" && this.keyStart && this.config.roleId !== 1) {
        this.sendObject({
          "index": this.questionIdx,
          "roomId": this.config.roomId,
          "handler": "answer_right"
        })
        this.keyStart = false
      }
    }
    // zg.checkBrowser()
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
    if (this.config.roleId === 1) {
      this.userInfo.judge = this.config.nickName
    } else {
      this.userInfo.player1 = this.config.nickName
    }
    this.$notify.warning({
      title: "注意事项",
      message: "1.当题目显示后按回车键抢答。\n2.抢答失败将被闭麦，题目不会自动关闭，请手动关闭\n3.回答时间为10s，时间到将闭麦\n4.等待裁判计分并点击下一题\n" +
          "5.十题后比赛结束，裁判宣判结果后可点击结束比赛按钮",
      duration: 10000,
      showClose: true
    })
  }
}
</script>

<style scoped>
#videoBox {
  margin-top: 40px;
  margin-bottom: 40px;
  display: flex;
  flex-direction: column;
}

#judge {
  display: flex;
  flex-direction: column;
  align-items: center;
}

#player {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;
}

video {
  height: 240px;
  width: 240px;
}
</style>