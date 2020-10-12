package ru.avperm.TelegramSyncBotApi.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avperm.TelegramSyncBotApi.model.AwtList;
import ru.avperm.TelegramSyncBotApi.model.CSTerminalDto;
import ru.avperm.TelegramSyncBotApi.model.CSUpdateData;
import ru.avperm.TelegramSyncBotApi.service.CSUpdateDataService;

import javax.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class CSUpdateDataServiceImpl implements CSUpdateDataService {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final EntityManagerFactory entityManagerFactory;

    private static String editRowWithSpace(String string, int before, int length) {
        string = "\u2503" + String.join("", Collections.nCopies(before, " ")) + string;
        int stringLength = string.length();
        int needLength = length - stringLength - 1;
        String border = String.join("", Collections.nCopies(needLength, " "));
        if (stringLength < length) {
            string = string + border;
        }
        return string;
    }

    private static String editRowWithSpacelastColumn(String string, int before, int length) {
        string = "\u2503" + String.join("", Collections.nCopies(before, " ")) + string;
        int stringLength = string.length();
        int needLength = length - stringLength - 1;
        String border = String.join("", Collections.nCopies(needLength, " "));
        if (stringLength < length) {
            string = string + border + "\u2503";
        }
        return string;
    }

    @Override
    public HashMap<String, String> getAllByCode(String code) {
        String nameEntities = "";
        HashMap<String, String> resultMap = new HashMap<>();
        String result = " ";
        String queryStr = "SELECT u.entityName as entityName, u.recipientNumber as recipientNumber, u.UPDATE_STATUS as updateDataStatus, " +
                "u.DT_PULL as datePull, u.DT_PUSH as datePush, s.name as recipientName FROM TUPDATEDATA u " +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber " +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID " +
                "WHERE u.recipientNumber = :code order by u.entityName asc";
        List<CSUpdateData> syncList = new ArrayList<>();
        List<Object[]> resultList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {
            openSession.getTransaction().begin();
            resultList = openSession.createNativeQuery(queryStr)
                    .setParameter("code", code)
                    .addScalar("entityName", StandardBasicTypes.STRING)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("updateDataStatus", StandardBasicTypes.STRING)
                    .addScalar("datePull", StandardBasicTypes.TIMESTAMP)
                    .addScalar("datePush", StandardBasicTypes.TIMESTAMP)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach((p) -> {
                syncList.add(CSUpdateData.builder()
                        .entityName((String) p[0])
                        .recipientNumber((String) p[1])
                        .updateDataStatus((String) p[2])
                        .datePull((Timestamp) p[3])
                        .datePush((Timestamp) p[4])
                        .recipientName((String) p[5])
                        .build());
            });
        }

        if (syncList.isEmpty()) {
            resultMap.put("Список пуст", nameEntities);
            return resultMap;
        }

        result = result
                .concat("<b>Синхронизация справочников " + syncList.get(0).getRecipientName() + "</b>").concat("\n")
                .concat("<pre>")
                .concat(String.join("", Collections.nCopies(69, "\u2501"))).concat("\n")
                .concat(editRowWithSpace("справочник", 1, 18))
                .concat(editRowWithSpace("начало загрузки", 1, 23))
                .concat(editRowWithSpace("конец загрузки", 1, 23))
                .concat(editRowWithSpacelastColumn("статус", 0, 8))
                .concat("</pre>").concat("\n")
                .concat("<pre>")
                .concat(String.join("", Collections.nCopies(69, "\u2501")))
                .concat("</pre>").concat("\n");

        for (CSUpdateData data : syncList) {
            String entityName = data.getEntityName() == null
                    ? "null"
                    : data.getEntityName();
            nameEntities += entityName + ",";
            String datePull = data.getDatePull() == null
                    ? "null"
                    : sdf.format(data.getDatePull());
            String datePush = data.getDatePush() == null
                    ? "null"
                    : sdf.format(data.getDatePush());
            String status = data.getUpdateDataStatus() == null
                    ? "null"
                    : data.getUpdateDataStatus();

            result = result.concat("<pre>")
                    .concat(editRowWithSpace(entityName, 1, 18))
                    .concat(editRowWithSpace(datePull, 1, 23))
                    .concat(editRowWithSpace(datePush, 1, 23))
                    .concat(editRowWithSpacelastColumn(status, 1, 8))
                    .concat("</pre>").concat("\n");
        }
        result = result.concat("<pre>").concat(String.join("", Collections.nCopies(69, "\u2501"))).concat("</pre>")
                .concat("<b>Выберите справочник для повторной выгрузки:</b>");
        nameEntities = nameEntities.substring(0, nameEntities.length() - 1);
        log.debug(" СТРОКА " + nameEntities);
        resultMap.put(result, nameEntities);
        return resultMap;
    }

    @Override
    public String getAllNotLoaded(int startLimit, int count, int listCount) {
        String result = " ";
        String queryStr = "SELECT u.entityName as entityName, u.recipientNumber as recipientNumber, u.UPDATE_STATUS as updateDataStatus, " +
                "u.DT_PULL as datePull, u.DT_PUSH as datePush, s.name as recipientName FROM TUPDATEDATA u " +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber " +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID " +
                "WHERE u.UPDATE_STATUS = 'Pull' order by s.name asc LIMIT :startLimit, :count";
        List<CSUpdateData> syncList = new ArrayList<>();
        List<Object[]> resultList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultList = openSession.createNativeQuery(queryStr)
                    .setParameter("startLimit", startLimit)
                    .setParameter("count", count)
                    .addScalar("entityName", StandardBasicTypes.STRING)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("updateDataStatus", StandardBasicTypes.STRING)
                    .addScalar("datePull", StandardBasicTypes.TIMESTAMP)
                    .addScalar("datePush", StandardBasicTypes.TIMESTAMP)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach((p) -> {
                syncList.add(CSUpdateData.builder()
                        .entityName((String) p[0])
                        .recipientNumber((String) p[1])
                        .updateDataStatus((String) p[2])
                        .datePull((Timestamp) p[3])
                        .datePush((Timestamp) p[4])
                        .recipientName((String) p[5])
                        .build());
            });
        }

        if (syncList.isEmpty()) {
            return "Список пуст";
        }
        result = result.concat("<pre>")
                .concat(" Синхронизация справочников (с ошибками)").concat("\n")
                .concat(String.join("", Collections.nCopies(88, "\u2501"))).concat("\n")
                .concat(editRowWithSpace("справочник", 1, 18))
                .concat(editRowWithSpace("код или название", 1, 22))
                .concat(editRowWithSpace("начало загрузки", 1, 22))
                .concat(editRowWithSpace("конец загрузки", 1, 22))
                .concat(editRowWithSpacelastColumn("статус", 0, 8))
                .concat("</pre>").concat("\n")
                .concat("<pre>")
                .concat(String.join("", Collections.nCopies(88, "\u2501")))
                .concat("</pre>").concat("\n");

        for (CSUpdateData data : syncList) {
            String entityName = data.getEntityName() == null
                    ? "null"
                    : data.getEntityName();
            String nameAwt =
                    data.getRecipientName() == null
                            ? data.getRecipientNumber()
                            : data.getRecipientName();
            String datePull = data.getDatePull() == null
                    ? "null"
                    : sdf.format(data.getDatePull());
            String datePush = data.getDatePush() == null
                    ? "null"
                    : sdf.format(data.getDatePush());
            String status = data.getUpdateDataStatus() == null
                    ? "null"
                    : data.getUpdateDataStatus();

            result = result.concat("<pre>")
                    .concat(editRowWithSpace(entityName, 1, 18))
                    .concat(editRowWithSpace(nameAwt, 0, 22))
                    .concat(editRowWithSpace(datePull, 0, 22))
                    .concat(editRowWithSpace(datePush, 0, 22))
                    .concat(editRowWithSpacelastColumn(status, 1, 8))
                    .concat("</pre>").concat("\n");
        }
        result = result.concat("<pre>").concat(String.join("", Collections.nCopies(88, "\u2501")))
                .concat("\n").concat("страница " + listCount + " </pre>");
        return result;
    }

    @Override
    public String getAllLoaded(int startLimit, int count, int listCount) {
        String result = " ";
        String queryStr = "SELECT u.entityName as entityName, u.recipientNumber as recipientNumber, u.UPDATE_STATUS as updateDataStatus, " +
                "u.DT_PULL as datePull, u.DT_PUSH as datePush, s.name as recipientName FROM TUPDATEDATA u " +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber " +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID " +
                "WHERE u.UPDATE_STATUS = 'Push' order by s.name desc LIMIT :startLimit, :count";
        List<CSUpdateData> syncList = new ArrayList<>();
        List<Object[]> resultList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultList = openSession.createNativeQuery(queryStr)
                    .setParameter("startLimit", startLimit)
                    .setParameter("count", count)
                    .addScalar("entityName", StandardBasicTypes.STRING)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("updateDataStatus", StandardBasicTypes.STRING)
                    .addScalar("datePull", StandardBasicTypes.TIMESTAMP)
                    .addScalar("datePush", StandardBasicTypes.TIMESTAMP)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();

            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach((p) -> {
                syncList.add(CSUpdateData.builder()
                        .entityName((String) p[0])
                        .recipientNumber((String) p[1])
                        .updateDataStatus((String) p[2])
                        .datePull((Timestamp) p[3])
                        .datePush((Timestamp) p[4])
                        .recipientName((String) p[5])
                        .build());
            });
        }

        if (syncList.isEmpty()) {
            return "Список пуст";
        }
        result = result.concat("<pre>")
                .concat(" Синхронизация справочников (без ошибок)").concat("\n")
                .concat(String.join("", Collections.nCopies(88, "\u2501"))).concat("\n")
                .concat(editRowWithSpace("справочник", 1, 18))
                .concat(editRowWithSpace("код или название", 1, 22))
                .concat(editRowWithSpace("начало загрузки", 1, 22))
                .concat(editRowWithSpace("конец загрузки", 1, 22))
                .concat(editRowWithSpacelastColumn("статус", 0, 8))
                .concat("</pre>").concat("\n")
                .concat("<pre>")
                .concat(String.join("", Collections.nCopies(88, "\u2501")))
                .concat("</pre>").concat("\n");

        for (CSUpdateData data : syncList) {
            String entityName = data.getEntityName() == null
                    ? "null"
                    : data.getEntityName();
            String nameAwt = data.getRecipientName() == null
                    ? data.getRecipientNumber()
                    : data.getRecipientName();
            String datePull = data.getDatePull() == null
                    ? "null"
                    : sdf.format(data.getDatePull());
            String datePush = data.getDatePush() == null
                    ? "null"
                    : sdf.format(data.getDatePush());
            String status = data.getUpdateDataStatus() == null
                    ? "null"
                    : data.getUpdateDataStatus();

            result = result.concat("<pre>")
                    .concat(editRowWithSpace(entityName, 1, 18))
                    .concat(editRowWithSpace(nameAwt, 0, 22))
                    .concat(editRowWithSpace(datePull, 0, 22))
                    .concat(editRowWithSpace(datePush, 0, 22))
                    .concat(editRowWithSpacelastColumn(status, 1, 8))
                    .concat("</pre>").concat("\n");
        }
        result = result.concat("<pre>").concat(String.join("", Collections.nCopies(88, "\u2501")))
                .concat("\n").concat("страница " + listCount + " </pre>");
        return result;
    }

    @Override
    public String getAllAwt(int startLimit, int count, int listCount) {
        String result = " ";
        String queryStr = "SELECT DISTINCT u.recipientNumber as recipientNumber, s.name as recipientName FROM TUPDATEDATA u \n" +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber\n" +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID\n" +
                "WHERE s.NAME is not null order by s.NAME ASC LIMIT :startLimit, :count";
        List<AwtList> syncList = new ArrayList<>();
        List<Object[]> resultList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultList = openSession.createNativeQuery(queryStr)
                    .setParameter("startLimit", startLimit)
                    .setParameter("count", count)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();

            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach((p) -> {
                syncList.add(AwtList.builder()
                        .recipientNumber((String) p[0])
                        .recipientName((String) p[1])
                        .build());
            });
        }

        if (syncList.isEmpty()) {
            return "Список пуст";
        }
        result = result.concat("<pre>")
                .concat(" Список вокзалов").concat("\n")
                .concat(String.join("", Collections.nCopies(31, "\u2501"))).concat("\n")
                .concat(editRowWithSpace("код", 1, 8))
                .concat(editRowWithSpacelastColumn("название", 1, 25))
                .concat("</pre>").concat("\n")
                .concat("<pre>")
                .concat(String.join("", Collections.nCopies(31, "\u2501")))
                .concat("</pre>").concat("\n");

        for (AwtList data : syncList) {

            String codeAwt = data.getRecipientNumber() == null
                    ? "null"
                    : data.getRecipientNumber();
            String nameAwt = data.getRecipientName() == null
                    ? "null"
                    : data.getRecipientName();

            result = result.concat("<pre>")
                    .concat(editRowWithSpace(codeAwt, 1, 8))
                    .concat(editRowWithSpacelastColumn(nameAwt, 1, 25))
                    .concat("</pre>").concat("\n");
        }
        result = result.concat("<pre>").concat(String.join("", Collections.nCopies(31, "\u2501")))
                .concat("\n").concat("страница " + listCount + " </pre>");
        return result;
    }

    @Override
    public String getOnlyNamesAllLoadedAwt() {
        String result = "";

        String queryPull = "SELECT DISTINCT u.recipientNumber as recipientNumber, s.name as recipientName FROM TUPDATEDATA u \n" +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber\n" +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID\n" +
                "WHERE u.UPDATE_STATUS = 'Pull' group by u.recipientNumber order by s.name asc";
        List<AwtList> pullList = new ArrayList<>();
        List<Object[]> resultPullList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultPullList = openSession.createNativeQuery(queryPull)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultPullList != null && !resultPullList.isEmpty()) {
            resultPullList.forEach((p) -> {
                pullList.add(AwtList.builder()
                        .recipientNumber((String) p[0])
                        .recipientName((String) p[1])
                        .build());
            });
        }

        String queryPush = "SELECT DISTINCT u.recipientNumber as recipientNumber, s.name as recipientName FROM TUPDATEDATA u " +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber " +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID " +
                "WHERE u.UPDATE_STATUS = 'Push' AND u.DT_PUSH < current_date group by u.recipientNumber order by s.name asc";
        List<AwtList> pushList = new ArrayList<>();
        List<Object[]> resultPushList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultPushList = openSession.createNativeQuery(queryPush)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultPushList != null && !resultPushList.isEmpty()) {
            resultPushList.forEach((p) -> {
                pushList.add(AwtList.builder()
                        .recipientNumber((String) p[0])
                        .recipientName((String) p[1])
                        .build());
            });
        }

        //список кодов с ошибками синхронизации
        List<String> numberPullList = pullList.stream().map(AwtList::getRecipientNumber).collect(Collectors.toList());
        //список кодов у которых дата пуша != сегодняшнему дню (могут быть названия с ошибками синхронизации)
        List<String> numberPushList = pushList.stream().map(AwtList::getRecipientNumber).collect(Collectors.toList());
        //список кодов которые нужно удалить из numberPushList
        List<String> numberDeleteFromPushList = numberPushList.stream().filter((n) -> !numberPullList.contains(n)).collect(Collectors.toList());
        //список кодов у которых дата пуша != сегодняшнему дню
        List<AwtList> mergeList = pushList.stream().filter((n) -> numberDeleteFromPushList.contains(n.getRecipientNumber())).collect(Collectors.toList());

        result = result.concat("<b> Список вокзалов со статусом 'Push' которые сегодня не обновлялись. </b> <pre>").concat("\n");

        for (AwtList list : mergeList) {
            String name = list.getRecipientName() == null
                    ? "" : list.getRecipientName();
            String number = list.getRecipientNumber() == null
                    ? "" : list.getRecipientNumber();
            if (list.getRecipientName() != null) {
                result = result.concat(number).concat(" " + name + ", ");
            }
        }
        result = result.substring(0, result.length() - 2).concat(".</pre> \n");

        return result;
    }

    @Override
    public String getOnlyNamesAllLoadedTerminals() {

        String result = "";

        String queryPull = "SELECT DISTINCT u.recipientNumber as recipientNumber, t.IDATP as idAtp, atp.NAME as nameAtp FROM TUPDATEDATA u \n" +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber\n" +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID\n" +
                "LEFT JOIN TTERMINALS t ON t.NAME = u.recipientNumber\n" +
                "LEFT JOIN TATPLIST atp ON atp.ID = t.IDATP\n" +
                "WHERE u.UPDATE_STATUS = 'Pull' AND s.NAME is NULL order by t.IDATP asc";
        List<CSTerminalDto> pullList = new ArrayList<>();
        List<Object[]> resultPullList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultPullList = openSession.createNativeQuery(queryPull)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("idAtp", StandardBasicTypes.INTEGER)
                    .addScalar("nameAtp", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultPullList != null && !resultPullList.isEmpty()) {
            resultPullList.forEach((p) -> {
                pullList.add(CSTerminalDto.builder()
                        .recipientNumber((String) p[0])
                        .idAtp((Integer) p[1])
                        .nameAtp((String) p[2])
                        .build());
            });
        }

        String queryPush = "SELECT DISTINCT u.recipientNumber as recipientNumber, t.IDATP as idAtp, atp.NAME as nameAtp FROM TUPDATEDATA u \n" +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber\n" +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID\n" +
                "LEFT JOIN TTERMINALS t ON t.NAME = u.recipientNumber\n" +
                "LEFT JOIN TATPLIST atp ON atp.ID = t.IDATP\n" +
                "WHERE u.UPDATE_STATUS = 'Push' AND u.DT_PUSH < CURRENT_DATE  AND s.NAME is NULL order by t.IDATP asc";
        List<CSTerminalDto> pushList = new ArrayList<>();
        List<Object[]> resultPushList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultPushList = openSession.createNativeQuery(queryPush)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("idAtp", StandardBasicTypes.INTEGER)
                    .addScalar("nameAtp", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultPushList != null && !resultPushList.isEmpty()) {
            resultPushList.forEach((p) -> {
                pushList.add(CSTerminalDto.builder()
                        .recipientNumber((String) p[0])
                        .idAtp((Integer) p[1])
                        .nameAtp((String) p[2])
                        .build());
            });
        }

        //список кодов с ошибками синхронизации
        List<String> numberPullList = pullList.stream().map(CSTerminalDto::getRecipientNumber).collect(Collectors.toList());
        //список кодов у которых дата пуша != сегодняшнему дню (могут быть названия с ошибками синхронизации)
        List<String> numberPushList = pushList.stream().map(CSTerminalDto::getRecipientNumber).collect(Collectors.toList());
        //список кодов которые нужно удалить из numberPushList
        List<String> numberDeleteFromPushList = numberPushList.stream().filter((n) -> !numberPullList.contains(n)).collect(Collectors.toList());
        //список кодов у которых дата пуша != сегодняшнему дню
        List<CSTerminalDto> mergeList = pushList.stream().filter((n) -> numberDeleteFromPushList.contains(n.getRecipientNumber())).collect(Collectors.toList());

        result = result.concat("<b> Список терминалов со статусом 'Push' которые сегодня не обновлялись. </b> <pre>").concat("\n");

        for (int i = 0; i < mergeList.size(); i++) {
            String number = mergeList.get(i).getRecipientNumber() == null
                    ? "" : mergeList.get(i).getRecipientNumber();
            Integer idAtp = mergeList.get(i).getIdAtp() == null
                    ? 0 : mergeList.get(i).getIdAtp();
            String nameAtp = mergeList.get(i).getNameAtp() == null
                    ? "Нет названия" : mergeList.get(i).getNameAtp();
            if (i == 0) {
                result = result.concat(nameAtp + " ( " + number + ", ");
            } else if (i != 0 && idAtp.equals(mergeList.get(i - 1).getIdAtp())) {
                result = result.concat(number + ", ");
            } else if (i != 0 && !idAtp.equals(mergeList.get(i - 1).getIdAtp())) {
                result = result.substring(0, result.length() - 2).concat(") " + nameAtp + " ( " + number + ", ");
            }
            log.debug("RESULT " + result);
        }
        result = result.substring(0, result.length() - 2).concat(" ).</pre> \n");
        return result;
    }

    @Override
    public String getOnlyNamesAllNotLoadedTerminals() {
        String result = "";

        String queryPull = "SELECT DISTINCT u.recipientNumber as recipientNumber, t.IDATP as idAtp, atp.NAME as nameAtp FROM TUPDATEDATA u \n" +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber\n" +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID\n" +
                "LEFT JOIN TTERMINALS t ON t.NAME = u.recipientNumber\n" +
                "LEFT JOIN TATPLIST atp ON atp.ID = t.IDATP\n" +
                "WHERE u.UPDATE_STATUS = 'Pull' AND s.NAME is NULL order by t.IDATP asc";
        List<CSTerminalDto> pullList = new ArrayList<>();
        List<Object[]> resultPullList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultPullList = openSession.createNativeQuery(queryPull)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("idAtp", StandardBasicTypes.INTEGER)
                    .addScalar("nameAtp", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultPullList != null && !resultPullList.isEmpty()) {
            resultPullList.forEach((p) -> {
                pullList.add(CSTerminalDto.builder()
                        .recipientNumber((String) p[0])
                        .idAtp((Integer) p[1])
                        .nameAtp((String) p[2])
                        .build());
            });
        }

        result = result.concat("<b> Список терминалов на которых есть ошибки синхронизации. </b> <pre>").concat("\n");

        for (int i = 0; i < pullList.size(); i++) {
            String number = pullList.get(i).getRecipientNumber() == null
                    ? "" : pullList.get(i).getRecipientNumber();
            Integer idAtp = pullList.get(i).getIdAtp() == null
                    ? 0 : pullList.get(i).getIdAtp();
            String nameAtp = pullList.get(i).getNameAtp() == null
                    ? "Нет названия" : pullList.get(i).getNameAtp();
            if (i == 0) {
                result = result.concat(nameAtp + " ( " + number + ", ");
            } else if (i != 0 && idAtp.equals(pullList.get(i - 1).getIdAtp())) {
                result = result.concat(number + ", ");
            } else if (i != 0 && !idAtp.equals(pullList.get(i - 1).getIdAtp())) {
                result = result.substring(0, result.length() - 2).concat(") " + nameAtp + " ( " + number + ", ");
            }
        }
        result = result.substring(0, result.length() - 2).concat(" ).</pre> \n");
        return result;
    }

    @Override
    public String getOnlyNamesAllNotLoadedAwt() {

        String result = "";

        String queryPull = "SELECT DISTINCT u.recipientNumber as recipientNumber, s.name as recipientName FROM TUPDATEDATA u \n" +
                "LEFT JOIN TAWTSTATIONS a ON a.CSAwt_CODE = u.recipientNumber\n" +
                "LEFT JOIN TSTOPPLACES s ON s.ID = a.stopplaces_ID\n" +
                "WHERE u.UPDATE_STATUS = 'Pull' group by u.recipientNumber order by s.name asc";
        List<AwtList> pullList = new ArrayList<>();
        List<Object[]> resultPullList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultPullList = openSession.createNativeQuery(queryPull)
                    .addScalar("recipientNumber", StandardBasicTypes.STRING)
                    .addScalar("recipientName", StandardBasicTypes.STRING)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultPullList != null && !resultPullList.isEmpty()) {
            resultPullList.forEach((p) -> {
                pullList.add(AwtList.builder()
                        .recipientNumber((String) p[0])
                        .recipientName((String) p[1])
                        .build());
            });
        }

        result = result.concat("<b> Список вокзалов на которых есть ошибки синхронизации. </b> <pre>").concat("\n");

        for (AwtList list : pullList) {
            String name = list.getRecipientName() == null
                    ? "" : list.getRecipientName();
            String number = list.getRecipientNumber() == null
                    ? "" : list.getRecipientNumber();
            if (list.getRecipientName() != null) {
                result = result.concat(number).concat(" " + name + ", ");
            }
        }
        result = result.substring(0, result.length() - 2).concat(".</pre> \n");

        return result;
    }

    @Override
    public String setNullOneRow(String code, String nameEntity) {
        String queryPull = "UPDATE TUPDATEDATA u SET u.DT_PUSH = null WHERE u.recipientNumber = :code AND u.entityName = :entityName";
        int resultUpdate = 0;
        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultUpdate = openSession.createNativeQuery(queryPull)
                    .setParameter("code", code)
                    .setParameter("entityName", nameEntity)
                    .executeUpdate();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        if (resultUpdate == 1) {
            return "Успешно";
        } else {
            return "Неуспешно";
        }
    }

    @Override
    public String setNullAllRows(String code) {
        String queryPull = "UPDATE TUPDATEDATA u SET u.DT_PUSH = null WHERE u.recipientNumber = :code";
        int resultUpdate = 0;
        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultUpdate = openSession.createNativeQuery(queryPull)
                    .setParameter("code", code)
                    .executeUpdate();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        if (resultUpdate == 1) {
            return "Успешно";
        } else {
            return "Неуспешно";
        }
    }
}
