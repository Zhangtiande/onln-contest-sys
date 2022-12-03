package com.ruoyi.race.config;

import com.alibaba.fastjson2.JSON;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * 网络套接字自定义编码
 * 在 websocket 中直接发送 obj 会有问题 - No encoder specified for object of class
 * 需要对 obj 创建解码类，实现 websocket 中的 Encoder.Text<>
 *
 * @author kjleo
 * @date 2022/12/03
 */
public class WebSocketCustomEncoding implements Encoder.Text<Object> {
    /**
     * The Encoder interface defines how developers can provide a way to convert their
     * custom objects into web socket messages. The Encoder interface contains
     * subinterfaces that allow encoding algorithms to encode custom objects to:
     * text, binary data, character stream and write to an output stream.
     * Encoder 接口定义了如何提供一种方法将定制对象转换为 websocket 消息
     * 可自定义对象编码为文本、二进制数据、字符流、写入输出流
     * Text、TextStream、Binary、BinaryStream
     */

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String encode(Object o) throws EncodeException {
        return JSON.toJSONString(o);
    }
}

