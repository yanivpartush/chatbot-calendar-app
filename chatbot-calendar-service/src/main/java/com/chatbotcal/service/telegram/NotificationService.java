package com.chatbotcal.service.telegram;

import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.service.tinyurl.TinyUrlService;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.api-url}")
    private String apiUrl;

    private final NotificationMessageService notificationMessageService;
    private final TinyUrlService tinyUrlService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void notifyUserOnFailure(TelegramEvent telegramEvent) {
        String messageText = notificationMessageService.getMessage("event.schedule.error", telegramEvent.getText());
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnUnknownIntent(TelegramEvent telegramEvent) {
        String messageText = notificationMessageService.getMessage("intent.unknown", telegramEvent.getText());
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnAuthorizationRequired(TelegramEvent telegramEvent, String link) {
        String messageText = notificationMessageService.getMessage("calendar.connect", link);
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnFutureEvents(TelegramEvent telegramEvent, String events) {
        String messageText = notificationMessageService.getMessage("schedule.upcoming", events);
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnCheckAvailability(TelegramEvent telegramEvent, String events) {
        String messageText = notificationMessageService.getMessage("check.availability", events);
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnEventCreation(TelegramEvent telegramEvent, Event createdEvent) {
        String messageText = notificationMessageService.getMessage("event.created", createdEvent.getSummary(),
                                                                   tinyUrlService.shorten(createdEvent.getHtmlLink()));
        notifyUser(telegramEvent.getUserId(), messageText);
    }
    public void notifyUserOnSuccessfulAuthorization(String userId) {
        String messageText = notificationMessageService.getMessage("auth.retry", userId);
        notifyUser(userId, messageText);
    }

    public void notifyUserOnTextError(String userId) {
        String messageText = notificationMessageService.getMessage("text.error");
        notifyUser(userId, messageText);
    }

    public void notifyUserOnVoiceError(String userId) {
        String messageText = notificationMessageService.getMessage("voice.error");
        notifyUser(userId, messageText);
    }

    public void notifyUser(String userId, String messageText) {
        try {
            String urlString = String.format(
                    apiUrl,
                    botToken, userId, URLEncoder.encode(messageText, StandardCharsets.UTF_8)
            );
            logger.info("Sending Telegram message to URL: {}", urlString);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.error("Failed to send Telegram message. Response code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Failed to notify user msg [{}]. Reason: {}", messageText, e);
        }
    }
}
