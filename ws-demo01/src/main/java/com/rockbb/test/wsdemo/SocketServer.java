package com.rockbb.test.wsdemo;


import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@ServerEndpoint("/websocket/server/{sessionId}")
public class SocketServer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private String sessionId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        this.sessionId = sessionId;
        /* Old connection will be kicked by new connection */
        sessionMap.put(sessionId, session);
        /*
         * this: instance id. New instances will be created for each sessionId
         * sessionId: assigned from path variable
         * session.getId(): the actual session id (start from 0)
         */
        log.info("On open: this{} sessionId {}, actual {}", this, sessionId, session.getId());
    }

    @OnClose
    public void onClose() {
        sessionMap.remove(sessionId);
        log.info("On close: sessionId {}", sessionId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("On message: sessionId {}, {}", session.getId(), message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("On error: sessionId {}, {}", session.getId(), error.getMessage());
    }

    public static void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public static Session getSession(String sessionId){
        return sessionMap.get(sessionId);
    }
}
