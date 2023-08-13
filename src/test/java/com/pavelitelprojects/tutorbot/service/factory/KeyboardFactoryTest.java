package com.pavelitelprojects.tutorbot.service.factory;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class KeyboardFactoryTest {
    private final KeyboardFactory keyboardFactory;

    @Autowired
    public KeyboardFactoryTest(KeyboardFactory keyboardFactory) {
        this.keyboardFactory = keyboardFactory;
    }

    @Test
    public void testGetInlineKeyboard() {
        // given
        List<String> text = List.of("Button 1", "Button 2", "Button 3");
        List<Integer> configuration = List.of(2, 2);
        List<String> data = List.of("data1", "data2", "data3");

        // when
        InlineKeyboardMarkup keyboardMarkup = keyboardFactory.getInlineKeyboard(text, configuration, data);

        // then
        assertNull(keyboardMarkup);

    }

    @Test
    public void testGetReplyKeyboard() {
        // given
        List<String> text = List.of("Button 1", "Button 3");
        List<Integer> configuration = List.of(2, 1);

        // when
        ReplyKeyboardMarkup keyboardMarkup = keyboardFactory.getReplyKeyboard(text, configuration);

        // then
        assertNull(keyboardMarkup);
    }
}