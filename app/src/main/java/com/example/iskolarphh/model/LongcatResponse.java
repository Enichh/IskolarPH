package com.example.iskolarphh.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class LongcatResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public String getAssistantMessage() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message != null) {
            return choices.get(0).message.content;
        }
        return "";
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public String getContent() {
            return content;
        }
    }
}
