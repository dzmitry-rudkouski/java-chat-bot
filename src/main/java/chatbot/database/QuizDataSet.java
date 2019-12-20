package chatbot.database;

public class QuizDataSet {
    public int id;
    public String name;
    public String initialMessage;
    public String shareText;
    public String questions;
    public String answers;
    public String quizGraph;
    public String answersIndexes;
    public String results;

    public QuizDataSet(String name, String initialMessage, String shareText, String questions, String answers,
                       String quizGraph, String answersIndexes, String results) {
        this.name = name;
        this.initialMessage = initialMessage;
        this.shareText = shareText;
        this.questions = questions;
        this.answers = answers;
        this.quizGraph = quizGraph;
        this.answersIndexes = answersIndexes;
        this.results = results;
    }

    public QuizDataSet(int id, String name, String initialMessage, String shareText, String questions, String answers,
                       String quizGraph, String answersIndexes, String results) {
        this.id = id;
        this.name = name;
        this.initialMessage = initialMessage;
        this.shareText = shareText;
        this.questions = questions;
        this.answers = answers;
        this.quizGraph = quizGraph;
        this.answersIndexes = answersIndexes;
        this.results = results;
    }
}
