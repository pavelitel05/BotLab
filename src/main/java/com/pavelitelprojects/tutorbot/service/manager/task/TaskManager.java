package com.pavelitelprojects.tutorbot.service.manager.task;

import com.pavelitelprojects.tutorbot.entity.task.CompleteStatus;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
                try {
                    return addTask(message, chatId, user, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
            case SENDING_TEXT -> {
                try {
                    return editText(message, chatId, user, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
            case SENDING_MEDIA -> {
                try {
                    return editMedia(message, chatId, user, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
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
        if (MENU.equals(splitCallbackData[1])) {
            return menu(callbackQuery, splitCallbackData[2]);
        }
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
                case TEXT -> {
                    return askText(callbackQuery, splitCallbackData[3]);
                }
                case MEDIA -> {
                    return askMedia(callbackQuery, splitCallbackData[3]);
                }
                case SEND -> {
                    return askConfirmation(callbackQuery, splitCallbackData[3]);
                }
                case CONFIRM -> {
                    try {
                        return sendTask(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
                case CHANGE -> {
                    return askUser(callbackQuery, splitCallbackData[4]);
                }
                case STUDENT -> {
                    return editStudent(callbackQuery, splitCallbackData[3]);
                }
                case SUCCESS -> {
                    try {
                        return sendInfo(callbackQuery, splitCallbackData[3], bot, true);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
                case FAIL -> {
                    try {
                        return sendInfo(callbackQuery, splitCallbackData[3], bot, false);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> sendInfo(CallbackQuery callbackQuery, String id, Bot bot, boolean status) throws TelegramApiException {
        var task = taskRepo.findById(UUID.fromString(id)).orElseThrow();
        task.setIsFinished(true);
        var teacher = task.getTeacher();
        String studentName = task.getStudent().getDetails().getFirstName();
        bot.execute(
                methodFactory.getEditMessageReplyMarkup(
                        callbackQuery, null
                )
        );

        if (status) {
            task.setCompleteStatus(CompleteStatus.SUCCESS);
            taskRepo.save(task);
            return methodFactory.getSendMessage(
                    teacher.getChatId(),
                    "Ученик " + studentName +
                            " успешно выполнил(а) задание",
                    null
            );
        } else {
            task.setCompleteStatus(CompleteStatus.FAIL);
            taskRepo.save(task);
            return methodFactory.getSendMessage(
                    teacher.getChatId(),
                    "Ученик " + studentName +
                            " не справился(ась) с заданием",
                    null
            );
        }
    }

    private BotApiMethod<?> editStudent(CallbackQuery callbackQuery, String userId) {
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(
                userRepo.findUserByChatId(callbackQuery.getMessage().getChatId()), true
        );
        var student = userRepo.findUserByChatId(Long.valueOf(userId));
        task.changeUser(student);
        taskRepo.save(task);
        return menu(callbackQuery, String.valueOf(task.getId()));
    }

    private BotApiMethod<?> askUser(CallbackQuery callbackQuery, String id) {
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        var teacher = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        int index = 0;
        for (User student : teacher.getUsers()) {
            text.add(student.getDetails().getFirstName());
            data.add(TASK_CREATE_STUDENT + student.getChatId());
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
        data.add(TASK_MENU + id);
        text.add("Назад");
        cfg.add(1);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                """
                        👤 Измените получателя, которому хотите дать домашнее задание""",
                keyboardFactory.getInlineKeyboard(
                        text,
                        cfg,
                        data
                )
        );
    }

    private BotApiMethod<?> sendTask(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        var user = userRepo.findUserByChatId(chatId);
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);

        bot.execute(methodFactory.getCopyMessage(
                task.getStudent().getChatId(),
                chatId,
                task.getMessageId(),
                keyboardFactory.getInlineKeyboard(
                        List.of("Готово", "Затрудняюсь с ответом"),
                        List.of(1, 1),
                        List.of(TASK_ANSWER_SUCCESS + id, TASK_ANSWER_FAIL + id)
                )
        ));

        bot.execute(methodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(), "Задание успешно отправлено"
        ));

        bot.execute(methodFactory.getDeleteMessage(
                chatId, task.getMessageId()
        ));
        task.setIsInCreation(false);
        taskRepo.save(task);
        return methodFactory.getDeleteMessage(
                chatId, task.getMenuId()
        );
    }

    private BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String id) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
                "Подтвердите, что хотите отправить задание выше ученику",
                keyboardFactory.getInlineKeyboard(
                        List.of("Да", "Нет"),
                        List.of(2),
                        List.of(TASK_CREATE_CONFIRM + id, TASK_MENU + id)
                )
        );
    }

    private BotApiMethod<?> editMedia(Message message, Long chatId, User user, Bot bot) throws TelegramApiException {
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);
        if (message.hasText()) {
            return methodFactory.getSendMessage(
                    chatId,
                    "Сообщение должно содержать один медиа файл (Фото, Видео, Документ или Аудио)\n\n" +
                            "И не должно содержать текста",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Назад"),
                            List.of(1),
                            List.of(TASK_MENU + task.getId())
                    )
            );
        }
        task.setHasMedia(true);
        int previousId = task.getMessageId();
        task.setMessageId(Math.toIntExact(bot.execute(
                methodFactory.getCopyMessage(
                        chatId,
                        chatId,
                        message.getMessageId()
                )
        ).getMessageId()));
        if (task.getTextContent() != null && !task.getTextContent().isEmpty()) {
            bot.execute(methodFactory.getEditMessageCaption(
                    chatId, task.getMessageId(), task.getTextContent()
            ));
        }
        bot.execute(methodFactory.getDeleteMessage(
                chatId, previousId
        ));
        task.setMenuId(bot.execute(menu(message, user)).getMessageId());
        taskRepo.save(task);
        return null;
    }

    private BotApiMethod<?> editText(Message message, Long chatId, User user, Bot bot) throws TelegramApiException {
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);
        if (!message.hasText()) {
            return methodFactory.getSendMessage(
                    chatId,
                    "Сообщение должно содержать текст",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Назад"),
                            List.of(1),
                            List.of(TASK_MENU + task.getId())
                    )
            );
        }
        String messageText = message.getText();
        task.setTextContent(messageText);
        int previousId = task.getMessageId();
        if (task.getHasMedia()) {
            bot.execute(methodFactory.getEditMessageCaption(
                    chatId, previousId, messageText
            ));
        } else {
            bot.execute(methodFactory.getEditeMessageText(
                    chatId, previousId, messageText
            ));
        }

        task.setMessageId(Math.toIntExact(bot.execute(methodFactory.getCopyMessage(
                chatId, chatId, previousId
        )).getMessageId()));

        bot.execute(methodFactory.getDeleteMessage(
                chatId, previousId
        ));
        task.setMenuId(bot.execute(menu(message, user)).getMessageId());
        taskRepo.save(task);
        return null;
    }

    private BotApiMethod<?> askMedia(CallbackQuery callbackQuery, String id) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_MEDIA);
        userRepo.save(user);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                "Отправьте измененное Фото|Видео|Документ|Аудио",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_MENU + id)
                )
        );
    }

    private BotApiMethod<?> askText(CallbackQuery callbackQuery, String id) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_TEXT);
        userRepo.save(user);
        return methodFactory.getEditeMessageText(
                callbackQuery,
                "Отправьте измененный текст",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_MENU + id)
                )
        );
    }

    private BotApiMethod<?> menu(CallbackQuery callbackQuery, String id) {
        return methodFactory.getEditeMessageText(
                callbackQuery,
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

    private SendMessage menu(Message message, User user) {
        user.setAction(Action.FREE);
        userRepo.save(user);
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);
        String id = String.valueOf(task.getId());
        return methodFactory.getSendMessage(
                message.getChatId(),
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

    private BotApiMethod<?> abortCreation(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        Integer messageId = taskRepo.findById(UUID.fromString(id)).get().getMessageId();
        taskRepo.deleteById(UUID.fromString(id));
        bot.execute(methodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(), "Операция успешно отменена"
        ));
        bot.execute(methodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(), messageId
        ));
        return methodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> addTask(Message message, Long chatId, User user, Bot bot) throws TelegramApiException {
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);
        task.setMessageId(Math.toIntExact(bot.execute(
                methodFactory.getCopyMessage(
                        chatId, chatId, message.getMessageId()
                )
        ).getMessageId()));
        if (message.hasVideo() || message.hasPhoto() || message.hasDocument() || message.hasAudio()) {
            task.setHasMedia(true);
        } else {
            task.setHasMedia(false);
        }
        if (message.hasText()) {
            task.setTextContent(message.getText());
        }
        String id = String.valueOf(task.getId());
        user.setAction(Action.FREE);
        userRepo.save(user);
        task.setMenuId(bot.execute(methodFactory.getSendMessage(
                chatId,
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 2),
                        List.of(TASK_CREATE_TEXT + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id, TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)
                )
        )).getMessageId());
        taskRepo.save(task);
        return null;
    }

    private BotApiMethod<?> setUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        taskRepo.deleteByUsersContainingAndIsInCreation(user, true);
        taskRepo.save(Task.builder()
                .users(List.of(
                        userRepo.findUserByChatId(Long.valueOf(splitCallbackData[3])),
                        user
                ))
                .isFinished(false)
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
                index = 0;
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
