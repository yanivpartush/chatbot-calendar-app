package com.chatbotcal.controller;

import com.chatbotcal.service.google.GoogleAuthService;
import com.chatbotcal.service.telegram.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuthCallbackController {

    private final GoogleAuthService authService;
    private final NotificationService notificationService;

    @GetMapping("/auth/callback")
    public String callback(@RequestParam String code, @RequestParam String state) throws Exception {
        authService.exchangeCode(state, code);
        String message = String.format("Authorization successful for user %s. Please send the event again. ", state);
        notificationService.notifyUser(state, message);
        return "Authorization successful for user " + state;
    }

}
