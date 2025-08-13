package com.chatdelta.tui;

import com.chatdelta.api.*;
import com.chatdelta.models.*;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ChatDeltaTUI {
    private final List<Provider> providers;
    private final Screen screen;
    private final Map<Integer, Integer> scrollPositions;
    private String inputBuffer = "";
    private int selectedColumn = 0;
    private String deltaText = "🔍 Differences between AI responses will appear here after you send a query to multiple providers";
    private boolean running = true;
    private final Map<String, String> latestResponses = new ConcurrentHashMap<>();
    
    public ChatDeltaTUI(Map<String, ProviderState> providerStates) throws IOException {
        this.providers = new ArrayList<>();
        this.scrollPositions = new HashMap<>();
        
        // Initialize providers
        for (String name : Arrays.asList("ChatGPT", "Gemini", "Claude")) {
            ProviderState state = providerStates.getOrDefault(name, ProviderState.DISABLED);
            AIClient client = null;
            
            if (state == ProviderState.ENABLED) {
                client = createClient(name);
            }
            
            providers.add(new Provider(name, state, client));
        }
        
        // Initialize scroll positions (3 providers + 1 delta column)
        for (int i = 0; i < 4; i++) {
            scrollPositions.put(i, 0);
        }
        
        // Initialize terminal
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        screen.setCursorPosition(null);
    }
    
    private AIClient createClient(String name) {
        return switch (name) {
            case "ChatGPT" -> new OpenAIClient(System.getenv("CHATGPT_API_KEY"));
            case "Gemini" -> new GeminiClient(System.getenv("GEMINI_API_KEY"));
            case "Claude" -> new ClaudeClient(System.getenv("CLAUDE_API_KEY"));
            default -> null;
        };
    }
    
    public void run() throws IOException {
        while (running) {
            draw();
            handleInput();
        }
        
        screen.stopScreen();
    }
    
    private void draw() throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        
        TerminalSize size = screen.getTerminalSize();
        int width = size.getColumns();
        int height = size.getRows();
        
        // Calculate column widths (4 equal columns)
        int columnWidth = width / 4;
        
        // Draw provider columns
        for (int i = 0; i < 3; i++) {
            drawProviderColumn(graphics, i, i * columnWidth, columnWidth, height - 3);
        }
        
        // Draw delta column
        drawDeltaColumn(graphics, 3 * columnWidth, columnWidth, height - 3);
        
        // Draw input area
        drawInputArea(graphics, 0, height - 3, width, 3);
        
        screen.refresh();
    }
    
    private void drawProviderColumn(TextGraphics graphics, int index, int x, int width, int height) {
        Provider provider = providers.get(index);
        boolean isSelected = selectedColumn == index;
        
        // Set colors based on state
        TextColor bgColor = provider.getState() == ProviderState.ENABLED ? 
            TextColor.ANSI.BLACK : TextColor.ANSI.BLACK_BRIGHT;
        TextColor fgColor = provider.getState() == ProviderState.ENABLED ? 
            TextColor.ANSI.WHITE : TextColor.ANSI.WHITE_BRIGHT;
        TextColor borderColor = isSelected ? TextColor.ANSI.CYAN : TextColor.ANSI.WHITE;
        
        graphics.setBackgroundColor(bgColor);
        graphics.setForegroundColor(borderColor);
        
        // Draw border
        drawBorder(graphics, x, 0, width, height);
        
        // Draw title
        String title = " " + provider.getName() + " ";
        if (provider.getState() == ProviderState.DISABLED) {
            title += "[No API Key] ";
        }
        graphics.putString(x + 2, 0, title);
        
        // Draw chat history
        graphics.setForegroundColor(fgColor);
        List<String> history = provider.getChatHistory();
        int scrollPos = scrollPositions.get(index);
        int y = 2;
        
        for (int i = scrollPos; i < history.size() && y < height - 1; i++) {
            String message = history.get(i);
            List<String> wrapped = wrapText(message, width - 4);
            for (String line : wrapped) {
                if (y >= height - 1) break;
                graphics.putString(x + 2, y, line);
                y++;
            }
            if (y < height - 1) {
                y++; // Add spacing between messages
            }
        }
    }
    
    private void drawDeltaColumn(TextGraphics graphics, int x, int width, int height) {
        boolean isSelected = selectedColumn == 3;
        
        TextColor borderColor = isSelected ? TextColor.ANSI.CYAN : TextColor.ANSI.WHITE;
        graphics.setBackgroundColor(TextColor.ANSI.BLACK);
        graphics.setForegroundColor(borderColor);
        
        // Draw border
        drawBorder(graphics, x, 0, width, height);
        
        // Draw title
        graphics.putString(x + 2, 0, " Delta Analysis ");
        
        // Draw delta content
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        List<String> wrapped = wrapText(deltaText, width - 4);
        int scrollPos = scrollPositions.get(3);
        int y = 2;
        
        for (int i = scrollPos; i < wrapped.size() && y < height - 1; i++) {
            graphics.putString(x + 2, y, wrapped.get(i));
            y++;
        }
    }
    
    private void drawInputArea(TextGraphics graphics, int x, int y, int width, int height) {
        graphics.setBackgroundColor(TextColor.ANSI.BLACK);
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        
        // Draw border
        drawBorder(graphics, x, y, width, height);
        
        // Draw prompt
        graphics.putString(x + 2, y, " Enter prompt (Tab: switch column, Enter: send, Ctrl+C: quit) ");
        
        // Draw input
        graphics.setForegroundColor(TextColor.ANSI.GREEN);
        String displayInput = "> " + inputBuffer;
        if (displayInput.length() > width - 4) {
            displayInput = displayInput.substring(displayInput.length() - width + 4);
        }
        graphics.putString(x + 2, y + 1, displayInput);
    }
    
    private void drawBorder(TextGraphics graphics, int x, int y, int width, int height) {
        // Top border
        graphics.putString(x, y, "┌");
        for (int i = 1; i < width - 1; i++) {
            graphics.putString(x + i, y, "─");
        }
        graphics.putString(x + width - 1, y, "┐");
        
        // Side borders
        for (int i = 1; i < height - 1; i++) {
            graphics.putString(x, y + i, "│");
            graphics.putString(x + width - 1, y + i, "│");
        }
        
        // Bottom border
        graphics.putString(x, y + height - 1, "└");
        for (int i = 1; i < width - 1; i++) {
            graphics.putString(x + i, y + height - 1, "─");
        }
        graphics.putString(x + width - 1, y + height - 1, "┘");
    }
    
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");
        
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > maxWidth) {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    // Handle very long words
                    while (word.length() > maxWidth) {
                        lines.add(word.substring(0, maxWidth));
                        word = word.substring(maxWidth);
                    }
                    currentLine.append(word);
                } else {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }
            
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        
        return lines;
    }
    
    private void handleInput() throws IOException {
        KeyStroke keyStroke = screen.readInput();
        
        if (keyStroke.getKeyType() == KeyType.Character) {
            inputBuffer += keyStroke.getCharacter();
        } else if (keyStroke.getKeyType() == KeyType.Backspace) {
            if (inputBuffer.length() > 0) {
                inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
            }
        } else if (keyStroke.getKeyType() == KeyType.Tab) {
            selectedColumn = (selectedColumn + 1) % 4;
        } else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
            int pos = scrollPositions.get(selectedColumn);
            if (pos > 0) {
                scrollPositions.put(selectedColumn, pos - 1);
            }
        } else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
            scrollPositions.put(selectedColumn, scrollPositions.get(selectedColumn) + 1);
        } else if (keyStroke.getKeyType() == KeyType.Enter) {
            if (!inputBuffer.trim().isEmpty()) {
                sendPromptToAll(inputBuffer.trim());
                inputBuffer = "";
            }
        } else if (keyStroke.isCtrlDown() && keyStroke.getCharacter() != null && 
                   keyStroke.getCharacter() == 'c') {
            running = false;
        }
    }
    
    private void sendPromptToAll(String prompt) {
        // Add user prompt to all enabled providers
        for (Provider provider : providers) {
            if (provider.getState() == ProviderState.ENABLED) {
                provider.addToHistory("You: " + prompt);
            }
        }
        
        // Clear latest responses for delta calculation
        latestResponses.clear();
        
        // Send to all enabled providers
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (Provider provider : providers) {
            if (provider.getState() == ProviderState.ENABLED && provider.getClient() != null) {
                CompletableFuture<Void> future = provider.getClient()
                    .sendPrompt(prompt)
                    .thenAccept(response -> {
                        provider.addToHistory(provider.getName() + ": " + response);
                        latestResponses.put(provider.getName(), response);
                        updateDelta();
                    })
                    .exceptionally(e -> {
                        provider.addToHistory(provider.getName() + ": Error - " + e.getMessage());
                        return null;
                    });
                futures.add(future);
            }
        }
        
        // Update delta when all responses are received
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(this::updateDelta);
    }
    
    private void updateDelta() {
        if (latestResponses.size() < 2) {
            deltaText = "Need at least 2 responses to compare";
            return;
        }
        
        StringBuilder delta = new StringBuilder();
        delta.append("📊 Response Analysis\n\n");
        
        // Compare response lengths
        delta.append("📏 Response Lengths:\n");
        for (Map.Entry<String, String> entry : latestResponses.entrySet()) {
            delta.append("• ").append(entry.getKey()).append(": ")
                .append(entry.getValue().length()).append(" characters\n");
        }
        
        delta.append("\n🔍 Key Differences:\n");
        
        // Simple difference detection (can be enhanced)
        Set<String> allWords = new HashSet<>();
        Map<String, Set<String>> providerWords = new HashMap<>();
        
        for (Map.Entry<String, String> entry : latestResponses.entrySet()) {
            Set<String> words = new HashSet<>(Arrays.asList(
                entry.getValue().toLowerCase().split("\\s+")
            ));
            providerWords.put(entry.getKey(), words);
            allWords.addAll(words);
        }
        
        // Find unique words per provider
        for (Map.Entry<String, Set<String>> entry : providerWords.entrySet()) {
            Set<String> unique = new HashSet<>(entry.getValue());
            for (Map.Entry<String, Set<String>> other : providerWords.entrySet()) {
                if (!other.getKey().equals(entry.getKey())) {
                    unique.removeAll(other.getValue());
                }
            }
            if (!unique.isEmpty() && unique.size() < 10) {
                delta.append("\n• Unique to ").append(entry.getKey()).append(": ");
                delta.append(String.join(", ", 
                    unique.stream().limit(5).toArray(String[]::new)));
            }
        }
        
        deltaText = delta.toString();
    }
}