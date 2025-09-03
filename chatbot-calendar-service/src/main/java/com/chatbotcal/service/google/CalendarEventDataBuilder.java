package com.chatbotcal.service.google;

import com.chatbotcal.event.CalendarEventData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalendarEventDataBuilder {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static CalendarEventData extractCalendarEventData(String openaiResponse) throws Exception {

        JsonNode meetingDetailsJson = mapper.readTree(openaiResponse);

        String title = meetingDetailsJson.get("title").asText();
        String date = meetingDetailsJson.get("date").asText();
        String time = meetingDetailsJson.get("time").asText();

        String location = Optional.ofNullable(meetingDetailsJson.get("location"))
                .map(JsonNode::asText)
                .orElse("");

        List<String> participants = new ArrayList<>();
        if (meetingDetailsJson.has("participants") && meetingDetailsJson.get("participants").isArray()) {
            for (JsonNode participantNode : meetingDetailsJson.get("participants")) {
                participants.add(participantNode.asText());
            }
        }




        String startDateTime = String.format("%sT%s:00", date, time);
        String endDateTime = String.format("%sT%s:00", date, LocalTime.parse(time).plusMinutes(60));

        return CalendarEventData.builder()
                .title(title)
                .location(location)
                .participants(participants)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)

                .build();
    }
}
