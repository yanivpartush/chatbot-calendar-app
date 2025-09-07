package com.chatbotcal.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class JsonTemplateUtil {

    public static InputStream loadExternalResource(String fileName) throws FileNotFoundException {
        String configDir = System.getenv("CONFIG_DIR");
        if (configDir == null) {
            throw new IllegalStateException("CONFIG_DIR environment variable not set");
        }

        Path path = Paths.get(configDir, fileName);
        return new FileInputStream(path.toFile());
    }

    public static String loadTemplateWithPrompt(
            String fileName,
            String prompt,
            String timeZone
    ) throws IOException {
        return loadTemplateWithPrompt(fileName, prompt, timeZone, "[]");
    }

    public static String loadTemplateWithPrompt(
            String fileName,
            String prompt,
            String timeZone,
            String eventsJson
    ) throws IOException {
        InputStream in = loadExternalResource(fileName);
        if (in == null) {
            throw new FileNotFoundException("Template file not found: " + fileName);
        }

        String template;
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
            template = scanner.useDelimiter("\\A").next();
        }

        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return template
                .replace("{{prompt}}", escapeJson(prompt))
                .replace("{{timeZone}}", escapeJson(timeZone))
                .replace("{{now}}", escapeJson(now))
                .replace("{{events}}", (eventsJson == null || eventsJson.isBlank()) ? "[]" : eventsJson);
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
