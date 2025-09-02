package com.chatbotcal.event;

import com.chatbotcal.infrastructure.handler.TelegramEventHandler;
import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoiceEvent extends TelegramEvent {

    private String voiceFileId;
    private int voiceDuration;
    private String voiceBase64;
    private String transcript;

    @Override
    public void dispatch(TelegramEventHandler handler) throws Exception {
        handler.on(this);
    }

    @Override
    public String getText() {
        return transcript;
    }

    @Override
    public UserMessage toUserMessage(User user, MessageStatus status) {
        return UserMessage.builder()
                .user(user)
                .status(status)
                .messageText(this.getText())
                .voiceFileId(voiceFileId)
                .voiceDuration(voiceDuration)
                .build();
    }


}
