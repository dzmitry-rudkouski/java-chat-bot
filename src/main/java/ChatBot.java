import database.DatabaseWorker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ChatBot {
    private QuizRunner runner;
    private DatabaseWorker db;

    protected final String quizNotActive = "Игра ещё не началась. Чтобы посмотреть " +
            "список доступных опросов, напиши команду /start";
    protected final String quizEnded = "Игра закончена. Чтобы начать заново, напиши /start";
    protected final String nextQuiz = "Чтобы пройти следующий тест, напиши /start";
    protected final String quizActive = "Игра уже идёт. Чтобы остановить, напиши /stop";
    protected final String start = "Привет! Выберите нужную опцию меню.";
    protected final String addQuiz = "Чтобы добавить новый опрос, пришли мне его в виде текстового файла";
    protected final String quizParseError = "Произошла ошибка во время обработки файла. %s";
    protected final String quizAdded = "Опрос успешно добавлен!";
    protected final String unrecognized = "Сообщение не распознано. Попробуй ещё раз";
    protected final String quizzesList = "Вот список доступных опросов:";
    protected final String quizNotFound = "Опрос на найден. Попробуйте пройти другой";
    protected final String invited = "Привет! Давай пройдём опрос, который тебе прислал твой друг.\n\n";
    protected final String btnAddQuiz = "➕ Добавить опрос";
    protected final String btnListQuiz = "❓ Список опросов";
    protected final String btnAddAdmin = "✨ Добавить админа";
    protected final String btnCancel = "❌ Отмена";
    protected final String stateAddAdmin = "add-admin";
    protected final String addAdmin = "Пришлите id нового администратора.";
    protected final String incorrectAdminId = "Пожалуйста, укажите корректный id.";
    protected final String returnToHome = "\nВозвращаемся в главное меню.";
    protected final String adminAdded = "Администратор добавлен.";
    protected final String quizDeleted = "Опрос успешно удалён";
    protected final String noQuizzes = "Нет доступных опросов";
    protected final String quizAccepted = "Опрос добавлен всем пользователям";
    protected final String quizNotAvailable = "Опрос недоступен";

    protected final Pattern quizSelection = Pattern.compile("[0-9]+:[ A-Za-zА-Яа-я?,.-]+");

    protected final List<List<String>> adminKeyboard = new ArrayList<>();
    protected final List<List<String>> userKeyboard = new ArrayList<>();
    protected final List<List<String>> cancelKeyboard = new ArrayList<>();

    protected HashMap<Long, String> state = new HashMap<>();

    ChatBot(String botUsername, DatabaseWorker db) {
        runner = new QuizRunner(botUsername, db);
        this.db = db;
        this.db.connect();
        cancelKeyboard.add(Collections.singletonList(btnCancel));
        adminKeyboard.add(Arrays.asList(btnListQuiz, btnAddQuiz, btnAddAdmin));
        userKeyboard.add(Arrays.asList(btnListQuiz, btnAddQuiz));
    }

    ChatBotReply answer(String message, long userId) {
        if (state.containsKey(userId)) {
            if (message.equals(btnCancel)) {
                state.remove(userId);
                return new ChatBotReply(returnToHome, getKeyboard(userId));
            }
            switch (state.get(userId)) {
                case stateAddAdmin:
                    try {
                        long adminId = Long.parseLong(message);
                        db.addAdmin(adminId);
                        state.remove(userId);
                        return new ChatBotReply(adminAdded + returnToHome, getKeyboard(userId));
                    }
                    catch (NumberFormatException e) {
                        return new ChatBotReply(incorrectAdminId, cancelKeyboard);
                    }
                default:
                    state.remove(userId);
            }
        }

        switch (message) {
            case "/start":
            case "старт":
                if (runner.isActive(userId))
                    return new ChatBotReply(quizActive);
                return new ChatBotReply(start, getKeyboard(userId));
            case "/add":
            case btnAddQuiz:
                if (runner.isActive(userId))
                    return new ChatBotReply(quizActive);
                else
                    return new ChatBotReply(addQuiz);
            case "/list":
            case btnListQuiz:
                var allQuizzes = getQuizzesList(db.isAdmin(userId));
                if (allQuizzes.size() == 0)
                    return new ChatBotReply(noQuizzes + returnToHome, getKeyboard(userId));
                return new ChatBotReply(quizzesList, allQuizzes);
            case "/stop":
            case "стоп":
                if (!runner.isActive(userId))
                    return new ChatBotReply(quizNotActive);
                runner.stop(userId);
                return new ChatBotReply(quizEnded);
            case btnAddAdmin:
                state.put(userId, stateAddAdmin);
                return new ChatBotReply(addAdmin, cancelKeyboard);
            default:
                if (runner.isActive(userId)) {
                    if (message.startsWith("/start"))
                        return startQuizFromInvite(message, userId);
                    ChatBotReply reply = runner.proceedRequest(message, userId);
                    if (reply.imageUrl == null)
                        return reply;
                    else {
                        return new ChatBotReply(reply.message + '\n' + nextQuiz,
                                reply.imageUrl, reply.shareText);
                    }
                } else {
                    Matcher m = quizSelection.matcher(message);
                    if (m.matches()) {
                        int quizId = Integer.parseInt(message.split(":")[0]);
                        return startQuiz(userId, quizId, false);
                    } else if (message.startsWith("/start")) {
                        return startQuizFromInvite(message, userId);
                    }
                    else if (message.startsWith("DELETE")) {
                        if (!db.isAdmin(userId))
                            return new ChatBotReply(unrecognized);
                        db.deleteQuiz(Integer.parseInt(message.split(" ")[1]));
                        return new ChatBotReply(quizDeleted + returnToHome, getKeyboard(userId));
                    }
                    else if (message.startsWith("ACCEPT"))
                    {
                        if (!db.isAdmin(userId))
                            return new ChatBotReply(unrecognized);
                        db.markUnhidden(Integer.parseInt(message.split(" ")[1]));
                        return new ChatBotReply(quizAccepted + returnToHome, getKeyboard(userId));
                    }
                    return new ChatBotReply(unrecognized);
                }
        }
    }

    ChatBotReply startQuiz(long userId, int quizId, boolean fromInvite) {
        if (!runner.start(userId, quizId))
            return new ChatBotReply(quizNotFound, getQuizzesList(db.isAdmin(userId)));
        ChatBotReply firstQuestion = runner.proceedRequest("", userId);
        if (fromInvite)
            return new ChatBotReply(invited + runner.getInitialMessage(quizId) +
                    '\n' + firstQuestion.message, firstQuestion.keyboardOptions);
        else
            return new ChatBotReply(runner.getInitialMessage(quizId) +
                    '\n' + firstQuestion.message, firstQuestion.keyboardOptions);
    }

    ChatBotReply startQuizFromInvite(String message, long userId) {
        try {
            int quizId = Integer.parseInt(message.split(" ")[1]);
            if (db.isHidden(quizId))
                return new ChatBotReply(quizNotAvailable);
            runner.stop(userId);
            return startQuiz(userId, quizId, true);
        } catch (Exception e) {
            return new ChatBotReply(start, getQuizzesList(db.isAdmin(userId)));
        }
    }

    ChatBotReply addQuiz(String content, long userId) {
        try {
            if (content == null)
                return new ChatBotReply(quizParseError);
            Quiz quiz = new Quiz(content, db);
            quiz.checkValidity();
            db.addQuiz(Serializer.serialize(quiz), !db.isAdmin(userId));
        } catch (QuizException e) {
            return new ChatBotReply(String.format(quizParseError, e.message));
        }
        return new ChatBotReply(quizAdded + returnToHome, getKeyboard(userId));
    }

    List<List<String>> getQuizzesList(boolean isAdmin) {
        var quizzes = db.getQuizzesList();
        List<List<String>> options = new ArrayList<>();
        for (var e : quizzes) {
            if (!isAdmin && e.getValue2())
                continue;
            options.add(new ArrayList<>());
            options.get(options.size() - 1).add(String.format("%s: %s", e.getValue0(), e.getValue1()));
            if (isAdmin)
            {
                options.get(options.size() - 1).add(String.format("DELETE %s", e.getValue0()));
                if (e.getValue2())
                {
                    options.get(options.size() - 1).add(String.format("ACCEPT %s", e.getValue0()));
                }
            }
        }
        return options;
    }

    List<List<String>> getKeyboard(long userId) {
        return db.isAdmin(userId) ? adminKeyboard : userKeyboard;
    }
}