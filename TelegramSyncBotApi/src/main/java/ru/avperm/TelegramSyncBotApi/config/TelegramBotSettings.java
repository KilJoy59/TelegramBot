package ru.avperm.TelegramSyncBotApi.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class TelegramBotSettings {

    @Value("${token}")
    private String token;

    @Value("${botName}")
    private String botName;

    @Value("${password}")
    private String password;
}
