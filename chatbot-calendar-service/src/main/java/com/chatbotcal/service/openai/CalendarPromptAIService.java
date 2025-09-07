package com.chatbotcal.service.openai;

import com.chatbotcal.event.CalendarEventData;
import com.chatbotcal.util.JsonTemplateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper mapper;

    public String executeCheckAvailabilityPrompt(String prompt, String timeZone, List<CalendarEventData> events) throws
            IOException {
        String eventsJson = mapper.writeValueAsString(events);
        String jsonBody = JsonTemplateUtil.loadTemplateWithPrompt("gpt-check-availability-template.json", prompt,
                                                                  timeZone, eventsJson);
        return executePrompt(jsonBody);
    }

    public String executeFutureEventPrompt(String prompt, String timeZone, List<CalendarEventData> events) throws
            IOException {
        String eventsJson = mapper.writeValueAsString(events);
        String jsonBody = JsonTemplateUtil.loadTemplateWithPrompt("gpt-future-events-template.json", prompt,
                                                                  timeZone, eventsJson);
        return executePrompt(jsonBody);
    }

    public String buildCalendarEventPrompt(String prompt, String timeZone) throws IOException {
        String jsonBody =
                JsonTemplateUtil.loadTemplateWithPrompt("gpt-build-calendar-event-template.json", prompt, timeZone);
        return executePrompt(jsonBody);
    }

    private String executePrompt(String jsonBody) {
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

