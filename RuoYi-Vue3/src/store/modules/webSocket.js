import {defineStore} from 'pinia'
import {getToken} from "@/utils/auth";

export const useWebSocket = defineStore('WebSocket', () => {

    const ws = new WebSocket("ws://159.138.0.68:4040/ws/" + getToken())

    function sendObject(data) {
        ws.send(JSON.stringify(data))
    }

    return {
        ws, sendObject
    }
})