package com.chatbotcal;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ChatbotCalendarServiceApplication {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;


    public static void main(String[] args) {
        SpringApplication.run(ChatbotCalendarServiceApplication.class, args);
    }

}
