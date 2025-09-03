package com.chatbotcal.service.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@Service
public class NotificationMessageService {

    private ResourceBundle bundle;

    public NotificationMessageService(@Value("${app.default-language:en}") String defaultLanguage) throws IOException {
        Locale defaultLocale;

        if (defaultLanguage == null || defaultLanguage.isBlank() || defaultLanguage.equalsIgnoreCase("en")) {
            defaultLocale = Locale.ROOT;
        } else {
            defaultLocale = new Locale(defaultLanguage);
        }

        bundle = extractCorrectBundle(defaultLocale);
    }

    private ResourceBundle extractCorrectBundle(Locale defaultLocale) throws IOException {
        String configDir = System.getenv("CONFIG_DIR");
        if (configDir == null) {
            throw new IllegalStateException("CONFIG_DIR environment variable not set");
        }

        String bundleName = "messages";
        String fileName = bundleName
                + (defaultLocale == Locale.ROOT ? "" : "_" + defaultLocale.getLanguage())
                + ".properties";

        Path path = Paths.get(configDir, "messages", fileName);
        try (InputStream in = Files.newInputStream(path)) {
            Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            bundle = new PropertyResourceBundle(reader);
        }
        return bundle;
    }

    public String getMessage(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return String.format(pattern, args);
        } catch (MissingResourceException e) {
            return "???" + key + "???";
        }
    }
}
