package com.chatbotcal.event.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarEventData {
    private String title;
    private String location;
    private String startDateTime; // ISO 8601: 2025-08-12T10:00:00
    private String endDateTime;


    public static CalendarEventData getCalendarEventData(String telegramJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper(); // need to create bean
        JsonNode root = mapper.readTree(telegramJson);

        String content = root
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();

        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}") + 1;

        String actualJson = content.substring(startIndex, endIndex);

        JsonNode realData = mapper.readTree(actualJson);

        String date = realData.get("date").asText();
        System.out.println("Date: " + date);

        //String date = root.get("date").asText(); // 2025-08-12
        String time = realData.get("time").asText();
        System.out.println("time: " + time);
        CalendarEventData calendarEventData = CalendarEventData.builder().title(realData.get("title").asText())
                //.location(realData.get("location").asText())
                                                               .startDateTime(date + "T" + time + ":00")
                                                               .endDateTime(date + "T" + LocalTime.parse(time)
                                                                                                  .plusMinutes(30) + ":00")
                                                               .build();
        return calendarEventData;
    }


}
