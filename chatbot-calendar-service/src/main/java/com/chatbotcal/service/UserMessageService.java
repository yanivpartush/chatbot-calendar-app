package com.chatbotcal.service;

import com.chatbotcal.repository.UserMessageRepository;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMessageService {

    private final UserMessageRepository userMessageRepository;

    @Transactional
    public UserMessage saveUserMessage(User user, String textMessage, MessageStatus status) {

        UserMessage userMessage = UserMessage.builder()
                                             .user(user)
                                             .textMessage(textMessage)
                                             .status(status)
                                             .build();

        return userMessageRepository.save(userMessage);
    }

    @Transactional
    public UserMessage updateStatus(Long messageId, MessageStatus newStatus) {
        UserMessage message = userMessageRepository.findById(messageId)
                                                   .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        message.setStatus(newStatus);
        return message;
    }


    public List<UserMessage> getAllMessages() {
        return userMessageRepository.findAll();
    }

    public void deleteMessage(Long id) {
        userMessageRepository.deleteById(id);
    }


}

