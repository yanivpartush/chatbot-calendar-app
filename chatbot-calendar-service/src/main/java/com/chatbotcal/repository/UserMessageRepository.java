package com.chatbotcal.repository;

import com.chatbotcal.repository.entity.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    public Optional<UserMessage> findTopByUserIdAndChatIdAndTextMessage(
            String userId, String chatId, String textMessage);

    public List<UserMessage> findByUserId(String userId);

}
