package com.rockbb.test.wsdemo3.service;

import com.rockbb.test.wsdemo3.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void notifyUser(final String id, final MessageDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(id, "/topic/private-messages", dto);
    }
}
