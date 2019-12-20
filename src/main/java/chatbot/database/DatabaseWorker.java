package chatbot.database;

import org.glassfish.grizzly.utils.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;

public interface DatabaseWorker {
    void connect();

    void initDatabase();

    void addModerator(long userId);

    ArrayList<String> getModerators();

    boolean isModerator(long userId);

    void removeModerator(long userId);

    void addQuiz(QuizDataSet quiz, Boolean isHidden, Long author_id);

    void markUnhidden(int quizId);

    Boolean isHidden(int quizId);

    void deleteQuiz(int quizId);

    Long getAuthorId(int quizId);

    ArrayList<Triplet<Integer, String, Boolean>> getQuizzesList();

    QuizDataSet getQuiz(long quizId);

    Pair<Integer, Integer> getCurrentQuizState(long userId);

    boolean quizExists(long quizId);

    void updateCurrentQuestionId(long userId, int currentQuestionId);

    void markGameActive(long userId, int quizId);

    void markGameInactive(long userId);

    boolean isGameActive(long userId);
}
