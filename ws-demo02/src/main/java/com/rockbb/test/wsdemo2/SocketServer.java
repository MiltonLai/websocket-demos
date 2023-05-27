package com.rockbb.test.wsdemo2;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
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
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@ServerEndpoint("/websocket/server/{sessionId}")
public class SocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private String sessionId = "";

    private SeekableByteChannel byteChannel;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        this.sessionId = sessionId;
        sessionMap.put(sessionId, session);
        log.info("On open: sessionId {}", sessionId);
    }

    @OnClose
    public void onClose() {
        sessionMap.remove(sessionId);
        log.info("On close: sessionId {}", sessionId);

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("On message: {}, {}", session.getId(), message);
    }

    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, Session session) throws IOException {
        if (byteBuffer.limit() == 0) {
            return;
        }

        byte mark = byteBuffer.get(0);
        if (mark == 1) {
            log.info("mark 1");
            byteBuffer.get();
            String info = new String(
                    byteBuffer.array(),
                    byteBuffer.position(),
                    byteBuffer.limit() - byteBuffer.position());
            FileInfo fileInfo = new JsonMapper().readValue(info, FileInfo.class);
            byteChannel = Files.newByteChannel(
                    Path.of("/home/milton/Downloads/" + fileInfo.getFileName()),
                    new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE});
            //ack
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            buffer.put((byte) 2);
            buffer.put("receive fileinfo".getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            session.getBasicRemote().sendBinary(buffer);
        } else if (mark == 3) {
            log.info("mark 3");
            byteBuffer.get();
            byteChannel.write(byteBuffer);
        } else if (mark == 5) {
            log.info("mark 5");
            //ack
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            buffer.clear();
            buffer.put((byte) 6);
            buffer.put("receive end".getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            session.getBasicRemote().sendBinary(buffer);
            byteChannel.close();
            byteChannel = null;
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("On error: {}, {}", session.getId(), error.getMessage());
    }

    public static void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public static Session getSession(String sessionId){
        return sessionMap.get(sessionId);
    }

    public static class FileInfo implements Serializable {
        private String fileName;
        private long fileSize;

        public String getFileName() {return fileName;}
        public void setFileName(String fileName) {this.fileName = fileName;}
        public long getFileSize() {return fileSize;}
        public void setFileSize(long fileSize) {this.fileSize = fileSize;}
    }
}
