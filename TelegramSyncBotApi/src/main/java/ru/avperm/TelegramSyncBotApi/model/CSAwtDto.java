package ru.avperm.TelegramSyncBotApi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */

@Data
@AllArgsConstructor
@Builder
public class CSAwtDto implements Serializable {

    private Integer id;

    private String nameFull;

}
