package com.rockbb.test.wsdemo3.controller;

import com.rockbb.test.wsdemo3.dto.MessageDTO;
import com.rockbb.test.wsdemo3.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MessageController {

    @Autowired
    private SimpUserRegistry simpUserRegistry;
    @Autowired
    private MessageService messageService;

    /**
     * List all connected users
     */
    @GetMapping("/ws/users")
    @ResponseBody
    public List<String> connectedEquipments() {
        return this.simpUserRegistry
                .getUsers()
                .stream()
                .map(SimpUser::getName)
                .collect(Collectors.toList());
    }

    @GetMapping("/ws/private-message/{id}")
    @ResponseBody
    public String sendPrivateMessage(@PathVariable("id") final String id) {
        MessageDTO dto = new MessageDTO();
        dto.setMessage("it is a private message");
        messageService.notifyUser(id, dto);
        return "succ";
    }

    // Handles messages sent to {prefix}/chat. prefix is defined in configureMessageBroker()
    @MessageMapping("/chat")
    // Sends the return value of this method to /topic/messages
    @SendTo("/topic/messages")
    public MessageDTO getMessages(MessageDTO dto){
        return dto;
    }

    // Handles messages sent to {prefix}/private-message. prefix is defined in configureMessageBroker()
    @MessageMapping("/private-message")
    // Sends the return value of this method to /topic/messages
    @SendToUser("/topic/private-messages")
    public MessageDTO getPrivateMessages(final MessageDTO dto, final Principal principal){
        dto.setMessage("private message to " + principal.getName() + ": " + dto.getMessage());
        return dto;
    }
}
