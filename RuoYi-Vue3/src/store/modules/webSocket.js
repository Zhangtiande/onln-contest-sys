import {defineStore} from 'pinia'
import {getToken} from "@/utils/auth";

export const useWebSocket = defineStore('WebSocket', () => {

    const ws = new WebSocket("wss://www.923yyds.top:4041/wss/" + getToken())
    function sendObject(data) {
        ws.send(JSON.stringify(data))
    }

    return {
        ws, sendObject
    }
})