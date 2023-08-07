package com.pavelitelprojects.tutorbot.service.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Component
public class AnswerMethodFactory {

    public SendMessage getSendMessage(Long chatId,
                                      String text,
                                      ReplyKeyboard keyboard) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .build();
    }

    public EditMessageText getEditeMessageText(CallbackQuery callbackQuery,
                                               String text,
                                               InlineKeyboardMarkup keyboard) {
        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(text)
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .build();
    }

    public EditMessageText getEditeMessageText(Long chatId,
                                               Integer messageId,
                                               String text) {
        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .disableWebPagePreview(true)
                .build();
    }

    public DeleteMessage getDeleteMessage(Long chatId,
                                          Integer messageId) {
        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public AnswerCallbackQuery getAnswerCallbackQuery(String callbackQueryId,
                                                      String text) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .build();
    }

    public CopyMessage getCopyMessage(Long chatId, Long fromChatId, Integer messageId) {
        return CopyMessage.builder()
                .fromChatId(fromChatId)
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public CopyMessage getCopyMessage(Long chatId, Long fromChatId, Integer messageId, ReplyKeyboard replyKeyboard) {
        return CopyMessage.builder()
                .fromChatId(fromChatId)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(replyKeyboard)
                .build();
    }

    public EditMessageCaption getEditMessageCaption(Long chatId,
                                                    Integer messageId,
                                                    String caption) {
        return EditMessageCaption.builder()
                .chatId(chatId)
                .caption(caption)
                .messageId(messageId)
                .build();
    }

    public EditMessageReplyMarkup getEditMessageReplyMarkup(CallbackQuery callbackQuery,
                                                            InlineKeyboardMarkup inlineKeyboardMarkup) {
        return EditMessageReplyMarkup.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

}
