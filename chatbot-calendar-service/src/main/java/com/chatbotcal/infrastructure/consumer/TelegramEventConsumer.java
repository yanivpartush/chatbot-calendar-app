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
                                 MeterRegistry registry) {
        this.eventHandler = eventHandler;
        this.objectMapper = objectMapper;
        this.meterRegistry = registry;

        this.receivedCounter = registry.counter("telegram_events_received_total");
        this.errorCounter = registry.counter("telegram_events_errors_total");
    }

    @KafkaListener(topics = "${spring.kafka.topic.telegram-messages}", groupId = "${spring.kafka.consumer.group-id}")
    @Timed(value = "telegram_event_processing_duration", description = "Time taken to process TelegramEvent")
    @Counted(value = "telegram_event_processing_total", description = "Number of processed TelegramEvents")
    public void consume(String message, Acknowledgment ack) {
        receivedCounter.increment();
        try {
            logger.info("Received message from Kafka");
            TelegramEvent event = objectMapper.readValue(message, TelegramEvent.class);
            event.dispatch(eventHandler);
        }
        catch (Exception e)
        {
            logger.error("Error while processing message: ", e);
            errorCounter.increment();
        }
        finally {
            ack.acknowledge();
        }
    }
}