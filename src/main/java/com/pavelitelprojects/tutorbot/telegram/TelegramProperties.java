package com.pavelitelprojects.tutorbot.telegram;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("telegram-bot")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramProperties {
    String username;
    String token;
    String path;
}
