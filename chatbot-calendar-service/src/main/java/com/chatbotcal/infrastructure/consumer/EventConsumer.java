package com.chatbotcal.infrastructure.consumer;

import org.springframework.kafka.support.Acknowledgment;

public interface EventConsumer {

    public void consume(String message,Acknowledgment ack);

}
