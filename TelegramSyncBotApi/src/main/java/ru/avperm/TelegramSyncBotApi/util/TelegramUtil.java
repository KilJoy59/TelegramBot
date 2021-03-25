package ru.avperm.TelegramSyncBotApi.util;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TelegramUtil {

    public static SendMessage createSimpleMessageFromCallBackQuery(Update update, String textMessage) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId)
                .setText(textMessage);
        return message;
    }

    public static SendMessage createSimpleMessage(Update update, String textMessage) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId)
                .setText(textMessage);
        return message;
    }

    public static SendMessage createHtmlMessageForSchedule(String chatId, String textMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId)
                .setText(textMessage)
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview();
        return message;
    }

    public static SendMessage createHtmlMessageWithInlineKeyboard(Update update, String textMessage, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId)
                .setText(textMessage);
        return new SendMessage()
                .setChatId(chatId)
                .setText(textMessage)
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview()
                .setReplyMarkup(inlineKeyboardMarkup);
    }

    public static SendMessage createHtmlMessageWithInlineKeyboardFromCallBackQuery(Update update, String textMessage, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId)
                .setText(textMessage);
        return new SendMessage()
                .setChatId(chatId)
                .setText(textMessage)
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview()
                .setReplyMarkup(inlineKeyboardMarkup);
    }

    public static SendMessage createHtmlMessage(Update update, String textMessage) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId)
                .setText(textMessage);
        return new SendMessage()
                .setChatId(chatId)
                .setText(textMessage)
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview();
    }

    public static InlineKeyboardMarkup createInlineKeyboardMarkup(String textOfButton, String textOfData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(new InlineKeyboardButton().setText(textOfButton).setCallbackData(textOfData));
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
