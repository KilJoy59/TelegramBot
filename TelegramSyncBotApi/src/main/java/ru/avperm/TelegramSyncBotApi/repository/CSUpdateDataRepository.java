package ru.avperm.TelegramSyncBotApi.repository;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */

//@Repository
//public interface CSUpdateDataRepository extends JpaRepository<CSUpdateData, CSUpdateDataPK> {
//
//    @Query(value = "SELECT t.* FROM TUPDATEDATA t WHERE t.recipientNumber like :code", nativeQuery = true)
//    List<CSUpdateData> findByCode(@Param("code") String code);
//
//
//    @Query(value = "SELECT u.* FROM TUPDATEDATA u WHERE u.DT_PUSH is null OR u.UPDATE_STATUS = 'Pull' order by u.recipientNumber asc", nativeQuery = true)
//    List<CSUpdateData> findAllNotLoaded();
//
//    @Query(value = "SELECT u.* FROM TUPDATEDATA u WHERE u.DT_PUSH is not null OR u.UPDATE_STATUS = 'Push' order by u.recipientNumber asc", nativeQuery = true)
//    List<CSUpdateData> findAllLoaded();
//}
