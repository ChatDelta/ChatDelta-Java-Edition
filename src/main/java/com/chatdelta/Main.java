package com.chatdelta;

import com.chatdelta.gui.ChatDeltaGUI;
import com.chatdelta.models.ProviderState;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, ProviderState> providerStates = detectProviders();
        
        try {
            ChatDeltaGUI gui = new ChatDeltaGUI(providerStates);
            gui.run();
        } catch (Exception e) {
            System.err.println("Error running ChatDelta: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static Map<String, ProviderState> detectProviders() {
        Map<String, ProviderState> states = new HashMap<>();
        
        states.put("ChatGPT", 
            System.getenv("CHATGPT_API_KEY") != null ? 
            ProviderState.ENABLED : ProviderState.DISABLED);
            
        states.put("Gemini", 
            System.getenv("GEMINI_API_KEY") != null ? 
            ProviderState.ENABLED : ProviderState.DISABLED);
            
        states.put("Claude", 
            System.getenv("CLAUDE_API_KEY") != null ? 
            ProviderState.ENABLED : ProviderState.DISABLED);
            
        return states;
    }
}