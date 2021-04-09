package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CSTerminalDto {

    private String recipientNumber;

    private Integer idAtp;

    private String nameAtp;

}
