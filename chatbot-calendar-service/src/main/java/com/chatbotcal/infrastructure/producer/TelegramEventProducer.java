package com.chatbotcal.infrastructure.producer;

import com.chatbotcal.event.TelegramEvent;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.service.UserMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserMessageService userMessageService;
    private final Counter sentCounter;
    private final Counter errorCounter;

    @Value("${spring.kafka.topic.telegram-messages}")
    private String telegramTopic;

    @Autowired
    public TelegramEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper,
                                 UserMessageService userMessageService,
                                 MeterRegistry registry) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.userMessageService = userMessageService;

        this.sentCounter = registry.counter("telegram_events_sent_total");
        this.errorCounter = registry.counter("telegram_events_send_errors_total");
    }

}