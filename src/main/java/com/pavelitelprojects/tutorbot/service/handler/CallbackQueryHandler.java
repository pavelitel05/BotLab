package com.pavelitelprojects.tutorbot.service.handler;

import com.pavelitelprojects.tutorbot.service.manager.feedback.FeedbackManager;
import com.pavelitelprojects.tutorbot.service.manager.help.HelpManager;
import com.pavelitelprojects.tutorbot.service.manager.task.TaskManager;
import com.pavelitelprojects.tutorbot.service.manager.timetable.TimetableManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.pavelitelprojects.tutorbot.service.data.CallbackData.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackQueryHandler {
    final HelpManager helpManager;
    final FeedbackManager feedbackManager;
    final TimetableManager timetableManager;
    final TaskManager taskManager;
    @Autowired
    public CallbackQueryHandler(HelpManager helpManager,
                                FeedbackManager feedbackManager,
                                TimetableManager timetableManager,
                                TaskManager taskManager) {
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String keyWord = callbackData.split("_")[0];
        if (TIMETABLE.equals(keyWord)) {
            return timetableManager.answerCallbackQuery(callbackQuery, bot);
        }
        if (TASK.equals(keyWord)) {
            return taskManager.answerCallbackQuery(callbackQuery, bot);
        }
        switch (callbackData) {
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
        }
        return null;
    }
}
