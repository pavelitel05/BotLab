package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.telegram.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class MessageHandler {
    public BotApiMethod<?> answer(Message message, Bot bot) {
        return null;
    }
}
