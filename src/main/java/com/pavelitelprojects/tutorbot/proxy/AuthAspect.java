package com.pavelitelprojects.tutorbot.proxy;

import com.pavelitelprojects.tutorbot.entity.user.Action;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.entity.user.User;
import com.pavelitelprojects.tutorbot.repository.UserRepo;
import com.pavelitelprojects.tutorbot.service.manager.auth.AuthManager;
import com.pavelitelprojects.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Aspect
@Component
@Order(100)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthAspect {
    final UserRepo userRepo;
    final AuthManager authManager;

    @Autowired
    public AuthAspect(UserRepo userRepo,
                      AuthManager authManager) {
        this.userRepo = userRepo;
        this.authManager = authManager;
    }

    @Pointcut("execution(* com.pavelitelprojects.tutorbot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut() {
    }

    @Around("distributeMethodPointcut()")
    public Object authMethodAdvice(ProceedingJoinPoint joinPoint)
            throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
        User user;
        if (update.hasMessage()) {
            user = userRepo.findById(update.getMessage().getChatId()).orElseThrow();
        } else if (update.hasCallbackQuery()) {
            user = userRepo.findById(update.getCallbackQuery().getMessage().getChatId()).orElseThrow();
        } else {
            return joinPoint.proceed();
        }

        if (user.getRole() != Role.EMPTY) {
            return joinPoint.proceed();
        }

        if (user.getAction() == Action.AUTH) {
            return joinPoint.proceed();
        }
        return authManager.answerMessage(update.getMessage(),
                (Bot) joinPoint.getArgs()[1]);
    }

}
