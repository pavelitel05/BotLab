package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.repository.UserRepo;
import com.pavelitelprojects.tutorbot.service.manager.search.SearchManager;
import com.pavelitelprojects.tutorbot.service.manager.task.TaskManager;
import com.pavelitelprojects.tutorbot.service.manager.timetable.TimetableManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageHandler {

    final SearchManager searchManager;
    final TimetableManager timetableManager;
    final TaskManager taskManager;
    final UserRepo userRepo;
    @Autowired
    public MessageHandler(SearchManager searchManager,
                          TimetableManager timetableManager, TaskManager taskManager, UserRepo userRepo) {
        this.searchManager = searchManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.userRepo = userRepo;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        var user = userRepo.findUserByChatId(message.getChatId());
        switch (user.getAction()) {
            case SENDING_TOKEN -> {
                return searchManager.answerMessage(message, bot);
            }
            case SENDING_DESCRIPTION, SENDING_TITTLE -> {
                return timetableManager.answerMessage(message, bot);
            }
            case SENDING_TASK, SENDING_MEDIA, SENDING_TEXT -> {
                return taskManager.answerMessage(message, bot);
            }
        }
        return null;
    }
}
