package com.chatbotcal.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonTemplateUtil {

    public static String loadTemplate(String fileName, String prompt) throws IOException {
        InputStream in = JsonTemplateUtil.class.getClassLoader().getResourceAsStream(fileName);
        if (in == null) {
            throw new FileNotFoundException("Template file not found: " + fileName);
        }

        String template;
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
            template = scanner.useDelimiter("\\A").next();
        }

        return template.replace("{{prompt}}", escapeJson(prompt));
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
