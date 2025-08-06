package com.chatbotcal.service.openai;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class OpenAIService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-url}")
    private String apiUrl;

    public String getCalendarEventFromPrompt(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = "{\n" +
                "  \"model\": \"gpt-4-1106-preview\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"המר הודעת טקסט לאובייקט JSON של פגישת יומן. הפלט צריך להכיל: title, date (YYYY-MM-DD), time (HH:mm), location (אם יש), participants (אם יש).\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + prompt + "\"}\n" +
                "  ],\n" +
                "  \"temperature\": 0.1\n" +
                "}";

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

