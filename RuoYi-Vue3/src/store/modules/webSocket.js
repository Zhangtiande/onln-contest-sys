import {defineStore} from 'pinia'
import {getToken} from "@/utils/auth";
import user from "@/store/modules/user";
import {reactive} from "@vue/reactivity";

export const useWebSocket = defineStore('WebSocket', {
    state: () => ({
        ws: new WebSocket("wss://www.923yyds.top:4041/wss/" + getToken()),
        user: []
    }),
    getters: {
        getUsers: (state) => state.user,
    },
    actions: {
        sendObject(data) {
            this.ws.send(JSON.stringify(data))
        }
    }

})