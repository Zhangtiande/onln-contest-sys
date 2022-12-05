import {defineStore} from 'pinia'
import '/src/ZegoExpressWebRTC-1.0.0'

export const useZgEngineStore = defineStore('ZgEngine', () => {
    const baseApi = "https://experience.zegonetwork.com"
    const _config = {
        appid: 1234567,
        deviceType: "",
        anType: 0,
        dispatchServer: baseApi + ":15443/dispatch/connection",
        videoType: "VP8",
        roomId: 0,
        roleId: 0,
        streamID: (Date.now() & 20010327).toString()
    }
    const engine = new ZegoExpressEngine(_config.appid, _config.dispatchServer);

    function checkBrowser() {
        engine.checkSystemRequirements(null, 0).then((res) => {
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
            _config.videoType = videoCodec.H264 ? 'H264' : 'VP8';

        })
    }

    function listDevices() {
        return new Promise((resolve) => {
            engine.enumDevices().then((res) => {
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

    async function init(config) {
        _config.token = config.token
        _config.deviceId = config.device_id
        _config.nickName = config.user_name
        _config.idName = config.user_id
        return await engine.loginHall(_config.token, {
            userID: _config.idName, userName: _config.nickName
        }, {
            deviceID: _config.deviceId,
            deviceType: _config.deviceType,
            anType: _config.anType,
            roomFlag: true,
            userUpdate: true
        })
    }

    return {
        _config, engine, listDevices, checkBrowser, init
    }
})
