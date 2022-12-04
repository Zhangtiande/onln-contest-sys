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
      <el-col :span="4">
        <el-button :disabled="startState" ref="start" type="primary" @click="startCompetition">开始</el-button>
        <el-button :disabled="endState" @click="nextQuestion" type="primary">下一题</el-button>
        <el-button  @click="endGame" type="danger">结束比赛</el-button>
      </el-col>

    </el-row>
  </div>

  <div id="videoBox">
    <div id="judge">
      <span>{{ this.userInfo.judge }}</span>
      <video ref="judge_video" poster="@/assets/images/profile.jpg"></video>
    </div>
    <div id="player">
      <div style="display: flex; flex-direction: column;align-items: center;">
        <span>{{ this.userInfo.player1 }}</span>
        <video ref="player1" poster="@/assets/images/profile.jpg"></video>
      </div>
      <div style="display: flex; flex-direction: column;align-items: center;">
        <span>{{ this.userInfo.player2 }}</span>
        <video ref="player2" poster="@/assets/images/profile.jpg"></video>
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
      ws: undefined,
      userInfo: {
        judge: "",
        player1: "",
        player2: ""
      },
      keyStart: false,
      questionIdx: 0,
    }
  },
  methods: {
    enterRoom() {
      this.engine.enterRoom(this.config.roomId, this.config.roleId).then((res) => {
        if (res.errorCode === 0) {
          this.$message.success("进入房间成功！")
          this.engine.createStream().then((res) => {
            this.stream = res
            if (this.config.roleId === 1) {
              this.$refs.judge_video.srcObject = this.stream
              this.engine.startPublishingStream(this.config.streamID, res, {extraInfo: "judge"})

            } else {
              this.$refs.player1.srcObject = this.stream
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
        this.$message.success(state !== true ? "抢答成功，解禁麦克风，开始回答" : "开始抢答，关闭麦克风")
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
      let data = {
        "index": this.questionIdx,
        "roomId": this.config.roomId,
        "handler": "get_question"
      }
      this.ws.send(JSON.stringify(data))
      this.startState = true
    },
    nextQuestion() {
      this.questionIdx++
      this.startCompetition()
      if (this.questionIdx === 10){
        this.endState = true
      }
    },
    endGame() {
      let data = {
        "handler": "end_game",
        "roomId": this.config.roomId
      }
      this.ws.send(JSON.stringify(data))
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
    this.ws = useWebSocket().ws
    this.engine = zg.engine
    this.config = zg._config
    this.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
    this.engine.setDebugVerbose(false)
    this.engine.on('roomStreamUpdate', async (roomID, updateType, streamList, extendedData) => {
      if (updateType === 'ADD') {
        let stream = streamList.filter(v => {
          return this.playStreamList.every(e => e.streamID !== v.streamID);
        })
        this.playStreamList.push(stream)
        this.engine.startPlayingStream(stream.streamID).then(res => {
          if (stream.extraInfo === "judge") {
            this.$refs.judge_video.srcObject = res
            this.userInfo.judge = stream.user.userName
          } else {
            this.$refs.player2.srcObject = res
            this.userInfo.player2 = stream.user.userName
          }
        })
      }
    });
    this.engine.on('IMRecvBroadcastMessage', (roomId, charData) => {
      this.createMsg(charData)
    })
    this.ws.onmessage = (e) => {
      let mes = JSON.parse(e.data)
      switch (mes.msg) {
        case "get" : {
          this.muteMicrophone(false)
          setInterval(() => {
            this.$notify.warning({
              title: "三秒后闭麦",
              duration: 3000
            })
            setInterval(() => {
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
          this.$message.info("抢答失败，不开启麦克风")
          this.muteMicrophone(true)
          this.questionIdx++
          break;
        }
        case "question": {
          if (this.config.roleId !== 1) {
            this.muteMicrophone(true)
          }
          let title = (this.questionIdx + 1) + "." + mes.question
          let content = "A." + mes.a + '\n' + "B." + mes.b + '\n' + "C." + mes.c + '\n' + "D." + mes.d
          this.$notify.info({
            title: title,
            message: content,
            duration: 0,
            showClose: true
          })
          if (this.config.roleId === 1) {
            this.$notify.success({
              title: "正确答案",
              message: mes.answer,
              duration: 0,
              showClose: true
            })
          }
          this.keyStart = true
          break;
        }
      }
    }
    window.onkeydown = (e) => {
      if (e.key === "Enter" && this.keyStart && this.config.roleId === 10) {
        let data = {
          "index": this.questionIdx,
          "roomId": this.config.roomId,
          "handler": "answer_right"
        }
        this.ws.send(JSON.stringify(data))
        this.keyStart = false
      }
    }

    zg.checkBrowser()
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
    })
    this.userInfo.player1 = this.config.nickName
    this.enterRoom()
    this.$notify.warning({
      title: "注意事项",
      message: "1.当题目显示后按回车键抢答。\n2.抢答失败将被闭麦\n3.回答时间为10s，时间到将闭麦\n4.等待裁判计分并点击下一题循环\n" +
          "5.十题后比赛结束，裁判宣判结果后可点击结束比赛按钮",
      duration: 10000
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