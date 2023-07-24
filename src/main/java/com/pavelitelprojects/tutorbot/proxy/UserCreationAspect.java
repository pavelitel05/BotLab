package com.pavelitelprojects.tutorbot.proxy;

import com.pavelitelprojects.tutorbot.entity.user.Action;
import com.pavelitelprojects.tutorbot.entity.user.Role;
import com.pavelitelprojects.tutorbot.entity.user.UserDetails;
import com.pavelitelprojects.tutorbot.repository.DetailsRepo;
import com.pavelitelprojects.tutorbot.repository.UserRepo;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Aspect
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationAspect {
    final UserRepo userRepo;
    final DetailsRepo detailsRepo;
    @Autowired
    public UserCreationAspect(UserRepo userRepo,
                              DetailsRepo detailsRepo) {
        this.userRepo = userRepo;
        this.detailsRepo = detailsRepo;
    }

    @Pointcut("execution(* com.pavelitelprojects.tutorbot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut(){}

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint)
            throws Throwable{
        Object[] args = joinPoint.getArgs();
        Update update = (Update) args[0];
        User telegramUser;
        if (update.hasMessage()) {
            telegramUser = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            telegramUser = update.getCallbackQuery().getFrom();
        } else {
            return joinPoint.proceed();
        }

        if (userRepo.existsById(telegramUser.getId())) {
            return joinPoint.proceed();
        }

        UserDetails details = UserDetails.builder()
                .firstName(telegramUser.getFirstName())
                .username(telegramUser.getUserName())
                .lastName(telegramUser.getLastName())
                .registeredAt(LocalDateTime.now())
                .build();
        detailsRepo.save(details);
        com.pavelitelprojects.tutorbot.entity.user.User newUser =
                com.pavelitelprojects.tutorbot.entity.user.User.builder()
                        .chatId(telegramUser.getId())
                        .action(Action.FREE)
                        .role(Role.USER)
                        .details(details)
                        .build();
        userRepo.save(newUser);

        return joinPoint.proceed();
    }


}
