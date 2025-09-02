package com.chatbotcal.controller.dto;

import com.chatbotcal.repository.entity.UserMessage;
import com.chatbotcal.repository.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageResponseDto {
    private Long id;
    private String userId;
    private String textMessage;
    private MessageStatus status;

    public static UserMessageResponseDto fromEntity(UserMessage entity) {
        return UserMessageResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .textMessage(entity.getMessageText())
                .status(entity.getStatus())
                .build();
    }
}
