package com.chatbotcal.service.google;

import com.chatbotcal.event.CalendarEventData;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class CalendarMeetingCreator {


    private static final Logger logger = LoggerFactory.getLogger(CalendarMeetingCreator.class);

    public Event createEvent(CalendarEventData eventData, String timeZone) throws Exception {

        Event eventToCreate = new Event()
                .setSummary(eventData.getTitle())
                .setLocation(eventData.getLocation());

        String description = "Created By Chatbot Calendar App";
        if (!eventData.getParticipants().isEmpty()) {
            description += "\nParticipants: " + String.join(", ", eventData.getParticipants());
        }
        eventToCreate.setDescription(description);

        LocalDateTime localStart = LocalDateTime.parse(eventData.getStartDateTime());
        ZonedDateTime zonedStart = localStart.atZone(ZoneId.of(timeZone));
        DateTime startDateTime = new DateTime(zonedStart.toInstant().toEpochMilli());

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(timeZone);

        LocalDateTime localEnd = LocalDateTime.parse(eventData.getEndDateTime());
        ZonedDateTime zonedEnd = localEnd.atZone(ZoneId.of(timeZone));
        DateTime endDateTime = new DateTime(zonedEnd.toInstant().toEpochMilli());

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(timeZone);

        eventToCreate.setStart(start);
        eventToCreate.setEnd(end);

        return eventToCreate;
    }
}
