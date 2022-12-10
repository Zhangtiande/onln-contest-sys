import {defineStore} from 'pinia'
import '/src/ZegoExpressWebRTC-1.0.0'

export const useZgEngineStore = defineStore('ZgEngine', {
    state: () => ({
        config: {
            appid: 1234567,
            deviceType: "",
            anType: 0,
            dispatchServer: "https://experience.zegonetwork.com:15443/dispatch/connection",
            videoType: "VP8",
            roomId: 0,
            roleId: 0,
            streamID: (Date.now() & 20010327).toString(),
            users: []
        }, engine: new ZegoExpressEngine(1234567, "https://experience.zegonetwork.com:15443/dispatch/connection")
    }),

    actions: {
        async initEngine(data) {
            // noinspection JSUnresolvedFunction
            this.config.userId = parseInt(data.user_id.split("fcayj")[1])
            this.config.token = data.token
            this.config.deviceId = data.device_id
            this.config.nickName = data.user_name
            this.config.idName = data.user_id
            return await this.engine.loginHall(this.config.token, {
                userID: this.config.idName, userName: this.config.nickName
            }, {
                deviceID: this.config.deviceId,
                deviceType: this.config.deviceType,
                anType: this.config.anType,
                roomFlag: true,
                userUpdate: true
            })
        },
        checkBrowser() {
            this.engine.checkSystemRequirements(null, 0).then((res) => {
                const {camera, screenSharing, videoCodec, webRTC} = res;

                if (!camera || !screenSharing || !webRTC) {
                    let msg = "浏览器检测中，"

                    if (!camera) msg += "摄像头、"
                    if (!screenSharing) msg += "屏幕共享、"
                    if (!webRTC) msg += "WebRtc"
                    msg += "出现问题，请重新进入页面或换一个浏览器进入页面。"
                    window.alert(msg)
                    return
                }
                this.config.videoType = videoCodec.H264 ? 'H264' : 'VP8';
            })
        },
        listDevices() {
            return new Promise((resolve) => {
                this.engine.enumDevices().then((res) => {
                    const deviceMap = {
                        microphones: '音频输入设备', speakers: '音频输出设备', cameras: '视频输输入设备',
                    }

                    for (const key in res) {
                        if (res.hasOwnProperty(key)) {
                            const element = res[key];
                            !element.length && alert('没有找到' + deviceMap[key])
                        }
                    }
                    resolve(res)
                })
            });
        }
    }


})
