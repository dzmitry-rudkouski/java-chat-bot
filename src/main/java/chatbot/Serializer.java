package chatbot;

import chatbot.database.QuizDataSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Serializer {
    private static final Gson gson           = new Gson();
    private static final Type questions      = new TypeToken<Map<Integer, String>>() {
    }.getType();
    private static final Type answers        = new TypeToken<ArrayList<String>>() {
    }.getType();
    private static final Type quizGraph      = new TypeToken<ArrayList<ArrayList<DestinationNode>>>() {
    }.getType();
    private static final Type answersIndexes = new TypeToken<Map<String, Integer>>() {
    }.getType();
    private static final Type results        = new TypeToken<HashMap<String, HashMap<String, String>>>() {
    }.getType();

    public static Quiz deserialize(QuizDataSet data) {
        return new Quiz(data.name, data.initialMessage, data.shareText, gson.fromJson(data.questions, questions),
            gson.fromJson(data.answers, answers), gson.fromJson(data.quizGraph, quizGraph),
            gson.fromJson(data.answersIndexes, answersIndexes), gson.fromJson(data.results, results));
    }

    public static QuizDataSet serialize(Quiz quiz) {
        return new QuizDataSet(quiz.name, quiz.initialMessage, quiz.shareText, gson.toJson(quiz.questions),
            gson.toJson(quiz.answers), gson.toJson(quiz.quizGraph), gson.toJson(quiz.answersIndexes),
            gson.toJson(quiz.results));
    }
}
