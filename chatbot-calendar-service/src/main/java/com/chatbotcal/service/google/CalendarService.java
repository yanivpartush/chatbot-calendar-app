package com.chatbotcal.service.google;

import com.chatbotcal.event.CalendarEventData;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String APPLICATION_NAME = "Chatbot calendar";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);
    private static final String CALENDAR_ID = "primary";

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
        Event createdEvent = client.events().insert(CALENDAR_ID, event).execute();
        logger.info(String.format("Event created in Google Calendar : %s", createdEvent.getHtmlLink()));
        return createdEvent;
    }

    public List<CalendarEventData> getEventsForNextWeek(Credential credential, String timeZone) throws
            Exception {
        Calendar client = buildClient(credential);
        LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
        LocalDateTime weekLater = now.plusDays(7);

        Events events = client.events().list(CALENDAR_ID)
                .setTimeMin(new com.google.api.client.util.DateTime(
                        java.util.Date.from(now.atZone(ZoneId.of(timeZone)).toInstant())))
                .setTimeMax(new com.google.api.client.util.DateTime(
                        java.util.Date.from(weekLater.atZone(ZoneId.of(timeZone)).toInstant())))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<CalendarEventData> result = new ArrayList<>();

        for (Event event : events.getItems()) {
            String start = null;
            String end = null;

            if (event.getStart().getDateTime() != null) {

                start = event.getStart().getDateTime().toStringRfc3339();
                end = event.getEnd().getDateTime().toStringRfc3339();
            } else if (event.getStart().getDate() != null) {
              // All-day event
                start = event.getStart().getDate().toStringRfc3339();
                end = event.getEnd().getDate().toStringRfc3339();
            }


            List<String> participants = new ArrayList<>();
            if (event.getAttendees() != null) {
                for (EventAttendee attendee : event.getAttendees()) {
                    participants.add(attendee.getEmail());
                }
            }

            result.add(new CalendarEventData(
                    event.getSummary(),
                    event.getLocation() != null ? event.getLocation() : "",
                    start,
                    end,
                    participants
            ));
        }
        logger.info(String.format("Next week events : %s", result));
        return result;
    }

}
