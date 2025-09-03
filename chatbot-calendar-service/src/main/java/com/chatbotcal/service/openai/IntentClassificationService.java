package com.chatbotcal.service.openai;

import com.chatbotcal.repository.enums.UserIntent;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntentClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(IntentClassificationService.class);

    private final OpenAiService openAiService;

    public UserIntent classifyMessage(String userMessage) throws IOException {
        String configDir = System.getenv("CONFIG_DIR");
        String template = Files.readString(Paths.get(configDir, "intent_prompt.txt"), StandardCharsets.UTF_8);

        String prompt = String.format(template, userMessage);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                .temperature(0.0)
                .maxTokens(20)
                .build();

        ChatCompletionResult response = openAiService.createChatCompletion(request);
        String result = response.getChoices().get(0).getMessage().getContent().trim().toUpperCase();

        logger.info("OpenAI classification result: {} for result message -> {}", result, userMessage);

        switch (result) {
            case "SCHEDULE_MEETING":
                return UserIntent.SCHEDULE_MEETING;
            case "GET_WEEKLY_SCHEDULE":
                return UserIntent.GET_WEEKLY_SCHEDULE;
            case "CHECK_AVAILABILITY":
                return UserIntent.CHECK_AVAILABILITY;
            default:
                return UserIntent.UNKNOWN;
        }
    }
}
