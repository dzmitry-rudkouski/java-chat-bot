package chatbot;

import java.util.List;

class ChatBotReply {
    String message;
    List<List<String>> keyboardOptions;
    String imageUrl;
    String shareText;

    ChatBotReply(String message) {
        this.message = message;
        keyboardOptions = null;
        imageUrl = null;
        shareText = null;
    }

    ChatBotReply(String message, List<List<String>> options) {
        this.message = message;
        keyboardOptions = options;
        imageUrl = null;
        shareText = null;
    }

    ChatBotReply(String message, String imageUrl, String shareText) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.shareText = shareText;
    }
}
