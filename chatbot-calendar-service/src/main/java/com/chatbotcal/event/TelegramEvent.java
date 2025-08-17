package com.chatbotcal.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramEvent {
    private String userId;
    private String chatId;
    private String firstName;
    private String lastName;
    private String username;
    private String text;
    private String timeZone;
}
