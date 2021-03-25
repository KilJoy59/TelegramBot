package ru.avperm.TelegramSyncBotApi.util;

public class MainUtil {

    public static int countList(Integer callbackCount, Integer nextStartLimit) {
        int count = 1;
        if (callbackCount == 0) {
            count = 2;
        } else {
            int a = 0;
            int b = nextStartLimit;
            while (a != b) {
                a = a + 25;
                count++;
            }
        }
        return count;
    }
}
