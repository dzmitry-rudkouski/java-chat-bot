package chatbot.database;

import org.glassfish.grizzly.utils.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;

public class FakeDatabaseWorker implements DatabaseWorker {
    @Override
    public void connect() {

    }

    @Override
    public void initDatabase() {

    }

    @Override
    public void addModerator(long userId) {

    }

    @Override
    public ArrayList<String> getModerators() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isModerator(long userId) {
        return false;
    }

    @Override
    public void removeModerator(long userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addQuiz(QuizDataSet quiz, Boolean isHidden, Long author_id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markUnhidden(int quizId) {
    }

    @Override
    public Boolean isHidden(int quizId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteQuiz(int quizId) {

    }

    @Override
    public Long getAuthorId(int quizId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList<Triplet<Integer, String, Boolean>> getQuizzesList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public QuizDataSet getQuiz(long quizId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pair<Integer, Integer> getCurrentQuizState(long userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean quizExists(long quizId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCurrentQuestionId(long userId, int currentQuestionId) {
    }

    @Override
    public void markGameActive(long userId, int quizId) {
    }

    @Override
    public void markGameInactive(long userId) {
    }

    @Override
    public boolean isGameActive(long userId) {
        throw new UnsupportedOperationException();
    }
}
