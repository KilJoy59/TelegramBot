package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */

@Data
@AllArgsConstructor
@Builder
public class CSUpdateData {

    private String entityName;

    private String recipientNumber;

    private String updateDataStatus;

    private Date datePull;

    private Date datePush;

    private String recipientName;

}
