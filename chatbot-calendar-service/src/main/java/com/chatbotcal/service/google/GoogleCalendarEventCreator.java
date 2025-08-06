package com.chatbotcal.service.google;

import com.chatbotcal.event.google.CalendarEventData;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.*;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarEventCreator {

    @Autowired
    private Calendar calendar;

    public void createEvent(CalendarEventData eventData) throws Exception {
        //Calendar service = googleCalendarService.getCalendarService();

        Event event = new Event()
                .setSummary(eventData.getTitle())
                .setLocation(eventData.getLocation())
                .setDescription("נוצר אוטומטית מ־GPT");

        DateTime startDateTime = new DateTime(eventData.getStartDateTime());
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Jerusalem");

        DateTime endDateTime = new DateTime(eventData.getEndDateTime());
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Jerusalem");

        event.setStart(start);
        event.setEnd(end);

        calendar.events().insert("primary", event).execute();
        System.out.println("אירוע נוצר: " + event.getHtmlLink());
    }
}
