package com.chatbotcal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GPTJsonExtractor {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode extractInnerJson(String gptResponse) throws Exception {

        JsonNode root = mapper.readTree(gptResponse);

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            throw new IllegalStateException("No choices found in GPT response");
        }

        String content = choices.get(0).path("message").path("content").asText();
        String jsonBlock = extractJsonBlock(content);
        return mapper.readTree(jsonBlock);
    }

    private static String extractJsonBlock(String content) throws Exception {
        String startMarker = "```json";
        String endMarker = "```";

        int startIndex = content.indexOf(startMarker);
        if (startIndex == -1) {
            throw new IllegalStateException("Missing ```json marker");
        }
        startIndex += startMarker.length();

        int endIndex = content.indexOf(endMarker, startIndex);
        if (endIndex == -1) {
            throw new IllegalStateException("Missing closing ``` marker");
        }

        return content.substring(startIndex, endIndex).trim();
    }
}

