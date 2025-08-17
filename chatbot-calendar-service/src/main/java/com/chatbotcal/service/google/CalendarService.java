package com.chatbotcal.service.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String APPLICATION_NAME = "Chatbot calendar";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    private Calendar buildClient(Credential credential) throws Exception {
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

    }

    public Event createEvent(Credential credential, Event event) throws Exception {
        Calendar client = buildClient(credential);
        return client.events().insert("primary", event).execute();
    }


}
