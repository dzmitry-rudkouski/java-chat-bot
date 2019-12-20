package chatbot;

public class QuizException extends Exception {
    String message;

    QuizException(String message) {
        this.message = message;
    }
}
