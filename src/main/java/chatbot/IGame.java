package chatbot;

public interface IGame {
    String getInitialMessage(int quizId);

    ChatBotReply proceedRequest(String request, long userId);

    boolean start(long userId, int quizId);

    void stop(long userId);

    boolean isActive(long userId);
}
