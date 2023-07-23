package com.pavelitelprojects.tutorbot.service.manager.timetable;

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
import static com.pavelitelprojects.tutorbot.service.data.CallbackData.*;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    @Autowired
    public TimetableManager(AnswerMethodFactory methodFactory,
                            KeyboardFactory keyboardFactory) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case TIMETABLE -> {
                return mainMenu(callbackQuery);
            }
            case TIMETABLE_SHOW -> {
                return show(callbackQuery);
            }
            case TIMETABLE_REMOVE -> {
                return remove(callbackQuery);
            }
            case TIMETABLE_ADD -> {
                return add(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое расписание",
                                "Удалить занятие", "Добавить занятие"),
                        List.of(1, 2),
                        List.of(TIMETABLE_SHOW, TIMETABLE_REMOVE, TIMETABLE_ADD)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое расписание",
                                "Удалить занятие", "Добавить занятие"),
                        List.of(1, 2),
                        List.of(TIMETABLE_SHOW, TIMETABLE_REMOVE, TIMETABLE_ADD)
                )
        );
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
          callbackQuery,
                """
                        📆 Выберете день недели""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)
                )
        );
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        ✏️ Выберете день, в который хотите добавить занятие:""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)
                )
        );
    }

    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        ✂️ Выберете занятие, которое хотите удалить из вашего расписания""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)
                )
        );
    }

}
