import {defineStore} from 'pinia'
import '/src/ZegoExpressWebRTC-1.0.0'

export const useZgEngineStore = defineStore('ZgEngine', () => {
    const baseApi = "https://experience.zegonetwork.com"
    const _config = {
        appid: 1234567,
        deviceId: "",
        deviceType: "",
        anType: 0,
        idName: "PC" + Date.now(),
        nickName: "PC" + Date.now(),
        dispatchServer: baseApi + ":15443/dispatch/connection",
        logLevel: 5,
        logUrl: "",
        remoteLogLevel: 5,
        roomFlag: true,
        testEnvironment: false, // 摄像头
        videoType: null,
        roomId: 0,
        roleId: 10,
        streamID :(Date.now() & 20010327).toString()
    }
    const engine = new ZegoExpressEngine(_config.appid, baseApi + ":15443/dispatch/connection");

    async function login(token) {
        return await engine.loginHall(token, {
            userID: _config.nickName, userName: _config.idName
        }, {
            deviceID: _config.deviceId,
            deviceType: _config.deviceType,
            anType: _config.anType,
            roomFlag: true,
            userUpdate: true
        })
    }

    function checkBrowser() {
        engine.checkSystemRequirements(null, 0).then((res) => {
            const {camera, screenSharing, videoCodec, webRTC} = res;

            if (!webRTC) {
                alert('浏览器不支持 webRtc，更换最新的 chrome 浏览器');
            }
            if (!camera) {
                alert('浏览器不支持获取摄像头麦克风设备');
            }
            if (!screenSharing) {
                alert('浏览器不支持屏幕共享');
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

    function init(config) {
        _config.deviceId = "device" + config.userId
        _config.nickName = config.nickName
        _config.idName = config.userId.toString()
    }

    return {
        _config, engine, login, listDevices, checkBrowser, init
    }
})
