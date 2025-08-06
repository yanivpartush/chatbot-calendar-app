package com.chatbotcal.service.google;

import com.chatbotcal.event.CalendarEventData;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarMeetingCreator {

    @Autowired
    private Calendar calendar;

    private static final Logger logger = LoggerFactory.getLogger(CalendarMeetingCreator.class);

    public Event createEvent(CalendarEventData eventData) throws Exception {

        Event eventToCreate = new Event()
                .setSummary(eventData.getTitle())
                .setLocation(eventData.getLocation());

        String description = "Created By Chatbot Calendar App";
        if (!eventData.getParticipants().isEmpty()) {
            description += "\nמשתתפים: " + String.join(", ", eventData.getParticipants());
        }
        eventToCreate.setDescription(description);

        DateTime startDateTime = new DateTime(eventData.getStartDateTime() + "+03:00" );
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Jerusalem");

        DateTime endDateTime = new DateTime(eventData.getEndDateTime() + "+03:00" );
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Jerusalem");

        eventToCreate.setStart(start);
        eventToCreate.setEnd(end);

        return calendar.events().insert("primary", eventToCreate).execute();
    }
}
