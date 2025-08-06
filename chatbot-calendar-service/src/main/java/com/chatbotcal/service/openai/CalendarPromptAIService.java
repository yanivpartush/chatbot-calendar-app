package com.chatbotcal.service.openai;

import com.chatbotcal.util.JsonTemplateUtil;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class CalendarPromptAIService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-url}")
    private String apiUrl;

    public String getCalendarEventFromPrompt(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = JsonTemplateUtil.loadTemplate("gpt-request-template.json", prompt);

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
}

