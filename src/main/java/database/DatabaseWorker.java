package database;

import org.glassfish.grizzly.utils.Pair;
import org.javatuples.Triplet;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseWorker {
    private Connection c;
    private String dbUrl;

    public DatabaseWorker(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void connect() {
        try {
            c = DriverManager.getConnection(dbUrl);
            initDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void reconnect() {
        try {
            c.close();
            connect();
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void initDatabase() {
        try {
            checkConnection();

            Statement stmt = c.createStatement();
            String quiz = "CREATE TABLE IF NOT EXISTS quiz(" +
                    "id BIGINT PRIMARY KEY NOT NULL, " +
                    "current_quiz_id INT NOT NULL, " +
                    "current_question_id INT NOT NULL, " +
                    "game_active BOOLEAN)";
            stmt.executeUpdate(quiz);

            String quizzes = "CREATE TABLE IF NOT EXISTS quizzes(" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "initial_message TEXT NOT NULL, " +
                    "share_text TEXT NOT NULL, " +
                    "questions TEXT NOT NULL, " +
                    "answers TEXT NOT NULL, " +
                    "quiz_graph TEXT NOT NULL, " +
                    "answers_indexes TEXT NOT NULL, " +
                    "results TEXT NOT NULL, " +
                    "hidden BOOLEAN NOT NULL)";
            stmt.executeUpdate(quizzes);

            String moderators = "CREATE TABLE IF NOT EXISTS moderators(" +
                    "id BIGINT PRIMARY KEY NOT NULL)";
            stmt.executeUpdate(moderators);
            stmt.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void addModerator(long userId) {
        try {
            checkConnection();

            if (isModerator(userId))
                return;
            PreparedStatement stmt = c.prepareStatement("INSERT INTO moderators VALUES (?);");
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isModerator(long userId) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) FROM moderators WHERE id = ?;");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("count") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeModerator(long userId) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("DELETE FROM moderators WHERE id = ?");
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addQuiz(QuizDataSet quiz, Boolean isHidden) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("INSERT INTO quizzes(name, initial_message, share_text, " +
                    "questions, answers, quiz_graph, answers_indexes, " +
                    "results, hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            stmt.setString(1, quiz.name);
            stmt.setString(2, quiz.initialMessage);
            stmt.setString(3, quiz.shareText);
            stmt.setString(4, quiz.questions);
            stmt.setString(5, quiz.answers);
            stmt.setString(6, quiz.quizGraph);
            stmt.setString(7, quiz.answersIndexes);
            stmt.setString(8, quiz.results);
            stmt.setBoolean(9, isHidden);
            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void markUnhidden(int quizId)
    {
        try
        {
            PreparedStatement stmt = c.prepareStatement("UPDATE quizzes SET hidden = FALSE WHERE id = ?;");
            stmt.setInt(1, quizId);
            stmt.executeUpdate();
            stmt.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Boolean isHidden(int quizId)
    {
        try
        {
            PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) FROM quizzes WHERE id = ? AND hidden = TRUE;");
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("count") > 0;
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void deleteQuiz(int quizId)
    {
        try {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM quizzes WHERE id = ?;");
            stmt.setInt(1, quizId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<Triplet<Integer, String, Boolean>> getQuizzesList() {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("SELECT id, name, hidden FROM quizzes ORDER BY id;");
            ResultSet rs = stmt.executeQuery();

            ArrayList<Triplet<Integer, String, Boolean>> quizzes = new ArrayList<>();
            while (rs.next()) {
                quizzes.add(new Triplet<>(rs.getInt("id"), rs.getString("name"),
                        rs.getBoolean("hidden")));
            }

            return quizzes;
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public QuizDataSet getQuiz(long quizId) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("SELECT * FROM quizzes WHERE id = ?;");
            stmt.setLong(1, quizId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new QuizDataSet(rs.getInt("id"), rs.getString("name"), rs.getString("initial_message"),
                        rs.getString("share_text"), rs.getString("questions"), rs.getString("answers"),
                        rs.getString("quiz_graph"), rs.getString("answers_indexes"), rs.getString("results"));
            }
            stmt.close();
            return null;
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public Pair<Integer, Integer> getCurrentQuizState(long userId) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("SELECT * FROM quiz WHERE id = ?;");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                rs.getInt("id");
                int currentQuizId = rs.getInt("current_quiz_id");
                int currentQuestionId = rs.getInt("current_question_id");
                rs.close();
                stmt.close();
                return new Pair<>(currentQuizId, currentQuestionId);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public boolean quizExists(long quizId) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) FROM quizzes WHERE id = ?;");
            stmt.setLong(1, quizId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("count") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return false;
    }

    public void updateCurrentQuestionId(long userId, int currentQuestionId) {
        try {
            checkConnection();

            PreparedStatement stmt;
            stmt = c.prepareStatement("UPDATE quiz SET current_question_id = ? WHERE id = ?");
            stmt.setInt(1, currentQuestionId);
            stmt.setLong(2, userId);

            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void runSql (long userId, String query) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void checkConnection() throws SQLException {
        if (c.isClosed())
            reconnect();
    }

    private void createGameData(long userId, int quizId) {
        try {
            checkConnection();

            PreparedStatement stmt = c.prepareStatement("INSERT INTO quiz VALUES(?, ?, 0, TRUE)");
            stmt.setLong(1, userId);
            stmt.setInt(2, quizId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void markGameActive(long userId, int quizId) {
        destroyGameData(userId);
        createGameData(userId, quizId);
    }

    public void markGameInactive(long userId) {
        runSql(userId, "UPDATE quiz SET game_active = FALSE WHERE id = ?");
    }

    private void destroyGameData(long userId) {
        runSql(userId, "DELETE FROM quiz WHERE id = ?");
    }

    public boolean isGameActive(long userId) {
        try {
            if (c.isClosed())
                reconnect();

            PreparedStatement stmt = c.prepareStatement("SELECT game_active FROM quiz WHERE id = ?;");
            stmt.setLong(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean gameActive = rs.getBoolean("game_active");
                rs.close();
                stmt.close();
                return gameActive;
            }
            else
                return false;
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }
}