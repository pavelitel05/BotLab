package com.pavelitelprojects.tutorbot.service.manager.help;

import com.pavelitelprojects.tutorbot.service.factory.AnswerMethodFactory;
import com.pavelitelprojects.tutorbot.service.factory.KeyboardFactory;
import com.pavelitelprojects.tutorbot.service.manager.AbstractManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static com.pavelitelprojects.tutorbot.service.data.CallbackData.START;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HelpManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public HelpManager(AnswerMethodFactory methodFactory,
                       KeyboardFactory keyboardFactory) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }
    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        📍 Доступные команды:
                        Отображаются в панели "меню"
                                                
                        📍 Доступные функции:
                        - Расписание (/timetable)
                        - Домашнее задание (/task)
                        - Контроль успеваемости (/progress)
                                                
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Главное меню"),
                        List.of(1),
                        List.of(START)
                )

        );
    }
    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        📍 Доступные команды:
                        Отображаются в панели "меню"
                                                
                        📍 Доступные функции:
                        - Расписание (/timetable)
                        - Домашнее задание (/task)
                        - Контроль успеваемости (/progress)
                                                
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDD19 Назад"),
                        List.of(1),
                        List.of(START)
                )
        );
    }
    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
