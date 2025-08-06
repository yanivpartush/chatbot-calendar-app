package com.chatbotcal.service;

import com.chatbotcal.entity.User;
import com.chatbotcal.entity.UserMessage;
import com.chatbotcal.enums.MessageStatus;
import com.chatbotcal.repository.UserMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMessageService {

    private final UserMessageRepository userMessageRepository;

    @Transactional
    public UserMessage saveUserMessage(User user, String chatId, String textMessage, MessageStatus status) {

        UserMessage userMessage = UserMessage.builder()
                                             .user(user).chatId(chatId)
                                             .textMessage(textMessage)
                .status(status)
                                             .build();

        return userMessageRepository.save(userMessage);
    }

    @Transactional
    public void updateStatus(Long messageId, MessageStatus newStatus) {
        UserMessage message = userMessageRepository.findById(messageId)
                                                   .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        message.setStatus(newStatus);
    }

    public Optional<UserMessage> findExistingMessage(String userId, String chatId, String messageText) {
        return userMessageRepository
                .findTopByUserIdAndChatIdAndTextMessage(userId, chatId, messageText);
    }



}

