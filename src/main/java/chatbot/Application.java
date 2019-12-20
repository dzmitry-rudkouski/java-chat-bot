package chatbot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Objects;

public class Application {

    private static Log log = LogFactory.getLog(Application.class);

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
            String username = Objects.requireNonNull(System.getProperty("BOT_USERNAME"), "BOT_USERNAME is not provided");
            String token = Objects.requireNonNull(System.getProperty("BOT_TOKEN"), "BOT_TOKEN is not provided");
            botsApi.registerBot(
                new TelegramBot(
                    username,
                    token,
                    botOptions
                )
            );
        } catch (Exception e) {
            log.error("Unexpected error", e);
        }
    }
}