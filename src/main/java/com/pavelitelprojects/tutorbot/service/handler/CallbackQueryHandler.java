package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.telegram.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Service
public class CallbackQueryHandler {
    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }
}
