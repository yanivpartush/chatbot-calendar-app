package com.chatbotcal.service;

import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.repository.UserMessageRepository;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserMessageService {

    private UserMessageRepository userMessageRepository;
    private MeterRegistry meterRegistry;

    private Counter messagesDeletedCounter;
    private Counter messagesErrorCounter;

    private static final Logger logger = LoggerFactory.getLogger(UserMessageService.class);

    @Autowired
    public UserMessageService(UserMessageRepository userMessageRepository, MeterRegistry meterRegistry) {
        this.userMessageRepository = userMessageRepository;
        this.meterRegistry = meterRegistry;

        this.messagesDeletedCounter = Counter.builder("chatbot.messages.deleted")
                .description("Number of messages deleted")
                .register(meterRegistry);

        this.messagesErrorCounter = Counter.builder("chatbot.messages.errors")
                .description("Number of errors in UserMessageService")
                .register(meterRegistry);
    }

    @Timed(value = "chatbot.messages.save.duration", description = "Time taken to save a message")
    @Counted(value = "chatbot.messages.save.count", description = "Number of calls to saveUserMessage")
    public UserMessage saveUserMessage(User user, TelegramEvent telegramEvent) {
        UserMessage message = telegramEvent.toUserMessage(user, MessageStatus.RECEIVED);
        return userMessageRepository.save(message);
    }


    @Timed(value = "chatbot.messages.update.duration", description = "Time taken to update message status")
    @Counted(value = "chatbot.messages.update.count", description = "Number of calls to updateStatus")
    @Transactional
    public UserMessage updateStatus(Long messageId, MessageStatus newStatus) {
        try {
            UserMessage message = userMessageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

            message.setStatus(newStatus);

            Counter.builder("chatbot.messages.status.updated")
                    .description("Number of messages updated by status")
                    .tag("status", newStatus.name())
                    .register(meterRegistry)
                    .increment();

            return message;
        } catch (Exception e) {
            messagesErrorCounter.increment();
            throw e;
        }
    }

    @Timed(value = "chatbot.messages.fetch.duration", description = "Time taken to fetch all messages")
    @Counted(value = "chatbot.messages.fetch.count", description = "Number of calls to getAllMessages")
    public List<UserMessage> getAllMessages() {
        try {
            return userMessageRepository.findAll();
        } catch (Exception e) {
            messagesErrorCounter.increment();
            throw e;
        }
    }

    @Counted(value = "chatbot.messages.delete.count", description = "Number of deleteMessage calls")
    public void deleteMessage(Long id) {
        try {
            userMessageRepository.deleteById(id);
            messagesDeletedCounter.increment();
        } catch (Exception e) {
            messagesErrorCounter.increment();
            throw e;
        }
    }

    @Timed(value = "chatbot.messages.fetch.duration", description = "Time taken to fetch messages by status")
    @Counted(value = "chatbot.messages.fetch.count", description = "Number of calls to findMessagesByStatus")
    public List<UserMessage> findMessagesByStatus(String userId, MessageStatus status) {
        try {
            return userMessageRepository.findByUserIdAndStatus(userId, status);
        } catch (Exception e) {
            messagesErrorCounter.increment();
            throw e;
        }
    }
}
