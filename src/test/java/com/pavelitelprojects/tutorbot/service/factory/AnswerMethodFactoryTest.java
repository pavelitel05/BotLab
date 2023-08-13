package com.pavelitelprojects.tutorbot.service.factory;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AnswerMethodFactoryTest {
    private final AnswerMethodFactory methodFactory;
    private final String text = "Test text";
    private final InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
    private final CallbackQuery callbackQuery = new CallbackQuery();
    private final Message message = new Message();

    @BeforeEach
    void setUp() {
        Chat chat = new Chat();
        chat.setId(123L);
        message.setChat(chat);
        message.setMessageId(123);
        callbackQuery.setMessage(message);
        callbackQuery.setId("123");
    }

    @Autowired
    public AnswerMethodFactoryTest(AnswerMethodFactory methodFactory) {
        this.methodFactory = methodFactory;
    }

    @Test
    public void testGetAnswerCallbackQuery() {
        // given
        String callbackQueryId = callbackQuery.getId();

        // when
        AnswerCallbackQuery expectedAnswer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .build();

        AnswerCallbackQuery actualAnswer = methodFactory.getAnswerCallbackQuery(callbackQueryId, text);

        // then
        assertEquals(expectedAnswer, actualAnswer);
    }

    @Test
    public void testGetSendMessage() {
        // given
        Long chatId = message.getChatId();

        // when
        SendMessage expectedMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboardMarkup)
                .disableWebPagePreview(true)
                .build();


        SendMessage actualMessage = methodFactory.getSendMessage(chatId, text, keyboardMarkup);

        // then
        assertEquals(expectedMessage, actualMessage);
    }


    @Test
    public void testGetEditMessageText() {
        // when
        EditMessageText expected = EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(text)
                .replyMarkup(keyboardMarkup)
                .disableWebPagePreview(true)
                .build();

        // then
        EditMessageText actual = methodFactory.getEditeMessageText(
                callbackQuery,
                text,
                keyboardMarkup
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDeleteMessage() {
        // given
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        // when
        DeleteMessage expected = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();

        // then
        DeleteMessage actual = methodFactory.getDeleteMessage(
                chatId,
                messageId
        );
        assertEquals(expected, actual);
    }
}