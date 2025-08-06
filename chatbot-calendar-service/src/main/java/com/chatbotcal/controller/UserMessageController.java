package com.chatbotcal.controller;


import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class UserMessageController {

    private final UserMessageService userMessageService;

    @GetMapping
    public List<UserMessage> getAllMessages() {
        return userMessageService.getAllMessages();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        userMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserMessage>> getMessagesByUserId(@PathVariable String userId) {
        List<UserMessage> messages = userMessageService.getMessagesByUserId(userId);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }


}
