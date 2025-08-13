package com.chatdelta.models;

import com.chatdelta.api.AIClient;
import java.util.ArrayList;
import java.util.List;

public class Provider {
    private final String name;
    private final ProviderState state;
    private final List<String> chatHistory;
    private final AIClient client;
    
    public Provider(String name, ProviderState state, AIClient client) {
        this.name = name;
        this.state = state;
        this.client = client;
        this.chatHistory = new ArrayList<>();
        this.chatHistory.add(createWelcomeMessage(name));
    }
    
    private String createWelcomeMessage(String name) {
        return switch (name) {
            case "ChatGPT" -> """
                🤖 Welcome to ChatGPT!
                
                🧠 Model: GPT-4o
                🏢 Provider: OpenAI
                
                ✨ Ready to assist with your queries!
                I excel at general knowledge, coding, writing, and analysis.""";
                
            case "Gemini" -> """
                🌟 Welcome to Gemini!
                
                🚀 Model: Gemini-1.5-Pro
                🏢 Provider: Google
                
                🎯 Ready for action!
                I'm great at multimodal tasks, long context understanding, and creative problem-solving.""";
                
            case "Claude" -> """
                🎭 Welcome to Claude!
                
                🧬 Model: Claude-3.5-Sonnet
                🏢 Provider: Anthropic
                
                👋 Hello there!
                I'm designed to be helpful, harmless, and honest. I excel at analysis, writing, coding, and thoughtful conversation.""";
                
            default -> """
                🤖 Welcome to AI Chat!
                
                Ready to help with your questions!""";
        };
    }
    
    public String getName() {
        return name;
    }
    
    public ProviderState getState() {
        return state;
    }
    
    public List<String> getChatHistory() {
        return chatHistory;
    }
    
    public AIClient getClient() {
        return client;
    }
    
    public void addToHistory(String message) {
        chatHistory.add(message);
    }
}