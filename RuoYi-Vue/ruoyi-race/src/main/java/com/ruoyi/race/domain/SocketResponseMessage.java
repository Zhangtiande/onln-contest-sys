package com.ruoyi.race.domain;

import java.io.Serializable;

/**
 * 套接字响应消息
 *
 * @author kjleo
 * @date 2022/12/03
 */
public class SocketResponseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 0:正常
     * 401：权限验证失败
     * 601：用户不在线
     */
    private int code = 0;

    /**
     * 调用方法
     */
    private String msg;


    /**
     * 数据
     */
    private Object data;

    public SocketResponseMessage(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public SocketResponseMessage(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public SocketResponseMessage(int code, Object data) {
        this.code = code;
        this.data = data;
    }

    public SocketResponseMessage(String msg, Object data) {
        this.msg = msg;
        this.data = data;
    }

    public SocketResponseMessage(String msg) {
        this.msg = msg;
    }

    public SocketResponseMessage(Object data) {
        this.data = data;
    }

    public SocketResponseMessage(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "SocketResponseMessage{" + "code=" + code + ", msg='" + msg + '\'' + ", data=" + data + '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
