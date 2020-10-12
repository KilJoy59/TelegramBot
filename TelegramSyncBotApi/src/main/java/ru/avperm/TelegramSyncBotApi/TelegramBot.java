package ru.avperm.TelegramSyncBotApi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avperm.TelegramSyncBotApi.model.TelegramUsers;
import ru.avperm.TelegramSyncBotApi.service.CSUpdateDataService;
import ru.avperm.TelegramSyncBotApi.service.TelegramUserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Project SyncBotApi
 * Created by End on окт., 2020
 */
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${token}")
    private String token;
    @Value("${botName}")
    private String botName;
    @Value("${password}")
    private String password;

    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private CSUpdateDataService updateDataService;


    public TelegramBot(TelegramUserService telegramUserService, CSUpdateDataService updateDataService) {

        this.telegramUserService = telegramUserService;
        this.updateDataService = updateDataService;
    }

    public String getBotToken() {
        return token;
    }

    public String getBotUsername() {
        return botName;
    }

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                if (update.getCallbackQuery().getData().startsWith("code")) {
                    String callbackData = update.getCallbackQuery().getData();
                    String codeEntity = callbackData.split(" ")[1];
                    String nameEntity = callbackData.split(" ")[2];

                    if (nameEntity.equalsIgnoreCase("all")) {
                        updateDataService.setNullAllRows(codeEntity);
                    } else {
                        updateDataService.setNullOneRow(codeEntity, nameEntity);
                    }
                    SendMessage message = new SendMessage();
                    message.setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setText("Готово");
                    execute(message);
                } else {

                    String callbackData = update.getCallbackQuery().getData();
                    String callBackDataNumber = callbackData.split(" ")[0];
                    String callBackDataString = callbackData.split(" ")[1];
                    Integer callbackCount = Integer.parseInt(callBackDataNumber);  // 0  25 50
                    Integer nextStartLimit = Integer.parseInt(callBackDataNumber) + 25; // 25 50 75
                    String nextCallBackData = nextStartLimit.toString() + " " + callBackDataString; //25 50 75
                    int listCount = countList(callbackCount, nextStartLimit);
                    execute(sendInlineKeyBoardMessage(update.getCallbackQuery().getMessage().getChatId(), nextStartLimit, nextCallBackData, listCount, callBackDataString));

                }
            } else {
                sendMessage(update);
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    private synchronized void sendMessage(Update update) throws TelegramApiException {
        String text = (update.hasMessage() ? (update.getMessage().hasText() ? update.getMessage().getText() : "Сообщение пустое") : "Нет сообщений");
        long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getChat().getFirstName();

        SendMessage message = new SendMessage();
        if (checkUser(chatId)) {
            if (text.contains("/")) {
                String code = "";

                String command = text.split("/")[1];
                if (text.contains(" ")) {
                    code = text.split("/")[1].split(" ")[1];
                    command = command.split(" ")[0];
                }
                switch (command) {
                    case "list":
                        if (code.isEmpty()) {
                            InlineKeyboardMarkup inlineKeyboardMarkupAwtList = new InlineKeyboardMarkup();
                            List<InlineKeyboardButton> keyboardButtonsRow1AwtList = new ArrayList<>();
                            keyboardButtonsRow1AwtList.add(new InlineKeyboardButton().setText("Следующая страница")
                                    .setCallbackData("0 aw"));
                            List<List<InlineKeyboardButton>> rowListAwtList = new ArrayList<>();
                            rowListAwtList.add(keyboardButtonsRow1AwtList);
                            inlineKeyboardMarkupAwtList.setKeyboard(rowListAwtList);

                            message.setChatId(chatId).setText(updateDataService.getAllAwt(0, 25, 1))
                                    .enableHtml(true)
                                    .setParseMode(ParseMode.HTML)
                                    .enableWebPagePreview()
                                    .setReplyMarkup(inlineKeyboardMarkupAwtList);
                        } else {
                            message.setChatId(chatId).setText(firstName + ", такой команды не существует. Список команд можно посмотреть через / или набрать /help");
                        }
                        break;
                    case "code":
                        Map<String, String> codeMap = updateDataService.getAllByCode(code);
                        String msgText = "";
                        String entityNames = "";
                        for (Map.Entry<String, String> entry : codeMap.entrySet()) {
                            msgText = entry.getKey();
                            entityNames = entry.getValue();
                        }
                        if (msgText.equalsIgnoreCase("Список пуст")) {
                            message.setChatId(chatId).setText(msgText);
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

                            message.setChatId(chatId).setText(msgText)
                                    .enableHtml(true)
                                    .setParseMode(ParseMode.HTML)
                                    .enableWebPagePreview()
                                    .setReplyMarkup(inlineKeyboardMarkupCode);
                        }
                        break;
                    case "status":
                        if (code.equalsIgnoreCase("er")) {

                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
                            keyboardButtonsRow1.add(new InlineKeyboardButton().setText("Следующая страница")
                                    .setCallbackData("0 er"));
                            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                            rowList.add(keyboardButtonsRow1);
                            inlineKeyboardMarkup.setKeyboard(rowList);

                            message.setChatId(chatId).setText(updateDataService.getAllNotLoaded(0, 25, 1))
                                    .enableHtml(true)
                                    .setParseMode(ParseMode.HTML)
                                    .enableWebPagePreview()
                                    .setReplyMarkup(inlineKeyboardMarkup);

                        } else if (code.equalsIgnoreCase("ok")) {

                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
                            keyboardButtonsRow1.add(new InlineKeyboardButton().setText("Следующая страница")
                                    .setCallbackData("0 ok"));
                            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                            rowList.add(keyboardButtonsRow1);
                            inlineKeyboardMarkup.setKeyboard(rowList);

                            message.setChatId(chatId).setText(updateDataService.getAllLoaded(0, 25, 1))
                                    .enableHtml(true)
                                    .setParseMode(ParseMode.HTML)
                                    .enableWebPagePreview()
                                    .setReplyMarkup(inlineKeyboardMarkup);

                        } else {
                            message.setChatId(chatId).setText(firstName + ", такой команды не существует. Список команд можно посмотреть через / или набрать /help");
                        }
                        break;

                    case "help":
                        if (code.isEmpty()) {
                            String help = "<pre>\\list</pre>\n    Выводит список вокзалов (код и наименование).\n\n" +
                                    "<pre>\\code {код вокзала}</pre>\n    Выводит список синхронизации справочников по коду вокзала (Например code 111). " +
                                    "Под таблицей можно выбрать справочник для повторной загрузки, для этого необходимо нажать на кнопку с названием справочника, " +
                                    "так же можно повторно загрузить все справочники сразу нажав на кнопку \"Выгрузить все справочники\".\n\n" +
                                    "<pre>\\status ok</pre>\n    Выводит список всех успешно выгруженных справочников.\n\n" +
                                    "<pre>\\status er</pre>\n    Выводит список всех справочников у которых произошла ошибка во время синхронизации.";
                            message.setChatId(chatId).setText(help)
                                    .enableHtml(true)
                                    .setParseMode(ParseMode.HTML)
                                    .enableWebPagePreview();
                        } else {
                            message.setChatId(chatId).setText(firstName + ", такой команды не существует. Список команд можно посмотреть через / или набрать /help");

                        }
                        break;
                    default:
                        message.setChatId(chatId).setText(firstName + ", такой команды не существует. Список команд можно посмотреть через / или набрать /help");
                        break;
                }
            } else {
                message.setChatId(chatId).setText(firstName + ", введите команду. Список команд можно посмотреть через / или набрать /help");
            }
        } else {
            message = checkAuth(update, chatId, text);
        }

        execute(message);
    }

    @Scheduled(cron = "0 0 9,14 * * ?")
    public void channelNotification() {
        String chatId = "@avpermrusync";

        SendMessage pushMessage = new SendMessage();
        pushMessage.setChatId(chatId)
                .setText(updateDataService.getOnlyNamesAllLoadedAwt())
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview();
        try {
            execute(pushMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        SendMessage pullMessage = new SendMessage();
        pullMessage.setChatId(chatId)
                .setText(updateDataService.getOnlyNamesAllNotLoadedAwt())
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview();
        try {
            execute(pullMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        SendMessage pushMessageTerminals = new SendMessage();
        pushMessageTerminals.setChatId(chatId)
                .setText(updateDataService.getOnlyNamesAllLoadedTerminals())
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview();
        try {
            execute(pushMessageTerminals);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        SendMessage pullMessageTerminals = new SendMessage();
        pullMessageTerminals.setChatId(chatId)
                .setText(updateDataService.getOnlyNamesAllNotLoadedTerminals())
                .enableHtml(true)
                .setParseMode(ParseMode.HTML)
                .enableWebPagePreview();
        try {
            execute(pullMessageTerminals);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean checkUser(Long chatId) {
        TelegramUsers user = telegramUserService.findUserByChatId(chatId);
        return user != null;
    }

    private boolean checkPassword(String text) {
        return text.equalsIgnoreCase(password);
    }

    private SendMessage checkAuth(Update update, Long chatId, String text) {
        String firstName = update.getMessage().getChat().getFirstName();
        String lastName = update.getMessage().getChat().getLastName();
        String userName = update.getMessage().getChat().getUserName();

        SendMessage sendMessage = new SendMessage();
        if (checkPassword(text) || checkUser(chatId)) {
            if (!telegramUserService.userExist(chatId)) {
                telegramUserService.saveUser(chatId, firstName, lastName, userName);
                sendMessage.setChatId(chatId).setText("Бот запущен.\nВведите команду. Список команд можно посмотреть через /");
            }
        } else {
            sendMessage.setChatId(chatId).setText(firstName + ", для запуска бота введите пароль.");
        }
        return sendMessage;
    }

    public SendMessage sendInlineKeyBoardMessage(long chatId, int nextStartLimit, String callbackData, int listCount, String callBackDataString) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(new InlineKeyboardButton().setText("Следующая страница").setCallbackData(callbackData));
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        String text = "";
        if (callBackDataString.equalsIgnoreCase("ok")) {
            text = updateDataService.getAllLoaded(nextStartLimit, 25, listCount);
        } else if (callBackDataString.equalsIgnoreCase("er")) {
            text = updateDataService.getAllNotLoaded(nextStartLimit, 25, listCount);
        } else if (callBackDataString.equalsIgnoreCase("aw")) {
            text = updateDataService.getAllAwt(nextStartLimit, 25, listCount);
        }
        if (text.equalsIgnoreCase("Список пуст")) {
            return new SendMessage().setChatId(chatId).setText("Конец списка");
        } else {
            return new SendMessage().setChatId(chatId).setText(text).enableHtml(true)
                    .setParseMode(ParseMode.HTML).setReplyMarkup(inlineKeyboardMarkup);
        }
    }

    public int countList(Integer callbackCount, Integer nextStartLimit) {
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
