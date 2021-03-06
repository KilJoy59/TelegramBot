package ru.avperm.TelegramSyncBotApi.service;

import java.util.HashMap;

public interface CSUpdateDataService {

    HashMap<String, String> getAllByCode(String code);

    String getAllNotLoaded(int startLimit, int count, int listCount);

    String getAllLoaded(int startLimit, int count, int listCount);

    String getAllAwt(int startLimit, int count, int listCount);

    String getOnlyNamesAllLoadedAwt();

    String getOnlyNamesAllLoadedTerminals();

    String getOnlyNamesAllNotLoadedTerminals();

    String getOnlyNamesAllNotLoadedAwt();

    String reloadOneEntityFromUpdateData(String code, String nameEntity);

    String reloadAllEntitiesFromUpdateData(String code);
}
