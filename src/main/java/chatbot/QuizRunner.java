package chatbot;

import chatbot.database.DatabaseWorker;
import chatbot.database.QuizDataSet;
import org.glassfish.grizzly.utils.Pair;

import java.util.Objects;

public class QuizRunner implements IGame {
    protected final String         noSuchAnswer = "Такого варианта ответа нет. Попробуй ещё раз";
    protected final String         quizFinished = "Тест пройден. ";
    protected final String         inviteFriend = "\n\nПригласи друга пройти опрос, отправив эту ссылку: https://t.me/%s?start=%d";
    protected final String         quizNotFound = "Опрос не найден либо был удалён.";
    private         DatabaseWorker db;
    private         String         botUsername;

    QuizRunner(String botUsername, DatabaseWorker db) {
        this.botUsername = botUsername;
        this.db = db;
        db.connect();
    }

    private Pair<Integer, Integer> getGameData(long userId) {
        return db.getCurrentQuizState(userId);
    }

    @Override
    public ChatBotReply proceedRequest(String request, long userId) {
        Pair<Integer, Integer> gameData = getGameData(userId);
        int currentQuizId = gameData.getFirst();
        int currentQuestionId = gameData.getSecond();
        Quiz quiz = loadQuiz(currentQuizId);
        if (quiz == null) {
            stop(userId);
            return new ChatBotReply(quizNotFound);
        }

        if (!quiz.answersIndexes.containsKey(request)) {
            return new ChatBotReply(noSuchAnswer, quiz.getAnswersList(currentQuestionId));
        }
        currentQuestionId = quiz.getNextQuestionIndex(quiz.answersIndexes.get(request), currentQuestionId);
        if (quiz.quizGraph.get(currentQuestionId).size() == 0) {
            stop(userId);
            String inviteText = String.format(inviteFriend, botUsername, currentQuizId);
            String resultName = quiz.questions.get(currentQuestionId);
            return new ChatBotReply(String.format(quizFinished + quiz.results.get(resultName).get("description") + inviteText,
                resultName),
                quiz.results.get(resultName).get("image"),
                String.format(quiz.shareText, resultName));
        }
        db.updateCurrentQuestionId(userId, currentQuestionId);
        return new ChatBotReply(quiz.questions.get(currentQuestionId), quiz.getAnswersList(currentQuestionId));
    }

    @Override
    public String getInitialMessage(int quizId) {
        return Objects.requireNonNull(loadQuiz(quizId)).initialMessage;
    }

    @Override
    public boolean start(long userId, int quizId) {
        if (quizNotExists(quizId)) {
            return false;
        }
        db.markGameActive(userId, quizId);
        return true;
    }

    private Quiz loadQuiz(int quizId) {
        if (quizNotExists(quizId))
            return null;
        QuizDataSet data = db.getQuiz(quizId);
        return Serializer.deserialize(data);
    }

    private boolean quizNotExists(int quizId) {
        return !db.quizExists(quizId);
    }

    @Override
    public void stop(long userId) {
        db.markGameInactive(userId);
    }

    @Override
    public boolean isActive(long userId) {
        return db.isGameActive(userId);
    }
}
