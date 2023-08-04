package com.pavelitelprojects.tutorbot.service.manager.task;

import com.pavelitelprojects.tutorbot.entity.task.Task;
import com.pavelitelprojects.tutorbot.entity.user.Action;
import com.pavelitelprojects.tutorbot.entity.user.User;
import com.pavelitelprojects.tutorbot.repository.TaskRepo;
import com.pavelitelprojects.tutorbot.repository.UserRepo;
import com.pavelitelprojects.tutorbot.service.factory.AnswerMethodFactory;
import com.pavelitelprojects.tutorbot.service.factory.KeyboardFactory;
import com.pavelitelprojects.tutorbot.service.manager.AbstractManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.pavelitelprojects.tutorbot.service.data.CallbackData.*;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepo userRepo;
    final TaskRepo taskRepo;

    @Autowired
    public TaskManager(AnswerMethodFactory methodFactory,
                       KeyboardFactory keyboardFactory, UserRepo userRepo, TaskRepo taskRepo) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        Long chatId = message.getChatId();
        var user = userRepo.findUserByChatId(chatId);
        try {
            bot.execute(methodFactory.getDeleteMessage(chatId, message.getMessageId() - 1));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        switch (user.getAction()) {
            case SENDING_TASK -> {
                return addTask(message, chatId, user);
            }
        }

        return null;
    }
    @Transactional
    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case TASK -> {
                return mainMenu(callbackQuery);
            }
            case TASK_CREATE -> {
                return create(callbackQuery);
            }
        }
        String[] splitCallbackData = callbackData.split("_");
        if (splitCallbackData.length > 2) {
            String keyWord = splitCallbackData[2];
            switch (keyWord) {
                case USER -> {
                    return setUser(callbackQuery, splitCallbackData);
                }
                case CANCEL -> {
                    try {
                        return abortCreation(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> abortCreation(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        taskRepo.deleteById(UUID.fromString(id));
        bot.execute(methodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(), "Операция успешно отменена"
        ));
        return methodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> addTask(Message message, Long chatId, User user) {
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);
        task.setMessageId(message.getMessageId());
        taskRepo.save(task);
        String id = String.valueOf(task.getId());
        user.setAction(Action.FREE);
        userRepo.save(user);
        return methodFactory.getSendMessage(
                chatId,
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 2),
                        List.of(TASK_CREATE_TEXT + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id, TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)
                )
        );
    }
    private BotApiMethod<?> setUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        taskRepo.deleteByUsersContainingAndIsInCreation(user, true);
        taskRepo.save(Task.builder()
                .users(List.of(
                        userRepo.findUserByChatId(Long.valueOf(splitCallbackData[3])),
                        user
                ))
                .isInCreation(true)
                .build());
        user.setAction(Action.SENDING_TASK);
        userRepo.save(user);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        Отправьте задание одним сообщением, в дальнейшем, вы сможете его изменить""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }
    private BotApiMethod<?> create(CallbackQuery callbackQuery) {
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        var teacher = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        int index = 0;
        for (User student : teacher.getUsers()) {
            text.add(student.getDetails().getFirstName());
            data.add(TASK_CREATE_USER + student.getChatId());
            if (index == 4) {
                cfg.add(index);
            } else {
                index++;
            }
        }
        if (index != 0) {
            cfg.add(index);
        }
        data.add(TASK);
        text.add("Назад");
        cfg.add(1);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание""",
                keyboardFactory.getInlineKeyboard(
                        text,
                        cfg,
                        data
                )
        );
    }
    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }
}
