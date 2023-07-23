package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.service.manager.FeedbackManager;
import com.pavelitelprojects.tutorbot.service.manager.HelpManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.pavelitelprojects.tutorbot.service.data.CallbackData.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackQueryHandler {
    final HelpManager helpManager;
    final FeedbackManager feedbackManager;
    @Autowired
    public CallbackQueryHandler(HelpManager helpManager,
                                FeedbackManager feedbackManager) {
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery);
            }
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery);
            }
        }
        return null;
    }
}
