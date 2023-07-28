package com.pavelitelprojects.tutorbot.service.manager.timetable;

import com.pavelitelprojects.tutorbot.entity.timetable.TimeTable;
import com.pavelitelprojects.tutorbot.entity.timetable.WeekDay;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.repository.TimeTableRepo;
import com.pavelitelprojects.tutorbot.repository.UserRepo;
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
    final UserRepo userRepo;
    final TimeTableRepo timeTableRepo;
    @Autowired
    public TimetableManager(AnswerMethodFactory methodFactory,
                            KeyboardFactory keyboardFactory,
                            TimeTableRepo timeTableRepo,
                            UserRepo userRepo) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
        this.timeTableRepo = timeTableRepo;
        this.userRepo = userRepo;
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
            case TIMETABLE_1, TIMETABLE_2, TIMETABLE_3,
                    TIMETABLE_4, TIMETABLE_5, TIMETABLE_6,
                    TIMETABLE_7 -> {
                return showDay(callbackQuery);
            }
        }
        return null;
    }
    private BotApiMethod<?> mainMenu(Message message) {
        var user = userRepo.findUserByChatId(message.getChatId());
        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    """
                            📆 Здесь вы можете посмотреть ваше расписание""",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        }
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
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getEditeMessageText(
                    callbackQuery,
                    """
                            📆 Здесь вы можете посмотреть ваше расписание""",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        }
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
    private BotApiMethod<?> showDay(CallbackQuery callbackQuery) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        WeekDay weekDay = WeekDay.MONDAY;
        switch (callbackQuery.getData().split("_")[1]){
            case "2" -> weekDay = WeekDay.TUESDAY;
            case "3" -> weekDay = WeekDay.WEDNESDAY;
            case "4" -> weekDay = WeekDay.THURSDAY;
            case "5" -> weekDay = WeekDay.FRIDAY;
            case "6" -> weekDay = WeekDay.SATURDAY;
            case "7" -> weekDay = WeekDay.SUNDAY;
        }
        List<TimeTable> timeTableList = timeTableRepo
                .findAllByUsersContainingAndWeekDay(user, weekDay);
        StringBuilder text = new StringBuilder();
        if (timeTableList == null || timeTableList.isEmpty()) {
            text.append("У вас нет занятий в этот день!");
        } else {
            text.append("Ваши занятия сегодня:\n\n");
            for (TimeTable t: timeTableList) {
                text.append("▪\uFE0F ")
                        .append(t.getHour())
                        .append(":")
                        .append(t.getMinute())
                        .append(" - ")
                        .append(t.getTittle())
                        .append("\n\n");
            }
        }
        return methodFactory.getEditeMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)
                )
        );
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
          callbackQuery,
                """
                        📆 Выберете день недели""",
                keyboardFactory.getInlineKeyboard(
                        List.of(
                                "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
                                "Назад"
                        ),
                        List.of(7, 1),
                        List.of(
                                TIMETABLE_1, TIMETABLE_2, TIMETABLE_3, TIMETABLE_4, TIMETABLE_5, TIMETABLE_6, TIMETABLE_7,
                                TIMETABLE
                        )
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
