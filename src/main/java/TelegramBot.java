import database.DatabaseWorker;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TelegramBot extends TelegramLongPollingBot {
    private static ChatBot chatBot;

    private static String botUsername;
    private static String botToken;

    private final ReplyKeyboardRemove noKeyboard = new ReplyKeyboardRemove();

    protected final String vkShareUrl = "https://vk.com/share.php?url=%s&title=%s&image=%s";

    TelegramBot(String username, String token, DefaultBotOptions botOptions, DatabaseWorker db) {
        super(botOptions);
        chatBot = new ChatBot(username, db);
        botUsername = username;
        botToken = token;
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            ChatBotReply reply;
            if (update.getMessage().hasEntities() && update.getMessage().getEntities().get(0).getType().equals("url")) {
                String content = getFileContent(update.getMessage().getEntities().get(0).getText());
                reply = chatBot.addQuiz(content, update.getMessage().getFrom().getId());
            }
            else if (update.getMessage().hasDocument()) {
                if (update.getMessage().getDocument().getMimeType().equals("application/x-yaml"))
                    try {
                        reply = chatBot.addQuiz(getFileContent(update.getMessage().getDocument()), update.getMessage().getFrom().getId());
                    }
                    catch (NoSuchElementException e) {
                        reply = new ChatBotReply("Необходимо отправить файл в формате YAML.");
                    }
                else
                    reply = new ChatBotReply("Необходимо отправить файл в формате YAML. MIME-тип должен быть application/x-yaml.");
            }
            else {
                reply = chatBot.answer(update.getMessage().getText(), update.getMessage().getFrom().getId());
            }

            var sendMessage = new SendMessage(
                    update.getMessage().getChatId(),
                    reply.message
            );
            sendMessage.enableHtml(true);
            if (reply.keyboardOptions != null)
                sendMessage.setReplyMarkup(makeKeyboard(reply.keyboardOptions));
            else
                sendMessage.setReplyMarkup(noKeyboard);

            if (reply.imageUrl != null && reply.shareText != null)
            {
                var sendPhoto = new SendPhoto();
                sendPhoto.setChatId(update.getMessage().getChatId());
                sendPhoto.setPhoto(reply.imageUrl);

                InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(new InlineKeyboardButton()
                        .setText("Рассказать в VK")
                        .setUrl(String.format(vkShareUrl,
                                URLEncoder.encode(String.format("https://t.me/%s", botUsername), StandardCharsets.UTF_8),
                                URLEncoder.encode(reply.shareText, StandardCharsets.UTF_8),
                                URLEncoder.encode(reply.imageUrl, StandardCharsets.UTF_8))));
                inlineRows.add(row);
                inlineMarkup.setKeyboard(inlineRows);
                sendPhoto.setReplyMarkup(inlineMarkup);

                execute(sendPhoto);
            }
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup makeKeyboard(List<List<String>> options) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (var line : options) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (String part : line) {
                keyboardRow.add(part);
            }
            keyboardRows.add(keyboardRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    private String getFileContent(String url) {
        try {
            URL fileUrl = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                content.append('\n');
            }
            in.close();
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getFileContent(Document document) {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            File filePath = execute(getFile);
            java.io.File file = downloadFile(filePath);

            Scanner scanner = new Scanner(file).useDelimiter("\\Z");
            String content = scanner.next();
            scanner.close();

            return content;
        } catch (TelegramApiException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
