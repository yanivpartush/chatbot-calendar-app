package com.chatbotcal.controller;

import com.chatbotcal.controller.dto.UserMessageResponseDto;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages.properties")
@RequiredArgsConstructor
public class UserMessageController {

    private final UserMessageService userMessageService;

    @GetMapping
    public ResponseEntity<List<UserMessageResponseDto>> getAllMessages() {
        List<UserMessage> messages = userMessageService.getAllMessages();

        List<UserMessageResponseDto> response = messages.stream()
                .map(UserMessageResponseDto::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        userMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

}
