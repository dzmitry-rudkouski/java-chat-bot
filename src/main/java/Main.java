import database.DatabaseWorker;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class Main {
	public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

		DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

		if (args.length > 0 && args[0].equals("--dev")) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(System.getenv("PROXY_USER"),
							System.getenv("PROXY_PASS").toCharArray());
				}
			});

			botOptions.setProxyHost(System.getenv("PROXY_HOST"));
			botOptions.setProxyPort(Integer.parseInt(System.getenv("PROXY_PORT")));
			botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
		}
		else {
			botOptions.setProxyType(DefaultBotOptions.ProxyType.NO_PROXY);
		}

		try {
			botsApi.registerBot(new TelegramBot(System.getenv("BOT_USERNAME"), System.getenv("BOT_TOKEN"),
					botOptions, new DatabaseWorker(System.getenv("JDBC_DATABASE_URL")), System.getenv("ADMINS")));
		} catch (TelegramApiRequestException e) {
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			System.out.println("Укажите системные переменные.");
		}
	}
}