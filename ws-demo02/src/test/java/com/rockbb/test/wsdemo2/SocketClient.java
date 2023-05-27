package com.rockbb.test.wsdemo2;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class SocketClient {

    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        Object condition = new Object();

        WebSocketClient wsClient = new WebSocketClient(new URI("ws://127.0.0.1:8763/websocket/server/10001")) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                log.info("On open: {}, {}", serverHandshake.getHttpStatus(), serverHandshake.getHttpStatusMessage());
            }

            @Override
            public void onMessage(String s) {
                log.info("On message: {}", s);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                //To overwrite
                byte mark = bytes.get(0);
                if (mark == 2) {
                    synchronized (condition) {
                        condition.notify();
                    }
                    log.info("receive ack for file info");
                } else if (mark == 6){
                    synchronized (condition) {
                        condition.notify();
                    }
                    log.info("receive ack for file end");
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                log.info("On close: {}, {}, {}", i, s, b);
            }

            @Override
            public void onError(Exception e) {
                log.error("On error: {}", e.getMessage());
            }
        };

        wsClient.connect();

        log.info("Connecting ...");
        while (!ReadyState.OPEN.equals(wsClient.getReadyState())) {

        }
        log.info("Connected");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String line = scanner.next();
            if ("1".equals(line))
                sendFile(wsClient, condition);
            else
                wsClient.send(line);
        }
    }

    public static void sendFile(WebSocketClient webSocketClient, Object condition){
        new Thread(() -> {
            try {
                SeekableByteChannel byteChannel = Files.newByteChannel(
                        Path.of("/home/milton/Backup/linux/apache-tomcat-8.5.58.tar.gz"),
                        new StandardOpenOption[]{StandardOpenOption.READ});

                ByteBuffer byteBuffer = ByteBuffer.allocate(4*1024);

                byteBuffer.put((byte)1);
                String info = "{\"fileName\": \"apache-tomcat-8.5.58.tar.gz\", \"fileSize\":"+byteChannel.size()+"}";
                byteBuffer.put(info.getBytes(StandardCharsets.UTF_8));
                byteBuffer.flip();
                webSocketClient.send(byteBuffer);
                synchronized (condition) {
                    condition.wait();
                }

                byteBuffer.clear();
                byteBuffer.put((byte)3);
                while (byteChannel.read(byteBuffer) > 0) {
                    /* flip: read mode -> write mode */
                    byteBuffer.flip();
                    webSocketClient.send(byteBuffer);
                    byteBuffer.clear();
                    byteBuffer.put((byte)3);
                }

                byteBuffer.clear();
                byteBuffer.put((byte)5);
                byteBuffer.put("end".getBytes(StandardCharsets.UTF_8));
                byteBuffer.flip();
                webSocketClient.send(byteBuffer);
                synchronized (condition) {
                    condition.wait();
                }
                byteChannel.close();

            } catch (InterruptedException|IOException e) {
                log.error(e.getMessage(), e);
            }

        }).start();
    }
}
