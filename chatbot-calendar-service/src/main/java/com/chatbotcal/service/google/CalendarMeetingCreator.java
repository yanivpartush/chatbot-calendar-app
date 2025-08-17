package com.chatbotcal.service.google;

import com.chatbotcal.event.CalendarEventData;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CalendarMeetingCreator {


    private static final Logger logger = LoggerFactory.getLogger(CalendarMeetingCreator.class);

    public Event createEvent(CalendarEventData eventData, String timeZone) throws Exception {

        Event eventToCreate = new Event()
                .setSummary(eventData.getTitle())
                .setLocation(eventData.getLocation());

        String description = "Created By Chatbot Calendar App";
        if (!eventData.getParticipants().isEmpty()) {
            description += "\nParicipants: " + String.join(", ", eventData.getParticipants());
        }
        eventToCreate.setDescription(description);

        DateTime startDateTime = new DateTime(eventData.getStartDateTime() + "+03:00" );
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(timeZone);

        DateTime endDateTime = new DateTime(eventData.getEndDateTime() + "+03:00" );
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(timeZone);

        eventToCreate.setStart(start);
        eventToCreate.setEnd(end);

        return eventToCreate;
    }
}
