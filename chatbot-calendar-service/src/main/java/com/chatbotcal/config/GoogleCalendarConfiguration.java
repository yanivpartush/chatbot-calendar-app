package com.chatbotcal.config;

import com.chatbotcal.service.google.CalendarService;
import com.google.api.services.calendar.Calendar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCalendarConfiguration {

    @Bean
    public Calendar googleCalendarService() throws Exception {
        return new CalendarService().getCalendarService();
    }
}
