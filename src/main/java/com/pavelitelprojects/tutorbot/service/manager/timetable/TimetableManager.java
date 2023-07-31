package com.pavelitelprojects.tutorbot.service.manager.timetable;

import com.pavelitelprojects.tutorbot.entity.timetable.TimeTable;
import com.pavelitelprojects.tutorbot.entity.timetable.WeekDay;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.entity.user.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        String[] splitCallbackData = callbackData.split("_");
        if (splitCallbackData.length > 1 && "add".equals(splitCallbackData[1])) {
            if (splitCallbackData.length == 2 || splitCallbackData.length == 3) {
                return add(callbackQuery, splitCallbackData);
            }
            switch (splitCallbackData[2]) {
                case WEEKDAY -> {
                    return addWeekDay(callbackQuery, splitCallbackData);
                }
                case HOUR -> {
                    return addHour(callbackQuery, splitCallbackData);
                }
                case MINUTE -> {
                    return addMinute(callbackQuery, splitCallbackData);
                }
                case USER -> {
                    return addUser(callbackQuery, splitCallbackData);
                }
            }

        }
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
            case TIMETABLE_1, TIMETABLE_2, TIMETABLE_3,
                    TIMETABLE_4, TIMETABLE_5, TIMETABLE_6,
                    TIMETABLE_7 -> {
                return showDay(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(splitCallbackData[4]));
        var user = userRepo.findUserByChatId(Long.valueOf(splitCallbackData[3]));
        timeTable.addUser(user);
        timeTable.setTittle(user.getDetails().getFirstName());
        timeTableRepo.save(timeTable);

        return methodFactory.getEditeMessageText(
                callbackQuery,
                "Успешно!",
                null
        );
    }

    private BotApiMethod<?> addMinute(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(id));
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        timeTable.setMinute(Short.valueOf(splitCallbackData[3]));
        int index = 0;
        for (User user: userRepo
                .findAllByUsersContaining(
                        userRepo.findUserByChatId(callbackQuery.getMessage().getChatId())
                )) {
            text.add(user.getDetails().getFirstName());
            data.add(TIMETABLE_ADD_USER + user.getChatId() + "_" + id);
            if (index == 5) {
                cfg.add(5);
                index = 0;
            } else {
                index += 1;
            }
        }
        cfg.add(index);
        cfg.add(1);
        data.add(TIMETABLE_ADD_HOUR + timeTable.getHour() + "_" + id);
        text.add("Назад");
        timeTableRepo.save(timeTable);

        String messageText = "Выберете ученика";
        if (cfg.size() == 1) {
            messageText = "У вас нет ни одного ученика";
        }

        return methodFactory.getEditeMessageText(
                callbackQuery,
                messageText,
                keyboardFactory.getInlineKeyboard(
                        text,
                        cfg,
                        data
                )
        );
    }

    private BotApiMethod<?> addHour(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(id));
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        timeTable.setHour(Short.valueOf(splitCallbackData[3]));
        for (int i = 0; i <= 59; i ++) {
            text.add(String.valueOf(i));
            data.add(TIMETABLE_ADD_MINUTE + i + "_" + id);
        }
        text.add("Назад");
        switch (timeTable.getWeekDay()) {
            case MONDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 1 + "_" + id);
            case TUESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 2 + "_" + id);
            case WEDNESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 3 + "_" + id);
            case THURSDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 4 + "_" + id);
            case FRIDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 5 + "_" + id);
            case SATURDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 6 + "_" + id);
            case SUNDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 7 + "_" + id);
        }
        timeTableRepo.save(timeTable);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                "Выберете минуту",
                keyboardFactory.getInlineKeyboard(
                        text,
                        List.of(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1),
                        data
                )
        );
    }

    private BotApiMethod<?> addWeekDay(CallbackQuery callbackQuery, String[] data) {
        UUID id = UUID.fromString(data[4]);
        var timeTable = timeTableRepo.findTimeTableById(id);
        switch (data[3]) {
            case "1" -> timeTable.setWeekDay(WeekDay.MONDAY);
            case "2" -> timeTable.setWeekDay(WeekDay.TUESDAY);
            case "3" -> timeTable.setWeekDay(WeekDay.WEDNESDAY);
            case "4" -> timeTable.setWeekDay(WeekDay.THURSDAY);
            case "5" -> timeTable.setWeekDay(WeekDay.FRIDAY);
            case "6" -> timeTable.setWeekDay(WeekDay.SATURDAY);
            case "7" -> timeTable.setWeekDay(WeekDay.SUNDAY);
        }
        List<String> buttonsData = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            text.add(String.valueOf(i));
            buttonsData.add(TIMETABLE_ADD_HOUR + i + "_" + data[4]);
        }
        buttonsData.add(TIMETABLE_ADD + "_" + data[4]);
        text.add("Назад");
        timeTableRepo.save(timeTable);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                "Выберете час",
                keyboardFactory.getInlineKeyboard(
                        text,
                        List.of(6, 6, 6, 6, 1),
                        buttonsData
                )
        );
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
        switch (callbackQuery.getData().split("_")[1]) {
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
            for (TimeTable t : timeTableList) {
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
                        List.of("\uD83D\uDD19Назад"),
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
                                TIMETABLE_1, TIMETABLE_2, TIMETABLE_3,
                                TIMETABLE_4, TIMETABLE_5, TIMETABLE_6, TIMETABLE_7,
                                TIMETABLE
                        )
                )
        );
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = "";
        if (splitCallbackData.length == 2) {
            var timeTable = new TimeTable();
            timeTable.addUser(userRepo.findUserByChatId(callbackQuery.getMessage().getChatId()));
            id = timeTableRepo.save(timeTable).getId().toString();
        } else {
            id = splitCallbackData[2];
        }
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            data.add(TIMETABLE_ADD_WEEKDAY + i + "_" + id);
        }
        data.add(TIMETABLE);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        ✏️ Выберете день, в который хотите добавить занятие:""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
                                "\uD83D\uDD19Назад"),
                        List.of(7, 1),
                        data
                )
        );
    }

    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        ✂️ Выберете занятие, которое хотите удалить из вашего расписания""",
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDD19Назад"),
                        List.of(1),
                        List.of(TIMETABLE)
                )
        );
    }

}
