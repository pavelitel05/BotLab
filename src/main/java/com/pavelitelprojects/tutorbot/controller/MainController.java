package com.pavelitelprojects.tutorbot.controller;

import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainController {
    final Bot bot;

    @Autowired
    public MainController(Bot bot) {
        this.bot = bot;
    }

    @PostMapping("/")
    public BotApiMethod<?> listener(@RequestBody Update update) {
        if (update.hasMessage()) {
            return echo(update.getMessage());
        }
        return bot.onWebhookUpdateReceived(update);
    }

    private BotApiMethod<?> echo(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(message.getText())
                .build();
    }

}
