package com.chatbotcal.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JsonTemplateUtil {

    public static InputStream loadExternalResource(String fileName) throws FileNotFoundException {
        String configDir = System.getenv("CONFIG_DIR");
        if (configDir == null) {
            throw new IllegalStateException("CONFIG_DIR environment variable not set");
        }

        Path path = Paths.get(configDir, fileName);
        return new FileInputStream(path.toFile());
    }

    public static String loadTemplate(String fileName, String prompt) throws IOException {
        InputStream in = loadExternalResource(fileName);
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
