package com.chatbotcal.controller;


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



    @GetMapping("/auth/callback")
    public String callback(@RequestParam String code, @RequestParam String state) throws Exception {
        authService.exchangeCode(state, code);
        notificationService.notifyUserOnSuccessfulAuthorization(state);
        return "Authorization successful for user " + state;
    }

}
