package com.chatbotcal.infrastructure.producer;

import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.service.UserMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventProducer.class);

    private final KafkaTemplate kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserMessageService userMessageService;

    @Value("${spring.kafka.topic.telegram-messages}")
    private String telegramTopic;



    public void produce(List<UserMessage> messages) {
        messages.forEach(receivedMessage -> {
            try {
                TelegramEvent event = TelegramEvent.fromUserMessage(receivedMessage);
                String jsonEvent = objectMapper.writeValueAsString(event);
                logger.info("Sending message to Kafka: {}", jsonEvent);
                kafkaTemplate.send(telegramTopic, jsonEvent);
                userMessageService.updateStatus(receivedMessage.getId(), MessageStatus.RETRY);
                logger.info("Message marked in RETRY Status : messageId={}, userId={}",
                            receivedMessage.getId(), receivedMessage.getUser().getId());
            } catch (JsonProcessingException e) {
                logger.error("Failed to republish telegram message to kafka ", e);
            }
        });
    }

}
