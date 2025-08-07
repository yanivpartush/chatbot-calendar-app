package com.chatbotcal.controller.dto;

import com.chatbotcal.repository.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private Date creationDate;
    private List<UserMessageResponseDto> messages;

    public static UserDtoResponse fromEntity(User user) {
        return UserDtoResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .creationDate(user.getCreationDate())
                .messages(user.getMessages().stream()
                                  .map(UserMessageResponseDto::fromEntity)
                                  .collect(Collectors.toList()))
                .build();
    }
}
