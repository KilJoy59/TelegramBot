package ru.avperm.TelegramSyncBotApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avperm.TelegramSyncBotApi.model.TelegramUsers;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUsers, Integer> {

    TelegramUsers findByChatId(Long chatId);

    Boolean existsByChatId(Long chatId);
}
