package ru.avperm.TelegramSyncBotApi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avperm.TelegramSyncBotApi.config.TelegramBotSettings;
import ru.avperm.TelegramSyncBotApi.model.TelegramUsers;
import ru.avperm.TelegramSyncBotApi.service.CSUpdateDataService;
import ru.avperm.TelegramSyncBotApi.service.LoginNotificationService;
import ru.avperm.TelegramSyncBotApi.service.TelegramUserService;

import java.text.SimpleDateFormat;
import java.util.*;

import static ru.avperm.TelegramSyncBotApi.util.MainUtil.countList;
import static ru.avperm.TelegramSyncBotApi.util.TelegramUtil.*;

@Component
@Slf4j
public class TelegramBotController extends TelegramLongPollingBot {

    private final static String ERROR_ANSWER_WRONG_COMMAND = ", такой команды не существует. Список команд можно посмотреть через / или набрать /help";

    private final TelegramBotSettings telegramBotSettings;
    private final CSUpdateDataService updateDataService;
    private final TelegramUserService telegramUserService;
    private final LoginNotificationService loginNotificationService;

    public TelegramBotController(TelegramBotSettings telegramBotSettings, CSUpdateDataService updateDataService,
                                 TelegramUserService telegramUserService, LoginNotificationService loginNotificationService) {
        this.telegramBotSettings = telegramBotSettings;
        this.updateDataService = updateDataService;
        this.telegramUserService = telegramUserService;
        this.loginNotificationService = loginNotificationService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            SendMessage message;
            if (update.hasCallbackQuery()) {
                message = getCallBackQueryAndReturnMessage(update);
            } else {
                message = getCommandAndReturnMessage(update);
            }
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage getCallBackQueryAndReturnMessage(Update update) {
        String command = update.getCallbackQuery().getData().substring(update.getCallbackQuery().getData().indexOf(" "));
        switch (command) {
            case "code":
                return reloadEntityFromUpdateData(update);
            default:
                return getInfoAboutUpdateDataTable(update);
        }
    }

    private SendMessage reloadEntityFromUpdateData(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String codeEntity = callbackData.split(" ")[1];
        String nameEntity = callbackData.split(" ")[2];

        if (nameEntity.equalsIgnoreCase("all")) {
            updateDataService.reloadAllEntitiesFromUpdateData(codeEntity);
        } else {
            updateDataService.reloadOneEntityFromUpdateData(codeEntity, nameEntity);
        }
        return createSimpleMessageFromCallBackQuery(update, "Готово");
    }

    private SendMessage getInfoAboutUpdateDataTable(Update update) {
        String callbackData = update.getCallbackQuery().getData(); //Ответ
        String callBackDataNumber = callbackData.split(" ")[0]; // число записей в ответе
        String callBackDataString = callbackData.split(" ")[1]; // вид запроса (ok,er, aw)
        Integer callbackCount = Integer.parseInt(callBackDataNumber);//
        int nextStartLimit = Integer.parseInt(callBackDataNumber) + 25; // след страница с +25 элемента
        String nextCallBackData = nextStartLimit + " " + callBackDataString;
        int listCount = countList(callbackCount, nextStartLimit); //счетчик страниц
        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboardMarkup("Следующая страница", nextCallBackData);
        String text = "";
        switch (callBackDataString.toLowerCase()) {
            case "ok":
                text = updateDataService.getAllLoaded(nextStartLimit, 25, listCount);
                break;
            case "er":
                text = updateDataService.getAllNotLoaded(nextStartLimit, 25, listCount);
                break;
            case "aw":
                text = updateDataService.getAllAwt(nextStartLimit, 25, listCount);
                break;
        }
        SendMessage message;
        if (text.equalsIgnoreCase("Список пуст")) {
            message = createSimpleMessageFromCallBackQuery(update, "Конец списка");
        } else {
            message = createHtmlMessageWithInlineKeyboardFromCallBackQuery(update, text, inlineKeyboardMarkup);
        }
        return message;
    }


    private SendMessage getCommandAndReturnMessage(Update update) {
        String text = (update.hasMessage() ? (update.getMessage().hasText() ? update.getMessage().getText() : "Сообщение пустое") : "Нет сообщений");
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getFirstName();
        SendMessage message = new SendMessage();
        if (checkAuthUser(chatId)) {
            if (text.contains("/")) {
                message = analiseCommand(update, text);
            } else {
                message.setChatId(chatId).setText(userName + ", введите команду. Список команд можно посмотреть через / или набрать /help");
            }
        } else {
            message = requestPasswordAndAddNewUser(update, chatId, text);
        }
        return message;
    }

    private SendMessage requestPasswordAndAddNewUser(Update update, Long chatId, String text) {
        String firstName = update.getMessage().getChat().getFirstName();
        String lastName = update.getMessage().getChat().getLastName();
        String userName = update.getMessage().getChat().getUserName();

        SendMessage sendMessage = new SendMessage();
        if (checkPassword(text) || checkAuthUser(chatId)) {
            if (!telegramUserService.userExist(chatId)) {
                telegramUserService.saveUser(chatId, firstName, lastName, userName);
                sendMessage.setChatId(chatId).setText("Бот запущен.\nВведите команду. Список команд можно посмотреть через /");
            }
        } else {
            sendMessage.setChatId(chatId).setText(firstName + ", для запуска бота введите пароль.");
        }
        return sendMessage;
    }

    private boolean checkAuthUser(Long chatId) {
        TelegramUsers user = telegramUserService.findUserByChatId(chatId);
        return user != null;
    }

    private boolean checkPassword(String text) {
        return text.equalsIgnoreCase(telegramBotSettings.getPassword());
    }

    private SendMessage analiseCommand(Update update, String text) {
        String userName = update.getMessage().getChat().getUserName();
        String code = "";
        String command = text.split("/")[1];
        if (text.contains(" ")) {
            code = text.split("/")[1].split(" ")[1];
            command = command.split(" ")[0];
        }
        switch (command) {
            case "list":
                if (code.isEmpty()) {
                    InlineKeyboardMarkup inlineKeyboardMarkupAwtList = createInlineKeyboardMarkup("Следующая страница",
                            "0 aw");
                    return createHtmlMessageWithInlineKeyboard(update, updateDataService.getAllAwt(0, 25, 1), inlineKeyboardMarkupAwtList);
                } else {
                    String message = userName + ERROR_ANSWER_WRONG_COMMAND;
                    return createSimpleMessage(update, message);
                }
            case "code":
                Map<String, String> codeMap = updateDataService.getAllByCode(code);
                String msgText = "";
                String entityNames = "";
                for (Map.Entry<String, String> entry : codeMap.entrySet()) {
                    msgText = entry.getKey();
                    entityNames = entry.getValue();
                }
                if (msgText.equalsIgnoreCase("Список пуст")) {
                    return createSimpleMessage(update, msgText);
                } else {
                    String[] entitiesArray = entityNames.split(",");

                    InlineKeyboardMarkup inlineKeyboardMarkupCode = new InlineKeyboardMarkup();
                    List<String> listEntites = new ArrayList<>(Arrays.asList(entitiesArray));
                    List<List<InlineKeyboardButton>> rowListCode = new ArrayList<>();
                    int countOfCycle = 8;
                    do {
                        List<InlineKeyboardButton> keyboardButtonsRow1Code = new ArrayList<>();
                        if (listEntites.size() < 8) {
                            countOfCycle = listEntites.size();
                        }
                        for (int i = 0; i < countOfCycle; i++) {
                            keyboardButtonsRow1Code.add(new InlineKeyboardButton().setText(listEntites.get(0))
                                    .setCallbackData("code " + code + " " + listEntites.get(0)));
                            listEntites.remove(0);
                        }
                        rowListCode.add(keyboardButtonsRow1Code);
                    } while (!listEntites.isEmpty());

                    List<InlineKeyboardButton> keyboardButtonsRow2Code = new ArrayList<>();
                    keyboardButtonsRow2Code.add(new InlineKeyboardButton().setText("Выгрузить все справочники")
                            .setCallbackData("1 " + code + " all"));
                    rowListCode.add(keyboardButtonsRow2Code);
                    inlineKeyboardMarkupCode.setKeyboard(rowListCode);
                    return createHtmlMessageWithInlineKeyboard(update, msgText, inlineKeyboardMarkupCode);
                }
            case "status":
                if (code.equalsIgnoreCase("er")) {
                    InlineKeyboardMarkup inlineKeyboardMarkupError = createInlineKeyboardMarkup("Следующая страница", "0 er");
                    String message = updateDataService.getAllNotLoaded(0, 25, 1);
                    return createHtmlMessageWithInlineKeyboard(update, message, inlineKeyboardMarkupError);

                } else if (code.equalsIgnoreCase("ok")) {
                    InlineKeyboardMarkup inlineKeyboardMarkupOk = createInlineKeyboardMarkup("Следующая страница", "0 ok");
                    String message = updateDataService.getAllLoaded(0, 25, 1);
                    return createHtmlMessageWithInlineKeyboard(update, message, inlineKeyboardMarkupOk);

                } else {
                    String message = userName + ERROR_ANSWER_WRONG_COMMAND;
                    return createSimpleMessage(update, message);
                }
            case "help":
                if (code.isEmpty()) {
                    String help = "<pre>\\list</pre>\n    Выводит список вокзалов (код и наименование).\n\n" +
                            "<pre>\\code {код вокзала}</pre>\n    Выводит список синхронизации справочников по коду вокзала (Например code 111). " +
                            "Под таблицей можно выбрать справочник для повторной загрузки, для этого необходимо нажать на кнопку с названием справочника, " +
                            "так же можно повторно загрузить все справочники сразу нажав на кнопку \"Выгрузить все справочники\".\n\n" +
                            "<pre>\\status ok</pre>\n    Выводит список всех успешно выгруженных справочников.\n\n" +
                            "<pre>\\status er</pre>\n    Выводит список всех справочников у которых произошла ошибка во время синхронизации.";
                    return createHtmlMessage(update, help);
                } else {
                    String message = userName + ERROR_ANSWER_WRONG_COMMAND;
                    return createSimpleMessage(update, message);
                }
            default:
                String message = userName + ERROR_ANSWER_WRONG_COMMAND;
                return createSimpleMessage(update, message);
        }
    }


    @Override
    public String getBotUsername() {
        return telegramBotSettings.getBotName();
    }

    @Override
    public String getBotToken() {
        return telegramBotSettings.getToken();
    }


    @Scheduled(cron = "0 43 9,14 * * ?")
    public void duplicationPrimaryRacesNotification() {
        String chatId = "@avpermrusync";

        String text = loginNotificationService.duplicationCheckPrimaryRaces();
        if (text != null) {
            SendMessage message = createHtmlMessageForSchedule(chatId, text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

 /*   @Scheduled(cron = "0 * * * * ?")
    public void smartCardDataBaseNotification() {
        String chatId = "@avpermrusync";
        String text = loginNotificationService.checkNewNotification();
        if (text != null) {
            SendMessage message = createHtmlMessageForSchedule(chatId, text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }*/

    @Scheduled(cron = "0 40 9,14 * * ?")
    public void channelNotification() {
        String chatId = "@avpermrusync";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String text =
                "Отчет по синхронизациям за " + sdf.format(new Date())
                        + "\n"
                        + updateDataService.getOnlyNamesAllLoadedAwt()
                        + "\n"
                        + updateDataService.getOnlyNamesAllNotLoadedAwt()
                        + "\n"
                        + updateDataService.getOnlyNamesAllNotLoadedTerminals();

        SendMessage message = createHtmlMessageForSchedule(chatId, text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
