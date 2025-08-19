package com.chatbotcal.controller;

import com.chatbotcal.infrastructure.producer.TelegramEventProducer;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.service.UserMessageService;
import com.chatbotcal.service.google.GoogleAuthService;
import com.chatbotcal.service.telegram.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OAuthCallbackController {

    private final GoogleAuthService authService;
    private final NotificationService notificationService;
    private final UserMessageService userMessageService;
    private final TelegramEventProducer telegramEventProducer;


    @GetMapping("/auth/callback")
    public String callback(@RequestParam String code, @RequestParam String state) throws Exception {
        authService.exchangeCode(state, code);

        List<UserMessage> receivedMessages = userMessageService.findMessagesByStatus(state, MessageStatus.RECEIVED);

        String message =
                String.format("Authorization successful for user %s. retrying your pending messages...", state);
        notificationService.notifyUser(state, message);

        telegramEventProducer.produce(receivedMessages);

        return "Authorization successful for user " + state;
    }

}
