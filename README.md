## java-chat-bot

Self-Answering Telegram Bot in Java

Optional:
* `PROXY_HOST` - proxy server address
* `PROXY_PORT` - proxy server port
* `PROXY_USER` - proxy login
* `PROXY_PASS` - proxy password
Required:
* `BOT_USERNAME` - Telegram bot username
* `BOT_TOKEN`    - Telegram bot token

```shell
java -cp "target\lib\*" -DBOT_USERNAME=XXX -DBOT_TOKEN=XXX -jar target/bot-0.0.1-SNAPSHOT.jar
```