package com.chatbotcal.event;

import com.chatbotcal.infrastructure.handler.TelegramEventHandler;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextEvent extends TelegramEvent {

    private String text;

    @Override
    public void dispatch(TelegramEventHandler handler) throws Exception {
        handler.on(this);
    }

    @Override
    public UserMessage toUserMessage(User user, MessageStatus status) {
        return UserMessage.builder()
                .user(user)
                .status(status)
                .messageText(text)
                .build();
    }

}
