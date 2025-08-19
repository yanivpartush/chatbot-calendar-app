package com.chatbotcal.infrastructure.consumer;

import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.infrastructure.handler.TelegramEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramEventConsumer {

    private final TelegramEventHandler eventHandler;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventConsumer.class);

    @KafkaListener(topics = "${spring.kafka.topic.telegram-messages}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message, Acknowledgment ack) {
        try {
            logger.info("Received message from Kafka: {}", message);
            TelegramEvent event = objectMapper.readValue(message, TelegramEvent.class);
            this.eventHandler.on(event);
        } catch (Exception e) {
            logger.error("Error while processing message: ", e);
        } finally {
            ack.acknowledge();
        }
    }

}
