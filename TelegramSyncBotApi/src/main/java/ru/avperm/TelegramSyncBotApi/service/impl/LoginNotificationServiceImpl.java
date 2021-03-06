package ru.avperm.TelegramSyncBotApi.service.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.avperm.TelegramSyncBotApi.model.LoginNotification;
import ru.avperm.TelegramSyncBotApi.model.PrimaryRace;
import ru.avperm.TelegramSyncBotApi.service.LoginNotificationService;

import javax.persistence.EntityManagerFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
                    .concat("<b> ?????????????????????? ???? SmartCard </b>\n")
                    .concat("????????: <pre> " + dt + "</pre>\n")
                    .concat("??????????????????: <pre> " + header + " </pre>\n")
                    .concat("??????????: <pre> " + text + " </pre>\n\n");


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

    @Override
    public String duplicationCheckPrimaryRaces() {

        String result = "";
        String queryStr = "SELECT DTSTART as dateStart, IDROUTE as idRoute, IDATP as idAtp, COUNT(*) AS cnt " +
                "FROM TPRIMARYRACES " +
                "WHERE DTSTART>'2021-01-01 00:00:00' " +
                "GROUP BY DTSTART, IDROUTE, IDATP " +
                "HAVING cnt>1";
        List<PrimaryRace> syncList = new ArrayList<>();
        List<Object[]> resultList = null;

        try (
                Session openSession = entityManagerFactory.unwrap(SessionFactory.class).openSession()) {

            openSession.getTransaction().begin();
            resultList = openSession.createNativeQuery(queryStr)
                    .addScalar("dateStart", StandardBasicTypes.DATE)
                    .addScalar("idRoute", StandardBasicTypes.LONG)
                    .addScalar("idAtp", StandardBasicTypes.INTEGER)
                    .getResultList();
            openSession.getTransaction().commit();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach((p) -> {
                syncList.add(PrimaryRace.builder()
                        .dateStart((Date) p[0])
                        .idRoute((Long) p[1])
                        .idAtp((Integer) p[2])
                        .build());
            });
        }

        if (syncList.isEmpty()) {
            return null;
        }

        for (PrimaryRace data : syncList) {
            String date = data.getDateStart() == null
                    ? "null"
                    : data.getDateStart().toString();
            String idRoute = data.getIdRoute() == null
                    ? "null"
                    : data.getIdRoute().toString();
            String idAtp = data.getIdAtp() == null
                    ? "null"
                    : data.getIdAtp().toString();

            result = result
                    .concat("<b> ?????????????????????? ???? SmartCard </b>\n")
                    .concat("<b> ?????????? ?????????????? ???????????? </b>\n")
                    .concat("???????? ??????????: <pre> " + date + "</pre>\n")
                    .concat("??????????????: <pre> " + idRoute + " </pre>\n")
                    .concat("??????: <pre> " + idAtp + " </pre>\n\n");
        }
        return result;
    }

}
