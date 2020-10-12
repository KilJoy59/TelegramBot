package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */

@Data
@AllArgsConstructor
@Builder
public class CSTerminalDto {

    private String recipientNumber;

    private Integer idAtp;

    private String nameAtp;

}
