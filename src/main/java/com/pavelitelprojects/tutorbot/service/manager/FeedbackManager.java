package com.pavelitelprojects.tutorbot.service.manager;

import com.pavelitelprojects.tutorbot.service.factory.AnswerMethodFactory;
import com.pavelitelprojects.tutorbot.service.factory.KeyboardFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public FeedbackManager(AnswerMethodFactory methodFactory,
                           KeyboardFactory keyboardFactory) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    public BotApiMethod<?> answerCommand(Message message) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/pavelitel05
                        LinkedIn - https://linkedin.com/in/павел-кирсанов-62b762263
                        Telegram - https://t.me/pavelitel05
                        """,
                null
        );
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/pavelitel05
                        LinkedIn - https://linkedin.com/in/павел-кирсанов-62b762263
                        Telegram - https://t.me/pavelitel05
                        """,
                null
        );
    }

}
