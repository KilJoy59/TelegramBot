package ru.avperm.TelegramSyncBotApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(Application.class, args);
    }
}
