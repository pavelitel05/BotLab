package com.pavelitelprojects.tutorbot.service.manager.auth;

import com.pavelitelprojects.tutorbot.entity.user.Action;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.repository.UserRepo;
import com.pavelitelprojects.tutorbot.service.factory.AnswerMethodFactory;
import com.pavelitelprojects.tutorbot.service.factory.KeyboardFactory;
import com.pavelitelprojects.tutorbot.service.manager.AbstractManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import static com.pavelitelprojects.tutorbot.service.data.CallbackData.AUTH_TEACHER;
import static com.pavelitelprojects.tutorbot.service.data.CallbackData.AUTH_STUDENT;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthManager extends AbstractManager {
    final UserRepo userRepo;
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public AuthManager(UserRepo userRepo,
                       AnswerMethodFactory methodFactory,
                       KeyboardFactory keyboardFactory) {
        this.userRepo = userRepo;
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }


    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        Long chatId = message.getChatId();
        var user = userRepo.findById(chatId).orElseThrow();
        user.setAction(Action.AUTH);
        userRepo.save(user);
        return methodFactory.getSendMessage(
                chatId,
                """
                        Выберете свою роль""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Ученик", "Учитель"),
                        List.of(2),
                        List.of(AUTH_STUDENT, AUTH_TEACHER)
                )
        );
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        var user = userRepo.findById(chatId).orElseThrow();
        HashMap<String, String> commands = new HashMap<>();
        commands.put("start", "начни взаимодействовать с ботом");
        commands.put("help", "перечень доступной функиональности");
        commands.put("search", "установить соединение");
        commands.put("timetable", "расписание");
        commands.put("profile", "твоя личная информация");
        if (AUTH_TEACHER.equals(callbackQuery.getData())) {
            commands.put("task", "оставьте домашнее задание ученику");
            commands.put("progress", "отслеживание успеваемости");
            try {
                bot.execute(methodFactory.getBotCommandScopeChat(
                        chatId, commands
                ));
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
            user.setRole(Role.TEACHER);
        } else {
            try {
                bot.execute(methodFactory.getBotCommandScopeChat(
                        chatId, commands
                ));
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
            user.setRole(Role.STUDENT);
        }
        user.setAction(Action.FREE);
        userRepo.save(user);

        try {
            bot.execute(
                    methodFactory.getAnswerCallbackQuery(
                    callbackQuery.getId(),
                    """
                            Авторизация прошла успешна, повторите предыдущий запрос!"""
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return methodFactory.getDeleteMessage(
                chatId,
                messageId
        );
    }
}
