package com.chatbotcal.service.telegram;

import com.chatbotcal.event.TelegramEvent;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class NotificationService {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.api-url}")
    private String apiUrl;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void notifyUserOnFailure(TelegramEvent telegramEvent) {
        String messageText = String.format(
                "Your calendar event has been failed\n\nTitle: %s",
                telegramEvent.getText()
        );
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnAuthorizationRequired(TelegramEvent telegramEvent, String link) {
        String messageText = String.format(
                "Message was not processed, Click the link to connect your Google Calendar : %s",
                link
        );
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUserOnEventCreation(TelegramEvent telegramEvent, Event createdEvent) {
        String messageText = String.format(
                "Your calendar event has been successfully created!\n\nTitle: %s\nView in Calendar: %s",
                createdEvent.getSummary(),
                createdEvent.getHtmlLink()
        );
        notifyUser(telegramEvent.getUserId(), messageText);
    }

    public void notifyUser(String userId, String messageText) {
        //String userId = telegramEvent.getUserId();

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
