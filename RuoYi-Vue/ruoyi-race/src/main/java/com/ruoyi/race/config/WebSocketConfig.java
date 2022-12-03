package com.ruoyi.race.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


/**
 * 网络套接字配置
 *
 * @author kjleo
 * @date 2022/12/02
 */
@Configuration
public class WebSocketConfig {

    // 使用boot内置tomcat时需要注入此bean
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

