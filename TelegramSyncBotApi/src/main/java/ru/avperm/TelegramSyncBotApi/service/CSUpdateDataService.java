package ru.avperm.TelegramSyncBotApi.service;

import java.util.HashMap;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */
public interface CSUpdateDataService {

    HashMap<String, String> getAllByCode(String code);

    String getAllNotLoaded(int startLimit, int count, int listCount);

    String getAllLoaded(int startLimit, int count, int listCount);

    String getAllAwt(int startLimit, int count, int listCount);

    String getOnlyNamesAllLoadedAwt();

    String getOnlyNamesAllLoadedTerminals();

    String getOnlyNamesAllNotLoadedTerminals();

    String getOnlyNamesAllNotLoadedAwt();

    String setNullOneRow(String code, String nameEntity);

    String setNullAllRows(String code);
}
