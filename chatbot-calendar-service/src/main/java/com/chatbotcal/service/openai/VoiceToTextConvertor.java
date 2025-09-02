package com.chatbotcal.service.openai;

import com.chatbotcal.event.VoiceEvent;
import com.theokanning.openai.audio.CreateTranscriptionRequest;
import com.theokanning.openai.audio.TranscriptionResult;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VoiceToTextConvertor {

    private final OpenAiService service;

    private static final Logger logger = LoggerFactory.getLogger(VoiceToTextConvertor.class);

    public String convertBase64VoiceToText(VoiceEvent voiceEvent) throws IOException {

        byte[] audioBytes = Base64.getDecoder().decode(voiceEvent.getVoiceBase64());

        File tempFile = File.createTempFile("voice-", ".ogg");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(audioBytes);

            CreateTranscriptionRequest request = CreateTranscriptionRequest.builder()
                    .model("whisper-1").language(voiceEvent.getLanguageCode())
                    .build();

            TranscriptionResult result = service.createTranscription(request, tempFile);
            String transcript = result.getText();

            logger.info("Voice transcription result: {}", transcript);

            return transcript;
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
            }
        }
    }
}
