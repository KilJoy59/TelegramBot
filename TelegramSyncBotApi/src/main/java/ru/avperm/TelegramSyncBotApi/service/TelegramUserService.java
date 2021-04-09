package ru.avperm.TelegramSyncBotApi.service;

import ru.avperm.TelegramSyncBotApi.model.TelegramUsers;

public interface TelegramUserService {

    TelegramUsers findUserByChatId(Long chatId);

    TelegramUsers saveUser(Long chatId, String firstName, String lastName, String userName);

    Boolean userExist(Long chatId);
}
