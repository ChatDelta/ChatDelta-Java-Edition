package com.chatdelta;

import com.chatdelta.api.*;
import com.chatdelta.models.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BasicTest {
    
    @Test
    public void testProviderStateEnum() {
        assertEquals(ProviderState.ENABLED, ProviderState.valueOf("ENABLED"));
        assertEquals(ProviderState.DISABLED, ProviderState.valueOf("DISABLED"));
    }
    
    @Test
    public void testProviderCreation() {
        Provider provider = new Provider("TestProvider", ProviderState.DISABLED, null);
        assertNotNull(provider);
        assertEquals("TestProvider", provider.getName());
        assertEquals(ProviderState.DISABLED, provider.getState());
        assertNull(provider.getClient());
        assertNotNull(provider.getChatHistory());
        assertTrue(provider.getChatHistory().size() > 0);
    }
    
    @Test
    public void testProviderHistory() {
        Provider provider = new Provider("TestProvider", ProviderState.DISABLED, null);
        int initialSize = provider.getChatHistory().size();
        
        provider.addToHistory("Test message");
        assertEquals(initialSize + 1, provider.getChatHistory().size());
        assertTrue(provider.getChatHistory().contains("Test message"));
    }
    
    @Test
    public void testAIClientImplementations() {
        // Test that the client classes can be instantiated
        // Note: These would fail at runtime without valid API keys
        AIClient openAI = new OpenAIClient("test-key");
        assertNotNull(openAI);
        assertEquals("ChatGPT", openAI.getProviderName());
        
        AIClient gemini = new GeminiClient("test-key");
        assertNotNull(gemini);
        assertEquals("Gemini", gemini.getProviderName());
        
        AIClient claude = new ClaudeClient("test-key");
        assertNotNull(claude);
        assertEquals("Claude", claude.getProviderName());
    }
}