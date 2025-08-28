package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.event.CalendarEventData;
import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.service.UserMessageService;
import com.chatbotcal.service.UserService;
import com.chatbotcal.service.google.CalendarMeetingCreator;
import com.chatbotcal.service.google.CalendarService;
import com.chatbotcal.service.google.GoogleAuthService;
import com.chatbotcal.service.openai.CalendarPromptAIService;
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
                                MeterRegistry meterRegistry) {
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
    }

    @Timed(value = "telegram.events.processing.duration", description = "Time taken to process TelegramEvent")
    @Counted(value = "telegram.events.processing.count", description = "Number of TelegramEvent processed")
    public void on(TelegramEvent telegramEvent) {

        User user = userService.getOrCreateUser(
                telegramEvent.getUserId(),
                telegramEvent.getFirstName(),
                telegramEvent.getLastName(),
                telegramEvent.getUsername(),
                telegramEvent.getTimeZone());

        UserMessage message =
                userMessageService.saveUserMessage(user, telegramEvent.getText(), MessageStatus.RECEIVED);

        googleAuthService.getUserCredential(user.getId()).ifPresentOrElse(
                (credential) -> {
                    try {
                        userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);

                        String telegramJson =
                                calendarPromptAIService.getCalendarEventFromPrompt(telegramEvent.getText(), user.getTimeZone());
                        CalendarEventData calendarEventData = extractCalendarEventData(telegramJson);

                        Event createdEvent = calendarMeetingCreator.createEvent(calendarEventData, user.getTimeZone());
                        createdEvent = calendarService.createEvent(credential, createdEvent);

                        userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
                        successCounter.increment();

                        logger.info("Message processed successfully : messageId={}, userId={}",
                                    message.getId(), user.getId());
                        notificationService.notifyUserOnEventCreation(telegramEvent, createdEvent);

                    } catch (Exception e) {
                        userMessageService.updateStatus(message.getId(), MessageStatus.FAILED);
                        failedCounter.increment();

                        logger.error("Failed to process messageId={}, userId={}, error={}",
                                     message.getId(), user.getId(), e.getMessage(), e);
                        notificationService.notifyUserOnFailure(telegramEvent);
                    }
                }, () -> {
                    unauthorizedCounter.increment();
                    logger.info("User {} is not authorized, sending authorization link", user.getId());
                    notificationService.notifyUserOnAuthorizationRequired(
                            telegramEvent, googleAuthService.getAuthUrl(user.getId()));
                }
        );
    }
}