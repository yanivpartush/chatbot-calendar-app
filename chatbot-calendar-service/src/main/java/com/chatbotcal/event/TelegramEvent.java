package com.chatbotcal.event;

import com.chatbotcal.infrastructure.handler.TelegramEventHandler;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import com.chatbotcal.repository.enums.UserIntent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextEvent.class, name = "text"),
        @JsonSubTypes.Type(value = VoiceEvent.class, name = "voice")
})
public abstract class TelegramEvent {

    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    private String timeZone;
    private String languageCode;


    public abstract void dispatch(TelegramEventHandler handler) throws Exception;

    public abstract String getText();

    public abstract UserMessage toUserMessage(User user, MessageStatus status);

}
