package com.chatbotcal.event;

import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramEvent {
    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    private String text;
    private String timeZone;

    public static TelegramEvent fromUserMessage(UserMessage msg) {
        User user = msg.getUser();
        return TelegramEvent.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .timeZone(user.getTimeZone())
                .text(msg.getTextMessage())
                .build();
    }
}
