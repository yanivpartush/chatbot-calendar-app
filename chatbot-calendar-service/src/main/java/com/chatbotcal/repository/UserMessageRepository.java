package com.chatbotcal.repository;

import com.chatbotcal.entity.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    Optional<UserMessage> findTopByUserIdAndChatIdAndTextMessage(
            String userId, String chatId, String textMessage);

}
