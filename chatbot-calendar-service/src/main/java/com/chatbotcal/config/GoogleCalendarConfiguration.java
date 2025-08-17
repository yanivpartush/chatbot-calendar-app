package com.chatbotcal.config;

import com.chatbotcal.service.google.CalendarService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import static com.chatbotcal.service.google.CalendarService.JSON_FACTORY;
//import static com.chatbotcal.service.google.CalendarService.TOKENS_DIRECTORY_PATH;
import static com.chatbotcal.util.JsonTemplateUtil.loadExternalResource;

@Configuration
public class GoogleCalendarConfiguration {

   /* @Bean
    public Calendar googleCalendarService() throws Exception {
        return new CalendarService().getCalendarService();
    }

    @Bean
    public GoogleAuthorizationCodeFlow flow() throws Exception {
        InputStream in = loadExternalResource("gcp-chatbot-credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        return new  GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }*/


}
