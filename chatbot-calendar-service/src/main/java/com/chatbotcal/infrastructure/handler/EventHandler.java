package com.chatbotcal.infrastructure.handler;

import com.chatbotcal.event.TelegramMsgEvent;

public interface EventHandler {

    void on(TelegramMsgEvent event);

}
