package com.rockbb.test.wsdemo;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class SocketClient {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SocketClient.class);

    public static void main(String[] args) throws URISyntaxException {

        WebSocketClient wsClient = new WebSocketClient(
                new URI("ws://127.0.0.1:8763/websocket/server/10001")) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                log.info("On open: {}, {}", serverHandshake.getHttpStatus(), serverHandshake.getHttpStatusMessage());
            }

            @Override
            public void onMessage(String s) {
                log.info("On message: {}", s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                log.info("On close: {}, {}, {}", i, s, b);
            }

            @Override
            public void onError(Exception e) {
                log.info("On error: {}", e.getMessage());
            }
        };

        wsClient.connect();
        log.info("Connecting...");
        while (!ReadyState.OPEN.equals(wsClient.getReadyState())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Connected");

        wsClient.send("hello");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String line = scanner.next();
            wsClient.send(line);
        }
        wsClient.close();
    }
}
