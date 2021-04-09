package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AwtList {

    private String recipientNumber;

    private String recipientName;
}
