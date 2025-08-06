package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.event.CalendarEventData;
import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.service.UserMessageService;
import com.chatbotcal.service.UserService;
import com.chatbotcal.service.google.CalendarMeetingCreator;
import com.chatbotcal.service.openai.CalendarPromptAIService;
import com.chatbotcal.service.telegram.NotificationService;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.chatbotcal.service.google.CalendarEventDataBuilder.calendarEventData;

@Service
public class TelegramEventHandler implements EventHandler {

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarPromptAIService calendarPromptAIService;

    @Autowired
    private CalendarMeetingCreator calendarMeetingCreator;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventHandler.class);

    @Override
    public void on(TelegramEvent telegramEvent) {

        User user = userService.getOrCreateUser(
                telegramEvent.getUserId(), telegramEvent.getFirstName(), telegramEvent.getLastName(),
                telegramEvent.getUsername());

        UserMessage message =
                userMessageService.saveUserMessage(user, telegramEvent.getChatId(), telegramEvent.getText(),
                                                   MessageStatus.RECEIVED);

        try {

            userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);
            String telegramJson = calendarPromptAIService.getCalendarEventFromPrompt(telegramEvent.getText());
            CalendarEventData calendarEventData = calendarEventData(telegramJson);
            Event createdEvent = calendarMeetingCreator.createEvent(calendarEventData);
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
    }

}
