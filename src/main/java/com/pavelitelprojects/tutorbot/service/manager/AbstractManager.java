package com.pavelitelprojects.tutorbot.service.manager;

import com.pavelitelprojects.tutorbot.telegram.Bot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public abstract class AbstractManager {

    public abstract BotApiMethod<?> answerCommand(Message message, Bot bot);
    public abstract BotApiMethod<?> answerMessage(Message message, Bot bot);
    public abstract BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot);

}
