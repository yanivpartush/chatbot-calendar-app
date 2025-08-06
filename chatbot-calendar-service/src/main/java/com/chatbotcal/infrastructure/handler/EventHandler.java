package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.event.TelegramEvent;

public interface EventHandler {

    void on(TelegramEvent event);

}
