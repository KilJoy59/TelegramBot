package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class LoginNotification {

    private Long id;

    private String header;

    private String text;

    private Date dt;
}
