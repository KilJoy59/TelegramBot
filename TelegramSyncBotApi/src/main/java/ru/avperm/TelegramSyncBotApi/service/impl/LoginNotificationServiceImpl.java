package ru.avperm.TelegramSyncBotApi.service.impl;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.avperm.TelegramSyncBotApi.model.CSUpdateData;
import ru.avperm.TelegramSyncBotApi.model.LoginNotification;
import ru.avperm.TelegramSyncBotApi.service.LoginNotificationService;

import javax.management.Notification;
import javax.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Project TelegramSyncBotApi
 * Created by End on окт., 2020
 */

@Service

public class LoginNotificationServiceImpl implements LoginNotificationService {

    @Value("${notificationLogin}")
    private Integer notificationLogin;

    private final EntityManagerFactory entityManagerFactory;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Autowired
    public LoginNotificationServiceImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public String checkNewNotification() {

        String result = "";
        String queryStr = "SELECT l.id as id, l.HEADER as header, l.TEXT as text, l.DT as dt FROM TLOGINNOTIFICATION l \n" +
                "WHERE l.IDLOGIN = :idLogin AND l.ISREAD = 0 ORDER BY DT ASC";
        List<LoginNotification> syncList = new ArrayList<>();
        List<Object[]> resultList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultList = openSession.createNativeQuery(queryStr)
                    .setParameter("idLogin", notificationLogin)
                    .addScalar("id", StandardBasicTypes.LONG)
                    .addScalar("header", StandardBasicTypes.STRING)
                    .addScalar("text", StandardBasicTypes.STRING)
                    .addScalar("dt", StandardBasicTypes.TIMESTAMP)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach((p) -> {
                syncList.add(LoginNotification.builder()
                        .id((Long) p[0])
                        .header((String) p[1])
                        .text((String) p[2])
                        .dt((Date) p[3])
                        .build());
            });
        }

        if (syncList.isEmpty()) {
            return null;
        }

        for (LoginNotification data : syncList) {
            String header = data.getHeader() == null
                    ? "null"
                    : data.getHeader();
            String text = data.getText() == null
                    ? "null"
                    : data.getText();
            String dt = data.getDt() == null
                    ? "null"
                    : sdf.format(data.getDt());

            result = result
                    .concat("<b> Уведомление из SmartCard </b>\n")
                    .concat("Дата: <pre> " + dt + "</pre>\n")
                    .concat("Заголовок: <pre> " + header + " </pre>\n")
                    .concat("Текст: <pre> " + text + " </pre>\n\n");


            String updateQuery = "UPDATE TLOGINNOTIFICATION l SET l.ISREAD = 1 WHERE l.ID = :id";
            int resultUpdate = 0;
            try (
                    Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

                openSession.getTransaction().begin();
                resultUpdate = openSession.createNativeQuery(updateQuery)
                        .setParameter("id", data.getId())
                        .executeUpdate();
                openSession.getTransaction().commit();
            } catch (
                    Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
