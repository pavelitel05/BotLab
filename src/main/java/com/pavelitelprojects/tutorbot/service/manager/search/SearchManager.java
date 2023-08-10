package com.pavelitelprojects.tutorbot.service.manager.search;

import com.pavelitelprojects.tutorbot.entity.user.Action;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.entity.user.User;
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
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.pavelitelprojects.tutorbot.service.data.CallbackData.SEARCH_CANCEL;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchManager extends AbstractManager {

    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepo userRepo;
    @Autowired
    public SearchManager(AnswerMethodFactory methodFactory,
                         KeyboardFactory keyboardFactory,
                         UserRepo userRepo) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepo = userRepo;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return askToken(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        try {
            bot.execute(methodFactory.getDeleteMessage(
                    message.getChatId(),
                    message.getMessageId() - 1
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        var user = userRepo.findUserByChatId(message.getChatId());
        switch (user.getAction()) {
            case SENDING_TOKEN -> {
                return checkToken(message, user);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        switch (callbackQuery.getData()) {
            case SEARCH_CANCEL -> {
                try {
                    return cancel(callbackQuery, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> checkToken(Message message,
                                       User user) {
        String token = message.getText();
        var userTwo = userRepo.findUserByToken(token);
        if (userTwo == null) {
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    "По данному токену не найдено ни одного пользователя\n\nПовторите попытку!",
                    keyboardFactory.getInlineKeyboard(
                            List.of("❌ Отмена операции"),
                            List.of(1),
                            List.of(SEARCH_CANCEL)
                    )
            );
        }

        if (validation(user, userTwo)) {
            if (user.getRole() == Role.TEACHER) {
                user.addUser(userTwo);
            } else {
                userTwo.addUser(user);
            }
            user.setAction(Action.FREE);
            userRepo.save(user);
            userRepo.save(userTwo);
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    "✅ Связь успешно установлена",
                    null
            );
        }
        return methodFactory.getSendMessage(
                message.getChatId(),
                "Вы не можете установить соединение с учителем, если и вы им являетесь" +
                        " или то же самое, если вы ученик\n\nПовторите попытку!",
                keyboardFactory.getInlineKeyboard(
                        List.of("❌ Отмена операции"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)
                )
        );
    }

    private BotApiMethod<?> cancel(CallbackQuery callbackQuery, Bot bot) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        var user = userRepo.findUserByChatId(chatId);
        user.setAction(Action.FREE);
        userRepo.save(user);

        bot.execute(methodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(),
                "Операция отменена успешно"
        ));
        return methodFactory.getDeleteMessage(
                chatId, callbackQuery.getMessage().getMessageId()
        );
    }
    private boolean validation(User userOne, User userTwo) {
        return userOne.getRole() != userTwo.getRole();
    }
    private BotApiMethod<?> askToken(Message message) {
        Long chatId = message.getChatId();
        var user = userRepo.findUserByChatId(chatId);
        user.setAction(Action.SENDING_TOKEN);
        userRepo.save(user);
        return methodFactory.getSendMessage(
                chatId,
                "Отправьте токен",
                keyboardFactory.getInlineKeyboard(
                        List.of("❌ Отмена операции"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)
                )
        );
    }

}
