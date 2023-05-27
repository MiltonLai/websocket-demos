package com.rockbb.test.wsdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.Session;
import java.io.IOException;

@RestController
@SpringBootApplication
public class WsDemo01App {

    public static void main(String[] args) {
        SpringApplication.run(WsDemo01App.class, args);
    }

    @RequestMapping("/msg")
    public String sendMsg(String sessionId, String msg) throws IOException {
        Session session = SocketServer.getSession(sessionId);
        SocketServer.sendMessage(session, msg);
        return "send " + sessionId + " : " + msg;
    }
}
