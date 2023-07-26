package com.pavelitelprojects.tutorbot.service.manager.profile;

import com.pavelitelprojects.tutorbot.repository.UserRepo;
import com.pavelitelprojects.tutorbot.service.factory.AnswerMethodFactory;
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


@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final UserRepo userRepo;

    @Autowired
    public ProfileManager(AnswerMethodFactory methodFactory,
                          UserRepo userRepo) {
        this.methodFactory = methodFactory;
        this.userRepo = userRepo;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return showProfile(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }

    private BotApiMethod<?> showProfile(Message message) {
        Long chatId = message.getChatId();
        StringBuilder text = new StringBuilder("\uD83D\uDC64 Профиль\n");
        var user = userRepo.findUserByChatId(chatId);
        var details = user.getDetails();

        if (details.getUsername() == null) {
            text.append("▪\uFE0FИмя пользователя - ").append(details.getUsername());
        } else {
            text.append("▪\uFE0FИмя пользователя - ").append(details.getFirstName());
        }
        text.append("\n▪\uFE0FРоль -").append(user.getRole().name());
        text.append("\n▪\uFE0FВаш уникальный токен - \n").append(user.getToken());
        text.append("\n\n⚠\uFE0F - токен необходим для того, чтобы ученик или преподаватель могли установиться между собой связь");
        return methodFactory.getSendMessage(
                chatId,
                text.toString(),
                null
        );
    }

}
