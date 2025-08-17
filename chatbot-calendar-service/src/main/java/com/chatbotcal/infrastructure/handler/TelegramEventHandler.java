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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.chatbotcal.service.google.CalendarEventDataBuilder.calendarEventData;

@Service
@RequiredArgsConstructor
public class TelegramEventHandler  {

    private final UserMessageService userMessageService;
    private final UserService userService;
    private final CalendarPromptAIService calendarPromptAIService;
    private final CalendarMeetingCreator calendarMeetingCreator;
    private final NotificationService notificationService;
    private final GoogleAuthService googleAuthService;
    private final CalendarService calendarService;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventHandler.class);


    public void on(TelegramEvent telegramEvent) {

        User user = userService.getOrCreateUser(
                telegramEvent.getUserId(),
                telegramEvent.getFirstName(),
                telegramEvent.getLastName(),
                telegramEvent.getUsername(),
                telegramEvent.getTimeZone());

        googleAuthService.getUserCredential(user.getId()).ifPresentOrElse(
                ( credential ) -> {
                    UserMessage message =
                            userMessageService.saveUserMessage(user, telegramEvent.getText(),
                                                               MessageStatus.RECEIVED);
                    try {
                        userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);
                        String telegramJson =
                                calendarPromptAIService.getCalendarEventFromPrompt(telegramEvent.getText());
                        CalendarEventData calendarEventData = calendarEventData(telegramJson);
                        Event createdEvent = calendarMeetingCreator.createEvent(calendarEventData,user.getTimeZone());
                        createdEvent = calendarService.createEvent(credential, createdEvent);
                        logger.info(String.format("Event created in Google Calendar : %s", createdEvent.getHtmlLink()));
                        userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
                        logger.info("Message processed successfully: messageId={}, userId={}",
                                    message.getId(), user.getId());
                        notificationService.notifyUserOnEventCreation(telegramEvent, createdEvent);

                    } catch (Exception e) {
                        userMessageService.updateStatus(message.getId(), MessageStatus.FAILED);
                        logger.error("Failed to process messageId={}, userId={}, error={}",
                                     message.getId(), user.getId(), e.getMessage(), e);
                        notificationService.notifyUserOnFailure(telegramEvent);
                    }
                }, () -> {
                    logger.info("User {} is not authorized, sending authorization link", user.getId());
                    notificationService.notifyUserOnAuthorizationRequired(telegramEvent,
                                                                          googleAuthService.getAuthUrl(user.getId()));
                }
        );

    }

}
