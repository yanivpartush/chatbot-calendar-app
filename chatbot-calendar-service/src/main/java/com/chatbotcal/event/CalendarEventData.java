package com.chatbotcal.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CalendarEventData {

    private String title;
    private String location;
    private String startDateTime; // ISO 8601: 2025-08-12T10:00:00
    private String endDateTime;

    private List<String> participants;

}
