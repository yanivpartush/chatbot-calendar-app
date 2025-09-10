package com.chatbotcal.infrastructure.consumer;

import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.infrastructure.handler.TelegramEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class TelegramEventConsumer {

    private final TelegramEventHandler eventHandler;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventConsumer.class);

    private final Counter receivedCounter;
    private final Counter errorCounter;

    @Autowired
    public TelegramEventConsumer(TelegramEventHandler eventHandler,
                                 ObjectMapper objectMapper,
                                 MeterRegistry meterRegistry) {
        this.eventHandler = eventHandler;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;

        this.receivedCounter = Counter.builder("telegram.events.consumer.total")
                .description("Telegram events received and processed by consumer")
                .tag("status", "RECEIVED")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("telegram.events.consumer.total")
                .description("Telegram events errors during processing")
                .tag("status", "ERROR")
                .register(meterRegistry);
    }

    @KafkaListener(topics = "${spring.kafka.topic.telegram-messages}", groupId = "${spring.kafka.consumer.group-id}")
    @Timed(value = "telegram.events.consumer.processing.duration", description = "Time taken to process TelegramEvent in consumer")
    @Counted(value = "telegram.events.consumer.processing.count", description = "Number of TelegramEvents processed in consumer")
    public void consume(String message, Acknowledgment ack) {
        receivedCounter.increment();
        try {
            logger.info("Received message from Kafka");
            TelegramEvent event = objectMapper.readValue(message, TelegramEvent.class);
            event.dispatch(eventHandler);
        } catch (Exception e) {
            logger.error("Error while processing message: {}", e.getMessage(), e);
            errorCounter.increment();
        } finally {
            ack.acknowledge();
        }
    }
}