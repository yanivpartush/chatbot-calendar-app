package com.chatbotcal.repository;

import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {


    public List<UserMessage> findByUserId(String userId);

    List<UserMessage> findByUserIdAndStatus(String state, MessageStatus received);

    int countByStatus(MessageStatus status);
}
