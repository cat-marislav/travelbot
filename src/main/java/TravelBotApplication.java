import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AirlineData;
import dto.BotCredentials;
import dto.RequestDto;
import org.apache.shiro.session.Session;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot;
import service.MessageService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class TravelBotApplication extends TelegramLongPollingSessionBot {

    private final BotCredentials botCredentials;

    public TravelBotApplication() throws IOException {
        botCredentials = new ObjectMapper().readValue(new File("src/main/resources/bot-credentials.json"), BotCredentials.class);
    }

    public static void main(String[] args) throws IOException {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TravelBotApplication());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage()
                .enableMarkdown(true)
                .setChatId(message.getChatId())
                .setText(text);
        try {
            if ("/start".equals(message.getText())) {
                setButtons(sendMessage);
            }
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();

        keyboardFirstRow.add(new KeyboardButton("1"));
        keyboardFirstRow.add(new KeyboardButton("2"));
        keyboardFirstRow.add(new KeyboardButton("3"));

        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }


    @Override
    public void onUpdateReceived(Update update, Optional<Session> botSession) {

        final Message message = update.getMessage();
        try {
            if ("/start".equals(message.getText())) {
                final String startMessage = "Привет, " + update.getMessage().getFrom().getFirstName() + ", выбери один из вариантов поиска: " + "\n" +
                        "1. Из аэропорта в аэропорт с выбором дат вылета и возвращения" + "\n" +
                        "2. Из аэропорта в аэропорт без выбора дат" + "\n" +
                        "3. Из аэропорта без выбора дат" + "\n" +
                        "Если хочешь начать заново введи /start";
                botSession.ifPresent(session -> {
                    //session.setAttribute("language",update.getMessage().getFrom().getLanguageCode());
                    session.setAttribute("question_number", 0);
                    session.setAttribute("request_dto", null);
                });
                sendMsg(message, startMessage);
            } else {
                botSession.ifPresent(session -> {
                    session.setAttribute("request_dto", session.getAttribute("request_dto") == null ? new RequestDto() : session.getAttribute("request_dto"));
                    RequestDto requestDto = (RequestDto) session.getAttribute("request_dto");

                    session.setAttribute("handler", session.getAttribute("handler") == null ? new MessageService() : session.getAttribute("handler"));
                    MessageService handler = (MessageService) session.getAttribute("handler");

                    final Integer questionNumber = (Integer) session.getAttribute("question_number");

                    final String toSend = handler.askQuestion(questionNumber, message.getText(), requestDto);
                    sendMsg(message, toSend);
                    if (handler.isCorrectAnswer() && questionNumber <= 4) {
                        if (handler.isFirstVar()) {
                            session.setAttribute("question_number", questionNumber + 1);
                        } else {
                            session.setAttribute("question_number", 5);
                        }
                    }
                });
            }
        } catch (Exception e) {
            sendMsg(message, "Произошла ошибка. Нажмите /start!");
        }
    }

    @Override
    public String getBotUsername() {
        return botCredentials.name;
    }

    @Override
    public String getBotToken() {
        return botCredentials.token;
    }
}
