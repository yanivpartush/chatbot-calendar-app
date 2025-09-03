package com.chatbotcal.service.tinyurl;

import com.chatbotcal.infrastructure.handler.TelegramEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class TinyUrlService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramEventHandler.class);

    public String shorten(String longUrl) {
        try {
            logger.info("Long URL: {}", longUrl);
            String requestUrl = "http://tinyurl.com/api-create.php?url=" + java.net.URLEncoder.encode(longUrl, "UTF-8");
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(HttpMethod.GET.name());

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = reader.readLine();
            logger.info("Shorten URL: {}", result);
            return result;

        } catch (Exception e) {
            logger.info("Failed to shorten URL: {}", e);
        }
        return longUrl;
    }

}
