package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.event.CalendarEventData;
import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.event.TextEvent;
import com.chatbotcal.event.VoiceEvent;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.service.UserMessageService;
import com.chatbotcal.service.UserService;
import com.chatbotcal.service.google.CalendarMeetingCreator;
import com.chatbotcal.service.google.CalendarService;
import com.chatbotcal.service.google.GoogleAuthService;
import com.chatbotcal.service.openai.CalendarPromptAIService;
import com.chatbotcal.service.openai.VoiceToTextConvertor;
import com.chatbotcal.service.telegram.NotificationService;
import com.google.api.services.calendar.model.Event;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.chatbotcal.service.google.CalendarEventDataBuilder.extractCalendarEventData;

@Service
public class TelegramEventHandler {

    private final UserMessageService userMessageService;
    private final UserService userService;
    private final CalendarPromptAIService calendarPromptAIService;
    private final CalendarMeetingCreator calendarMeetingCreator;
    private final NotificationService notificationService;
    private final GoogleAuthService googleAuthService;
    private final CalendarService calendarService;
    private final MeterRegistry meterRegistry;
    private final VoiceToTextConvertor voiceToTextConvertor;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventHandler.class);

    private final Counter successCounter;
    private final Counter failedCounter;
    private final Counter unauthorizedCounter;

    @Autowired
    public TelegramEventHandler(UserMessageService userMessageService,
                                UserService userService,
                                CalendarPromptAIService calendarPromptAIService,
                                CalendarMeetingCreator calendarMeetingCreator,
                                NotificationService notificationService,
                                GoogleAuthService googleAuthService,
                                CalendarService calendarService,
                                MeterRegistry meterRegistry, VoiceToTextConvertor voiceToTextConvertor) {
        this.userMessageService = userMessageService;
        this.userService = userService;
        this.calendarPromptAIService = calendarPromptAIService;
        this.calendarMeetingCreator = calendarMeetingCreator;
        this.notificationService = notificationService;
        this.googleAuthService = googleAuthService;
        this.calendarService = calendarService;
        this.meterRegistry = meterRegistry;

        this.successCounter = Counter.builder("telegram.events.status")
                .description("Number of successful processed messages")
                .tag("status", "SUCCESS")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("telegram.events.status")
                .description("Number of failed processed messages")
                .tag("status", "FAILED")
                .register(meterRegistry);

        this.unauthorizedCounter = Counter.builder("telegram.events.status")
                .description("Number of unauthorized users")
                .tag("status", "UNAUTHORIZED")
                .register(meterRegistry);
        this.voiceToTextConvertor = voiceToTextConvertor;
    }

    public void on(VoiceEvent voiceEvent) {
        try {
            String transcript = voiceToTextConvertor.convertBase64VoiceToText(voiceEvent);
            logger.info("voice message transcript : {}", transcript);
            voiceEvent.setTranscript(transcript);
            UserMessage message = saveUserAndMessage(voiceEvent);
            processMessage(voiceEvent, message);

        } catch (IOException e) {
            logger.info("Failed to convert voice message to text -> {}", e);
            notificationService.notifyUser(
                    voiceEvent.getUserId(), "Failed to process your voice message. Please try again.");
        }
    }

    @Timed(value = "telegram.events.processing.duration", description = "Time taken to process TelegramEvent")
    @Counted(value = "telegram.events.processing.count", description = "Number of TelegramEvent processed")
    public void on(TextEvent textEvent) {
        UserMessage message = saveUserAndMessage(textEvent);
        processMessage(textEvent, message);
    }

    private void processMessage(TelegramEvent telegramEvent, UserMessage message) {
        googleAuthService.getUserCredential(message.getUser().getId()).ifPresentOrElse(
                (credential) -> {
                    try {
                        userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);

                        String telegramJson =
                                calendarPromptAIService.getCalendarEventFromPrompt(telegramEvent.getText(),
                                                                                   telegramEvent.getTimeZone());
                        CalendarEventData calendarEventData = extractCalendarEventData(telegramJson);

                        Event createdEvent =
                                calendarMeetingCreator.createEvent(calendarEventData, telegramEvent.getTimeZone());
                        createdEvent = calendarService.createEvent(credential, createdEvent);

                        userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
                        successCounter.increment();

                        logger.info("Message processed successfully : messageId={}, userId={}",
                                    message.getId(), telegramEvent.getUserId());
                        notificationService.notifyUserOnEventCreation(telegramEvent, createdEvent);

                    } catch (Exception e) {
                        userMessageService.updateStatus(message.getId(), MessageStatus.FAILED);
                        failedCounter.increment();

                        logger.error("Failed to process messageId={}, userId={}, error={}",
                                     message.getId(), telegramEvent.getUserId(), e.getMessage(), e);
                        notificationService.notifyUserOnFailure(telegramEvent);
                    }
                }, () -> {
                    unauthorizedCounter.increment();
                    logger.info("User {} is not authorized, sending authorization link", telegramEvent.getUserId());
                    notificationService.notifyUserOnAuthorizationRequired(
                            telegramEvent, googleAuthService.getAuthUrl(telegramEvent.getUserId()));
                }

        );
    }

    private UserMessage saveUserAndMessage(TelegramEvent telegramEvent) {
        User user = userService.getOrCreateUser(
                telegramEvent.getUserId(),
                telegramEvent.getFirstName(),
                telegramEvent.getLastName(),
                telegramEvent.getUsername(),
                telegramEvent.getTimeZone());
        return userMessageService.saveUserMessage(user, telegramEvent);
    }
}