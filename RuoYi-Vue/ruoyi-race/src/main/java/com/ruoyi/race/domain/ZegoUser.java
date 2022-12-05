package com.ruoyi.race.domain;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.model.LoginUser;

import java.io.Serializable;

public class ZegoUser implements Serializable {

    public final int seq = 1;
    public final Long timestamp;
    public final int app_id = 1234567;
    public final String user_id;
    public final String user_name;
    public final String device_id;
    public final int queue_role = 1;
    public final int room_role = 0;
    public final int net_type = 2;
    public String token;

    public ZegoUser(LoginUser user) {
        timestamp = (long) Math.ceil(System.currentTimeMillis() / 1000.0);
        user_id = "fcayj" + user.getUserId();
        user_name = user.getUser().getNickName();
        device_id = "device" + user.getUserId();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
