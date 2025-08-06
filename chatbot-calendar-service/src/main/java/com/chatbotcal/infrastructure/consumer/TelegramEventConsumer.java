package com.chatbotcal.infrastructure.consumer;

import com.chatbotcal.event.TelegramMsgEvent;
import com.chatbotcal.infrastructure.handler.EventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class TelegramEventConsumer implements EventConsumer {

    @Autowired
    private EventHandler eventHandler;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventConsumer.class);

    @Override
    @KafkaListener(topics = "telegram-messages", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message, Acknowledgment ack) { //
        try {
            logger.info("Received message from Kafka: {}", message);
            TelegramMsgEvent event = new ObjectMapper().readValue(message, TelegramMsgEvent.class);
            this.eventHandler.on(event);
        } catch (Exception e) {
            logger.error("Error while processing message: {}", e);
        } finally {
            ack.acknowledge();
        }
    }

}
