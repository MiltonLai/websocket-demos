package com.rockbb.test.wsdemo2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter initServerEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
