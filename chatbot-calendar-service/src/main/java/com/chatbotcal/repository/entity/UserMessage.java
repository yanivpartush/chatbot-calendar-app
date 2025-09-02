package com.chatbotcal.repository.entity;

import com.chatbotcal.repository.enums.MessageStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message_text", length = 1000)
    private String messageText;

    @Column(name = "voice_file_id")
    private String voiceFileId;

    @Column(name = "voice_duration")
    private Integer voiceDuration;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

}

