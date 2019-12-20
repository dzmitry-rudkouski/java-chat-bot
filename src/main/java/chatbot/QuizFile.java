package chatbot;

import java.util.ArrayList;
import java.util.HashMap;

public class QuizFile {
    public String name;
    public String initialMessage;
    public String shareText;
    public HashMap<String, HashMap<String, String>> results;
    public ArrayList<HashMap<String, String>> questions;
    public ArrayList<HashMap<String, String>> answers;
}
