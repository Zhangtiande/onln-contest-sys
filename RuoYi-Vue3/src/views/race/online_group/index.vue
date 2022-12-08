<template>
  <div>
    <el-row style="margin-top:10px; margin-left: 5%;">
      <el-col :span="4">
        <el-select v-model="roomInfo.microphoneChoose" placeholder="麦克风" @change="audioChange">
          <el-option
              v-for="item in roomInfo.audioDevice"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select v-model="roomInfo.videoChoose" placeholder="视频设备" @change="videoChange">
          <el-option
              v-for="item in roomInfo.videoDevice"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select v-model="roomInfo.audioChoose" placeholder="扬声器">
          <el-option
              v-for="item in roomInfo.outputDevice"
              :key="item.value"
              :label="item.label"
              :value="item.value"></el-option>
        </el-select>
      </el-col>
      <el-col v-if="isJudge" :span="8"
              style="display: flex;flex-direction: row;justify-content: space-between;">
        <el-select v-model="roomInfo.chooseUser" placeholder="指定选手回答" value-key="nickName" @change="mutePlayer">
          <el-option
              v-for="item in wsStore.user"
              :key="item.userId"
              :label="item.nickName"
              :value="item.userId"></el-option>
        </el-select>
        <el-button :disabled="roomInfo.startState" type="success"
                   @click="startCompetition">开始
        </el-button>
        <el-badge :value="20-roomInfo.questionIdx" class="item" type="primary">
          <el-button :disabled="roomInfo.endState" type="primary" @click="startCompetition">下一题
          </el-button>
        </el-badge>
        <el-button type="danger" @click="endGame">结束比赛</el-button>

      </el-col>

    </el-row>
  </div>

  <div id="videoBox">
    <div id="judge">
      <span>{{ userInfo.judge }}</span>
      <video ref="judge" autoplay controls muted playsinline></video>
    </div>
    <div class="player">
      <div class="playerA">
        <div class="videoContent" style="">
          <span>{{ "A1:" + userInfo.player1 }}</span>
          <video ref="player1" autoplay controls muted playsinline></video>
        </div>
        <div class="videoContent">
          <span>{{ "A2:" + userInfo.player2 }}</span>
          <video ref="player2" autoplay controls muted playsinline></video>
        </div>
      </div>
      <div class="playerB">
        <div class="videoContent">
          <span>{{ "B1:" + userInfo.player3 }}</span>
          <video ref="player3" autoplay controls muted playsinline></video>
        </div>
        <div class="videoContent">
          <span>{{ "B2:" + userInfo.player4 }}</span>
          <video ref="player4" autoplay controls muted playsinline></video>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import {computed, getCurrentInstance, onBeforeUnmount, onMounted, watch} from "vue";
import {getRoom, updateRoom} from "@/api/race/room";
import {useZgEngineStore} from "@/store/modules/ZgEngine";
import {useWebSocket} from "@/store/modules/WebSocket";
import {reactive} from "@vue/reactivity";
import "@/ZegoExpressWebRTC-1.0.0"

const {proxy} = getCurrentInstance();
const zegoStore = useZgEngineStore();
const wsStore = useWebSocket();
const roomInfo = reactive({
  audioDevice: [],
  videoDevice: [],
  outputDevice: [],
  startState: false,
  endState: false,
  audioChoose: "",
  videoChoose: "",
  microphoneChoose: "",
  questionIdx: 0,
  stream: undefined,
  playStreamList: [],
  chooseUser: ""
})
const userInfo = reactive({judge: "", player1: "", player2: "", player3: "", player4: ""})
const isJudge = computed(() => zegoStore.config.roleId === 1)

watch(() => roomInfo.questionIdx, (idx) => {
  if (idx >= 20)
    roomInfo.endState = true
})

onMounted(() => {
  zegoStore.engine.setLogConfig({logLevel: 'error', remoteLogLevel: 'error', logUrl: ""})
  zegoStore.engine.setDebugVerbose(false)
  zegoStore.engine.on('publisherStateUpdate', result => {
    console.log(result)
  })
  zegoStore.engine.on('publishQualityUpdate', (streamID, stats) => {
    console.log(stats)
  })
  zegoStore.engine.on('roomStreamUpdate', async (roomID, updateType, streamList, extendedData) => {
    if (updateType === 'ADD') {
      let stream = streamList.filter(v => {
        return roomInfo.playStreamList.every(e => e.streamID !== v.streamID);
      })
      for (let streamElement of stream) {
        zegoStore.engine.startPlayingStream(streamElement.streamID).then(res => {
          proxy.$refs[streamElement.extraInfo].srcObject = res
          userInfo[streamElement.extraInfo] = streamElement.user.userName
          roomInfo.playStreamList.push(streamElement)
        })
      }
    }
  })
  wsStore.ws.onmessage = (e) => {
    let mes = JSON.parse(e.data)
    switch (mes.msg) {
      case "mute": {
        if (isJudge) {
          muteMicrophone(mes.data)
          mes.data || setTimeout(() => {
            proxy.$notify.warning({
              title: "三秒后闭麦",
              duration: 3000
            })
            setTimeout(() => {
              proxy.$notify.warning({
                title: "闭麦",
                duration: 3000
              })
            }, 3000)
          }, 7000)
        }
        roomInfo.questionIdx++
        break;
      }
      case "question" : {
        isJudge || muteMicrophone(true)
        let title = (roomInfo.questionIdx + 1) + "." + mes.data.question
        let content = "A." + mes.data.a + '\n' + "B." + mes.data.b + '\n' + "C." + mes.data.c + '\n' + "D." + mes.data.d
        proxy.$notify.info({
          title: title,
          message: content,
          duration: 0,
          showClose: true
        })
        !isJudge || proxy.$message.success({
          message: "正确答案：" + mes.data.answer,
          duration: 0,
          showClose: true
        })
        break;
      }
      case 'end_game': {
        leaveRoom()
        break;
      }
      default: {
        proxy.$notify.warning("发生异常，请查看控制台")
        console.log(mes)
      }
    }
  }
  proxy.$notify.warning({
    title: "注意事项",
    message: "1.题目共二十题，A、B队轮流答题\n2.裁判指定者回答，其余闭麦\n",
    duration: 5000,
    showClose: true
  })
  zegoStore.listDevices().then((res) => {
    res.cameras.forEach((item) => {
      roomInfo.videoDevice.push({value: item.deviceID, label: item.deviceName})
    })
    res.microphones.forEach((item) => {
      roomInfo.audioDevice.push({value: item.deviceID, label: item.deviceName})
    })
    res.speakers.forEach((item) => {
      roomInfo.outputDevice.push({value: item.deviceID, label: item.deviceName})
    })
    roomInfo.audioChoose = roomInfo.audioDevice[0]
    roomInfo.videoChoose = roomInfo.videoDevice[0]
    roomInfo.microphoneChoose = roomInfo.outputDevice[0]
    enterRoom()
  })
})

onBeforeUnmount(() => {
  leaveRoom()
})

function enterRoom() {
  zegoStore.engine.enterRoom(zegoStore.config.roomId.toString(), zegoStore.config.roleId).then((res) => {
    if (res.errorCode === 0) {
      proxy.$message.success("进入房间成功！")
      zegoStore.engine.createStream().then((res) => {
        roomInfo.stream = res
        if (isJudge) {
          proxy.$refs.judge.srcObject = res
          zegoStore.engine.startPublishingStream(zegoStore.config.streamID, res, {extraInfo: "judge"})
          userInfo.judge = zegoStore.config.nickName
        } else {
          let idx = wsStore.user.findIndex(item => item.userId === zegoStore.config.userId)
          proxy.$refs["player" + (idx + 1)].srcObject = res
          zegoStore.engine.startPublishingStream(zegoStore.config.streamID, res, {extraInfo: "player" + (idx + 1)})
        }
      }).catch(err => {
        console.log(err)
        proxy.$message.error(err.msg)
      })
    } else {
      proxy.$message.error(res.extendedData)
    }
  })
}

function leaveRoom() {
  roomInfo.playStreamList.forEach((item) => {
    zegoStore.engine.stopPlayingStream(item)
  })
  zegoStore.engine.stopPublishingStream(zegoStore.config.streamID)
  zegoStore.engine.destroyStream(roomInfo.stream)
}

function muteMicrophone(state) {
  let res = zegoStore.engine.mutePublishStreamAudio(roomInfo.stream, state)
  if (res) {
    proxy.$message.success(state !== true ? "解禁麦克风" : "关闭麦克风")
  }
}

function audioChange(val) {
  zegoStore.engine.useAudioDevice(roomInfo.stream, val).then((res) => {
    let option = {title: "切换麦克风", showClose: false, message: res.extendedData}
    res.errorCode === 0 ? proxy.$notify.success(option) : proxy.$notify.error(option)
  })
}

function videoChange(val) {
  zegoStore.engine.useVideoDevice(roomInfo.stream, val).then((res) => {
    let option = {title: "切换摄像头", showClose: false, message: res.extendedData}
    res.errorCode === 0 ? proxy.$notify.success(option) : proxy.$notify.error(option)
  })
}

function startCompetition() {
  wsStore.sendObject({
    "index": roomInfo.questionIdx++,
    "roomId": zegoStore.config.roomId,
    "handler": "get_question"
  })
  roomInfo.startState = true
}

function endGame() {
  wsStore.sendObject({
    "handler": "end_game",
    "roomId": zegoStore.config.roomId
  })
  getRoom(zegoStore.config.roomId).then(res => {
    res.data.status = 0
    updateRoom(res.data).then(res => {
      console.log(res)
      leaveRoom()
    })
  })
}

function mutePlayer(val) {
  console.log(val)
  wsStore.sendObject({
    "roomId": zegoStore.config.roomId,
    "users": [val],
    "handler": "mute_player"
  })
  proxy.$notify.info("已发送开麦指令")
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