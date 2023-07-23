package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.service.manager.FeedbackManager;
import com.pavelitelprojects.tutorbot.service.manager.HelpManager;
import com.pavelitelprojects.tutorbot.service.manager.StartManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


import static com.pavelitelprojects.tutorbot.service.data.Command.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandHandler {
    final FeedbackManager feedbackManager;
    final HelpManager helpManager;
    final StartManager startManager;
    @Autowired
    public CommandHandler(FeedbackManager feedbackManager,
                          HelpManager helpManager,
                          StartManager startManager) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
        this.startManager = startManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START -> {
                return startManager.answerCommand(message);
            }
            case FEEDBACK_COMMAND -> {
                return feedbackManager.answerCommand(message);
            }
            case HELP_COMMAND -> {
                return helpManager.answerCommand(message);
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
}
