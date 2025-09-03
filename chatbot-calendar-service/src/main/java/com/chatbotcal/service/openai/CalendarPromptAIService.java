package com.chatbotcal.service.openai;

import com.chatbotcal.util.JsonTemplateUtil;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarPromptAIService {

    private final OpenAiService openAiService;

    public String getCalendarEventFromPrompt(String prompt, String timeZone) throws IOException {
        String jsonBody = JsonTemplateUtil.loadTemplateWithPromptAndTimezone("gpt-request-template.json", prompt, timeZone);

        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), jsonBody);


        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(userMessage))
                .temperature(0.0)
                .build();


        ChatCompletionResult result = openAiService.createChatCompletion(request);

        return result.getChoices().get(0).getMessage().getContent();
    }
}

