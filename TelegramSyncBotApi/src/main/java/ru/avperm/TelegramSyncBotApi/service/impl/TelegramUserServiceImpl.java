package ru.avperm.TelegramSyncBotApi.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avperm.TelegramSyncBotApi.model.TelegramUsers;
import ru.avperm.TelegramSyncBotApi.repository.TelegramUserRepository;
import ru.avperm.TelegramSyncBotApi.service.TelegramUserService;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */

@Service
@AllArgsConstructor
public class TelegramUserServiceImpl implements TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    @Override
    public TelegramUsers findUserByChatId(Long chatId) {
        return telegramUserRepository.findByChatId(chatId);
    }

    @Transactional
    @Override
    public TelegramUsers saveUser(Long chatId, String firstName, String lastName, String userName) {
        TelegramUsers user = new TelegramUsers(null, chatId, firstName, lastName, userName);
        return telegramUserRepository.save(user);
    }

    @Override
    public Boolean userExist(Long chatId) {
        return telegramUserRepository.existsByChatId(chatId);
    }
}
