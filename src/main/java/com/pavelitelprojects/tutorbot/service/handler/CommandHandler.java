package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.telegram.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.pavelitelprojects.tutorbot.service.data.Command.*;

@Service
public class CommandHandler {
    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START -> {
                return start(message);
            }
            case FEEDBACK -> {
                return feedback(message);
            }
            case HELP -> {
                return help(message);
            }
            default -> {
                return defaultAnswer(message);
            }
        }
    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage.builder()
                .text("Неподдерживаемая команда!")
                .chatId(message.getChatId())
                .build();
    }

    private BotApiMethod<?> help(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        📍 Доступные команды:
                        - start
                        - help
                        - feedback
                                                
                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости
                                                
                        """)
                .build();
    }

    private BotApiMethod<?> feedback(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/pavelitel05
                        LinkedIn - https://linkedin.com/in/павел-кирсанов-62b762263
                        Telegram - https://t.me/pavelitel05
                        """)
                .disableWebPagePreview(true)
                .build();
    }

    private BotApiMethod<?> start(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        🖖Приветствую в Tutor-Bot, инструменте для упрощения взаимодействия репититора и ученика.
                                                
                        Что бот умеет?
                        📌 Составлять расписание
                        📌 Прикреплять домашние задания
                        📌 Ввести контроль успеваемости                        
                        """)
                .build();
    }

}
