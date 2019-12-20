package chatbot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    private static Log log = LogFactory.getLog(TelegramBot.class);

    private final String              botUsername;
    private final String              botToken;

    public TelegramBot(String username, String token, DefaultBotOptions botOptions) {
        super(botOptions);
        botUsername = username;
        botToken = token;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            String textMessage = update.getMessage().getText();
            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId(), textMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.info("TelegramApiException", e);
        }
    }
}
