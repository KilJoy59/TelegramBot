package ru.avperm.TelegramSyncBotApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avperm.TelegramSyncBotApi.model.TelegramMemory;

@Repository
public interface TelegramMemoryRepository extends JpaRepository<TelegramMemory, Integer> {

}
