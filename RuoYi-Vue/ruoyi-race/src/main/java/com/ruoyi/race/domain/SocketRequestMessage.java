package com.ruoyi.race.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 套接字请求消息
 *
 * @author kjleo
 * @date 2022/12/03
 */
@SuppressWarnings("unused")
public class SocketRequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private int index;

    private Long roomId;

    private String handler;

    private List<Long> users;


    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Long> getUsers() {
        return users;
    }

    public void setUsers(List<Long> users) {
        this.users = users;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }
}
