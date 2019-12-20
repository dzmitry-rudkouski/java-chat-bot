package chatbot;

import chatbot.database.FakeDatabaseWorker;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class Application {

    public static void main(String[] args) {

        ApiContextInitializer.init();

        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        if (args.length > 0 && args[0].equals("--dev")) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        System.getProperty("PROXY_USER"),
                        System.getProperty("PROXY_PASS").toCharArray());
                }
            });

            botOptions.setProxyHost(System.getProperty("PROXY_HOST"));
            botOptions.setProxyPort(Integer.parseInt(System.getProperty("PROXY_PORT")));
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        } else {
            botOptions.setProxyType(DefaultBotOptions.ProxyType.NO_PROXY);
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi();
            String username = System.getProperty("BOT_USERNAME");
            System.out.println(username);
            String token = System.getProperty("BOT_TOKEN");
            botsApi.registerBot(
                new TelegramBot(
                    username,
                    token,
                    botOptions,
                    new FakeDatabaseWorker()
                )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}