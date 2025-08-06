package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.event.TelegramMsgEvent;
import com.chatbotcal.event.CalendarEventData;
import com.chatbotcal.service.google.CalendarMeetingCreator;
import com.chatbotcal.service.openai.CalendarPromptAIService;
import com.chatbotcal.service.UserMessageService;
import com.chatbotcal.service.UserService;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventHandler.class);

    @Override
    public void on(TelegramMsgEvent event) {

        if (isDuplicateMessage(event)) return;

        User user = userService.getOrCreateUser(
                event.getUserId(), event.getFirstName(), event.getLastName(), event.getUsername());

        UserMessage message =
                userMessageService.saveUserMessage(user, event.getChatId(), event.getText(), MessageStatus.RECEIVED);

        try {

            userMessageService.updateStatus(message.getId(), MessageStatus.IN_PROGRESS);

            String telegramJson = calendarPromptAIService.getCalendarEventFromPrompt(event.getText());
            CalendarEventData calendarEventData = calendarEventData(telegramJson);
            Event createdEvent = calendarMeetingCreator.createEvent(calendarEventData);
            logger.info(String.format("Event created in Google Calendar : %s", createdEvent.getHtmlLink()));

            userMessageService.updateStatus(message.getId(), MessageStatus.SUCCESS);
            logger.info("Message processed successfully: messageId={}, userId={}",
                        message.getId(), user.getId());

        } catch (Exception e) {
            userMessageService.updateStatus(message.getId(), MessageStatus.FAILED);
            logger.error("Failed to process messageId={}, userId={}, error={}",
                         message.getId(), user.getId(), e.getMessage(), e);
        }
    }

    private boolean isDuplicateMessage(TelegramMsgEvent event) {
        Optional<UserMessage> existingMessageOpt = userMessageService.findExistingMessage(
                event.getUserId(), event.getChatId(), event.getText());

        if (existingMessageOpt.isPresent()) {
            UserMessage existingMessage = existingMessageOpt.get();
            MessageStatus status = existingMessage.getStatus();

            if (status != MessageStatus.RECEIVED && status != MessageStatus.IN_PROGRESS) {
                logger.warn(String.format(
                        "Skipping already processed message: userId=%s, chatId=%s, status=%s, text=\"%s\"",
                        event.getUserId(), event.getChatId(), existingMessage.getStatus(), event.getText()));
                return true;
            }
        }
        return false;
    }

}
