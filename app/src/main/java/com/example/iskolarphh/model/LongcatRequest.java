package com.example.iskolarphh.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class LongcatRequest {
    private String model;
    private List<Message> messages;

    @SerializedName("max_tokens")
    private int maxTokens;

    private double temperature;

    public LongcatRequest(String model, List<Message> messages, int maxTokens, double temperature) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
