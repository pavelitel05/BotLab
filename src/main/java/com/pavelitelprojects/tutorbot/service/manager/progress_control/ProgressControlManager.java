package com.pavelitelprojects.tutorbot.service.manager.progress_control;

import com.pavelitelprojects.tutorbot.entity.task.CompleteStatus;
import com.pavelitelprojects.tutorbot.entity.task.Task;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.entity.user.User;
import com.pavelitelprojects.tutorbot.repository.TaskRepo;
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

import static com.pavelitelprojects.tutorbot.service.data.CallbackData.*;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressControlManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepo userRepo;
    final TaskRepo taskRepo;

    @Autowired
    public ProgressControlManager(AnswerMethodFactory methodFactory,
                                  KeyboardFactory keyboardFactory, UserRepo userRepo, TaskRepo taskRepo) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        var user = userRepo.findUserByChatId(message.getChatId());
        if (Role.STUDENT.equals(user.getRole())) {
            return null;
        }
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
            case PROGRESS -> {
                return mainMenu(callbackQuery);
            }
            case PROGRESS_STAT -> {
                return stat(callbackQuery);
            }
        }
        String[] splitCallbackData = callbackData.split("_");
        switch (splitCallbackData[1]) {
            case USER -> {
                return showUserStat(callbackQuery, splitCallbackData[2]);
            }
        }
        return null;
    }

    private BotApiMethod<?> showUserStat(CallbackQuery callbackQuery, String id) {
        var student = userRepo.findUserByToken(id);
        var details = student.getDetails();
        StringBuilder text = new StringBuilder("\uD83D\uDD39Статистика по пользователю \"")
                .append(details.getFirstName())
                .append("\"")
                .append("\n\n");
        int success = taskRepo.countAllByUsersContainingAndIsFinishedAndCompleteStatus(
                student, true, CompleteStatus.SUCCESS
        );
        int fail = taskRepo.countAllByUsersContainingAndIsFinishedAndCompleteStatus(
                student, true, CompleteStatus.FAIL
        );
        int sum = fail + success;
        text.append("\uD83D\uDCCDРешено - ")
                .append(success);
        text.append("\n\uD83D\uDCCDПровалено - ")
                .append(fail);
        text.append("\n\uD83D\uDCCDВсего - ")
                .append(sum);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDD19 Назад"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        Здесь вы можете увидеть статистику по каждому ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDCCA Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        Здесь вы можете увидеть статистику по каждому ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDCCA Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }
    private BotApiMethod<?> stat(CallbackQuery callbackQuery) {
        var teacher = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        List<User> students = teacher.getUsers();
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        int index = 0;
        for (User student: students) {
            text.add(student.getDetails().getFirstName());
            data.add(PROGRESS_USER + student.getToken());
            if (index == 4) {
                cfg.add(index);
                index = 0;
            } else {
                index++;
            }
        }
        if (index != 0) {
            cfg.add(index);
        }
        data.add(PROGRESS);
        text.add("\uD83D\uDD19 Назад");
        cfg.add(1);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                "\uD83D\uDC64 Выберете ученика",
                keyboardFactory.getInlineKeyboard(
                        text,
                        cfg,
                        data
                )
        );
    }

}
