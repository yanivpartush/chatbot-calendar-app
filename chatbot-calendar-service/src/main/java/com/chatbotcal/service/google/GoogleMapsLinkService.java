package com.chatbotcal.service.google;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleMapsLinkService {

    public String getLinkFromPlace(String placeName) {
        if (placeName == null || placeName.trim().isEmpty()) {
            return null;
        }
        String encoded = URLEncoder.encode(placeName, StandardCharsets.UTF_8);
        return "https://www.google.com/maps/search/?api=1&query=" + encoded;
    }
}