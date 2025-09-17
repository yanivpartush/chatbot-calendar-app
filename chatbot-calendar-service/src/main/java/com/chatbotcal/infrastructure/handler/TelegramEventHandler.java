package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.event.CalendarEventData;
import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.event.TextEvent;
import com.chatbotcal.event.VoiceEvent;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.repository.enums.UserIntent;
import com.chatbotcal.service.UserMessageService;
import com.chatbotcal.service.UserService;
import com.chatbotcal.service.google.CalendarMeetingCreator;
import com.chatbotcal.service.google.CalendarService;
import com.chatbotcal.service.google.GoogleAuthService;
import com.chatbotcal.service.openai.CalendarPromptAIService;
import com.chatbotcal.service.openai.IntentClassificationService;
import com.chatbotcal.service.openai.VoiceToTextConvertor;
import com.chatbotcal.service.telegram.NotificationService;
import com.chatbotcal.service.tinyurl.TinyUrlService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.Event;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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
    private final IntentClassificationService intentClassificationService;
    private final TinyUrlService tinyUrlService;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventHandler.class);


    private final Counter successCounter;
    private final Counter failedCounter;
    private final Counter unauthorizedCounter;


    private final Gauge inProgressGauge;


    private final DistributionSummary messageLengthSummary;


    private final Timer scheduleMeetingTimer;
    private final Timer checkAvailabilityTimer;

    @Autowired
    public TelegramEventHandler(UserMessageService userMessageService,
                                UserService userService,
                                CalendarPromptAIService calendarPromptAIService,
                                CalendarMeetingCreator calendarMeetingCreator,
                                NotificationService notificationService,
                                GoogleAuthService googleAuthService,
                                CalendarService calendarService,
                                MeterRegistry meterRegistry,
                                VoiceToTextConvertor voiceToTextConvertor,
                                IntentClassificationService intentClassificationService,
                                TinyUrlService tinyUrlService) {
        this.userMessageService = userMessageService;
        this.userService = userService;
        this.calendarPromptAIService = calendarPromptAIService;
        this.calendarMeetingCreator = calendarMeetingCreator;
        this.notificationService = notificationService;
        this.googleAuthService = googleAuthService;
        this.calendarService = calendarService;
        this.meterRegistry = meterRegistry;

        this.voiceToTextConvertor = voiceToTextConvertor;
        this.intentClassificationService = intentClassificationService;
        this.tinyUrlService = tinyUrlService;

        this.successCounter = Counter.builder("telegram.events.total")
                .description("Number of processed messages by status")
                .tag("status", "SUCCESS")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("telegram.events.total")
                .description("Number of processed messages by status")
                .tag("status", "FAILED")
                .register(meterRegistry);

        this.unauthorizedCounter = Counter.builder("telegram.events.total")
                .description("Number of processed messages by status")
                .tag("status", "UNAUTHORIZED")
                .register(meterRegistry);

        this.inProgressGauge = Gauge.builder("telegram.events.in_progress", userMessageService,
                                             UserMessageService::countMessagesInProgress)
                .description("Number of messages in processing")
                .register(meterRegistry);

        this.messageLengthSummary = DistributionSummary.builder("telegram.message.length")
                .description("Distribution of message lengths")
                .baseUnit("chars")
                .register(meterRegistry);

        this.scheduleMeetingTimer = Timer.builder("telegram.schedule.meeting.duration")
                .description("Time to schedule a meeting")
                .register(meterRegistry);

        this.checkAvailabilityTimer = Timer.builder("telegram.check.availability.duration")
                .description("Time to check availability")
                .register(meterRegistry);
    }

    @Timed(value = "telegram.events.voice.processing.duration", description = "Time taken to process VoiceEvent")
    @Counted(value = "telegram.events.voice.processing.count", description = "Number of VoiceEvent processed")
    public void on(VoiceEvent voiceEvent) throws Exception {
        try {
            String transcript = voiceToTextConvertor.convertBase64VoiceToText(voiceEvent);
            logger.info("voice message transcript : {}", transcript);
            voiceEvent.setTranscript(transcript);
            UserIntent userIntent = intentClassificationService.classifyMessage(transcript);
            UserMessage message = saveUserAndMessage(voiceEvent, userIntent);
            dispatch(voiceEvent, message);
            messageLengthSummary.record(transcript.length());
        } catch (IOException e) {
            logger.error("Failed to convert voice message to text -> {}", e);
            notificationService.notifyUserOnVoiceError(voiceEvent.getUserId());
        }
    }

    @Timed(value = "telegram.events.processing.duration", description = "Time taken to process TextEvent")
    @Counted(value = "telegram.events.processing.count", description = "Number of TextEvent processed")
    public void on(TextEvent textEvent) throws Exception {

        if ("__DUMMY_AUTH_CHECK__".equals(textEvent.getText())) {
            userService.getOrCreateUser(textEvent.getUserId(),textEvent.getFirstName()
                                                ,textEvent.getLastName()
                                                ,textEvent.getUsername(),textEvent.getTimeZone());
            handleDummyAuthCheck(textEvent);
            return;
        }

        try {
            UserIntent userIntent = intentClassificationService.classifyMessage(textEvent.getText());
            UserMessage message = saveUserAndMessage(textEvent, userIntent);
            dispatch(textEvent, message);
            messageLengthSummary.record(textEvent.getText().length());
        } catch (IOException e) {
            logger.error("Failed to process text event message -> {}", e);
            notificationService.notifyUserOnTextError(textEvent.getUserId());
        }
    }

    private void handleDummyAuthCheck(TextEvent textEvent) {
        boolean hasCalendarPermission = calendarService.checkAuthorization(textEvent.getUserId());
        if (hasCalendarPermission) {
            logger.info("User {} already has calendar permission", textEvent.getUserId());
        }
        else
        {
            String authUrl = tinyUrlService.shorten(googleAuthService.getAuthUrl(textEvent.getUserId()));
            logger.info("User {} is not authorized, sending authorization link {}", textEvent.getUserId(), authUrl);
            notificationService.notifyUserOnFirstTimeConnection(textEvent, authUrl);
        }
    }


    public void dispatch(TelegramEvent telegramEvent, UserMessage message) throws Exception {
        googleAuthService.getAndUpdateUserCredential(message.getUser().getId()).ifPresentOrElse(
                credential -> {
                    try {
                        switch (message.getUserIntent()) {
                            case SCHEDULE_MEETING -> scheduleMeetingTimer.record(() -> scheduleMeeting(credential, telegramEvent, message));
                            case GET_WEEKLY_SCHEDULE -> showWeeklySchedule(credential, telegramEvent, message);
                            case CHECK_AVAILABILITY -> checkAvailabilityTimer.record(() -> {
                                try {
                                    checkAvailability(credential, telegramEvent, message);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            case UNKNOWN -> handleUnknown(telegramEvent, message);
                        }
                    } catch (Exception e) {
                        handleFailure(telegramEvent, message, e);
                    }
                },
                () -> handleUnauthorizedUser(telegramEvent, message)
        );
    }

    private void handleUnknown(TelegramEvent telegramEvent, UserMessage message) {
        userMessageService.updateStatus(message.getId(), MessageStatus.UNKNOWN_USER_INTENT);
        logger.info("Could not understand intent of user {}. Message: '{}'",
                    telegramEvent.getUserId(), telegramEvent.getText());
        notificationService.notifyUserOnUnknownIntent(telegramEvent);
    }

    private void handleUnauthorizedUser(TelegramEvent telegramEvent, UserMessage message) {
        unauthorizedCounter.increment();
        String authUrl = tinyUrlService.shorten(googleAuthService.getAuthUrl(message.getUser().getId()));
        logger.info("User {} is not authorized, sending authorization link {}", message.getUser().getId(), authUrl);
        notificationService.notifyUserOnAuthorizationRequired(telegramEvent, authUrl);
    }

    private void handleFailure(TelegramEvent telegramEvent, UserMessage message, Exception e) {
        userMessageService.updateStatus(message.getId(), MessageStatus.FAILED);
        failedCounter.increment();
        logger.error("Failed to process Message={}, userId={}, error={}", message.getId(), telegramEvent.getUserId(), e.getMessage(), e);
        notificationService.notifyUserOnFailure(telegramEvent);
    }

    private void checkAvailability(Credential credential, TelegramEvent telegramEvent, UserMessage message) throws
            Exception {
        userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);
        List<CalendarEventData> futureEvents = calendarService.getEventsForNextWeek(credential, telegramEvent.getTimeZone());
        String response = calendarPromptAIService.executeCheckAvailabilityPrompt(telegramEvent.getText(), telegramEvent.getTimeZone(), futureEvents);
        userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
        successCounter.increment();
        logger.info("Message processed successfully : messageId={}, userId={}", message.getId(), telegramEvent.getUserId());
        notificationService.notifyUserOnCheckAvailability(telegramEvent, response);
    }

    private void showWeeklySchedule(Credential credential, TelegramEvent telegramEvent, UserMessage message) throws Exception {
        userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);
        List<CalendarEventData> futureEvents = calendarService.getEventsForNextWeek(credential, telegramEvent.getTimeZone());
        String response = calendarPromptAIService.executeFutureEventPrompt(telegramEvent.getText(), telegramEvent.getTimeZone(), futureEvents);
        userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
        successCounter.increment();
        logger.info("Message processed successfully : messageId={}, userId={}", message.getId(), telegramEvent.getUserId());
        notificationService.notifyUserOnFutureEvents(telegramEvent, response);
    }

    private void scheduleMeeting(Credential credential, TelegramEvent telegramEvent, UserMessage message) {
        userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);
        try {
            String meetingDetailsJson = calendarPromptAIService.buildCalendarEventPrompt(telegramEvent.getText(), telegramEvent.getTimeZone());
            CalendarEventData calendarEventData = extractCalendarEventData(meetingDetailsJson);
            Event createdEvent = calendarMeetingCreator.createEvent(calendarEventData, telegramEvent.getTimeZone());
            createdEvent = calendarService.createEvent(credential, createdEvent);
            userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
            successCounter.increment();
            logger.info("Message processed successfully : messageId={}, userId={}", message.getId(), telegramEvent.getUserId());
            notificationService.notifyUserOnEventCreation(telegramEvent, createdEvent);
        } catch (Exception e) {
            handleFailure(telegramEvent, message, e);
        }
    }

    private UserMessage saveUserAndMessage(TelegramEvent telegramEvent, UserIntent userIntent) {
        User user = userService.getOrCreateUser(
                telegramEvent.getUserId(),
                telegramEvent.getFirstName(),
                telegramEvent.getLastName(),
                telegramEvent.getUsername(),
                telegramEvent.getTimeZone()
        );
        return userMessageService.saveUserMessage(user, telegramEvent, userIntent);
    }


}