package com.chatdelta.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import com.chatdelta.models.ProviderState;
import com.chatdelta.api.*;

public class ChatDeltaGUI extends JFrame {
    // Column components for each AI
    private JTextArea chatGPTColumn;
    private JTextArea geminiColumn;
    private JTextArea claudeColumn;
    
    // Scroll panes for each column
    private JScrollPane chatGPTScroll;
    private JScrollPane geminiScroll;
    private JScrollPane claudeScroll;
    
    // Comparison analysis panel
    private JTextArea analysisPanel;
    private JScrollPane analysisScroll;
    private JButton analyzeButton;
    private JButton clearAnalysisButton;
    
    // Input components
    private JTextArea inputField;
    private JButton sendButton;
    private JButton clearAllButton;
    
    // Provider management
    private Map<String, ProviderState> providerStates;
    private Map<String, AIClient> providers;
    private Map<String, JCheckBox> providerCheckboxes;
    private ExecutorService executor;
    
    // Response tracking for analysis
    private String lastPrompt = "";
    private Map<String, String> lastResponses = new HashMap<>();
    private AtomicInteger responseCount = new AtomicInteger(0);
    
    public ChatDeltaGUI(Map<String, ProviderState> providerStates) {
        this.providerStates = providerStates;
        this.providerCheckboxes = new HashMap<>();
        this.providers = new HashMap<>();
        this.executor = Executors.newCachedThreadPool();
        
        initializeProviders();
        initializeUI();
    }
    
    private void initializeProviders() {
        if (providerStates.get("ChatGPT") == ProviderState.ENABLED) {
            providers.put("ChatGPT", new OpenAIClient(System.getenv("CHATGPT_API_KEY")));
        }
        if (providerStates.get("Claude") == ProviderState.ENABLED) {
            providers.put("Claude", new ClaudeClient(System.getenv("CLAUDE_API_KEY")));
        }
        if (providerStates.get("Gemini") == ProviderState.ENABLED) {
            providers.put("Gemini", new GeminiClient(System.getenv("GEMINI_API_KEY")));
        }
    }
    
    private void initializeUI() {
        setTitle("ChatDelta - AI Comparison Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel with all components
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create three-column panel
        JPanel columnsPanel = createColumnsPanel();
        mainPanel.add(columnsPanel, BorderLayout.CENTER);
        
        // Create bottom panel with analysis and input
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Create comparison analysis panel
        JPanel analysisContainer = createAnalysisPanel();
        bottomPanel.add(analysisContainer, BorderLayout.CENTER);
        
        // Create input panel
        JPanel inputPanel = createInputPanel();
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Set size and center
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private JPanel createColumnsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setPreferredSize(new Dimension(1200, 400));
        
        // ChatGPT Column
        JPanel chatGPTPanel = createColumnPanel("ChatGPT", chatGPTColumn = new JTextArea());
        chatGPTScroll = (JScrollPane) chatGPTPanel.getComponent(1);
        panel.add(chatGPTPanel);
        
        // Gemini Column
        JPanel geminiPanel = createColumnPanel("Gemini (2.5 Flash)", geminiColumn = new JTextArea());
        geminiScroll = (JScrollPane) geminiPanel.getComponent(1);
        panel.add(geminiPanel);
        
        // Claude Column
        JPanel claudePanel = createColumnPanel("Claude", claudeColumn = new JTextArea());
        claudeScroll = (JScrollPane) claudePanel.getComponent(1);
        panel.add(claudePanel);
        
        return panel;
    }
    
    private JPanel createColumnPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header with title and checkbox
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Add checkbox for enabling/disabling provider
        String providerKey = title.contains("ChatGPT") ? "ChatGPT" : 
                            title.contains("Gemini") ? "Gemini" : "Claude";
        JCheckBox checkbox = new JCheckBox("", providerStates.get(providerKey) == ProviderState.ENABLED);
        checkbox.setEnabled(providerStates.get(providerKey) == ProviderState.ENABLED);
        providerCheckboxes.put(providerKey, checkbox);
        headerPanel.add(checkbox, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Text area setup
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        
        return panel;
    }
    
    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(1200, 150));
        panel.setBorder(BorderFactory.createTitledBorder("Comparison Analysis (Powered by Gemini 2.5)"));
        
        // Analysis text area
        analysisPanel = new JTextArea();
        analysisPanel.setEditable(false);
        analysisPanel.setLineWrap(true);
        analysisPanel.setWrapStyleWord(true);
        analysisPanel.setFont(new Font("Arial", Font.PLAIN, 12));
        analysisPanel.setBackground(new Color(245, 245, 250));
        
        analysisScroll = new JScrollPane(analysisPanel);
        panel.add(analysisScroll, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        analyzeButton = new JButton("Analyze Differences");
        analyzeButton.setEnabled(false);
        analyzeButton.addActionListener(e -> analyzeResponses());
        buttonPanel.add(analyzeButton);
        
        clearAnalysisButton = new JButton("Clear Analysis");
        clearAnalysisButton.addActionListener(e -> analysisPanel.setText(""));
        buttonPanel.add(clearAnalysisButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Input text area (multi-line)
        inputField = new JTextArea(3, 50);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Add key listener for Ctrl+Enter
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendMessage();
                }
            }
        });
        
        JScrollPane inputScroll = new JScrollPane(inputField);
        panel.add(inputScroll, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        sendButton = new JButton("Send to All");
        sendButton.addActionListener(e -> sendMessage());
        buttonPanel.add(sendButton);
        
        clearAllButton = new JButton("Clear All");
        clearAllButton.addActionListener(e -> clearAll());
        buttonPanel.add(clearAllButton);
        
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Reset for new prompt
        lastPrompt = message;
        lastResponses.clear();
        responseCount.set(0);
        analyzeButton.setEnabled(false);
        
        // Clear previous responses
        chatGPTColumn.setText("");
        geminiColumn.setText("");
        claudeColumn.setText("");
        analysisPanel.setText("");
        
        // Add user prompt to each column
        String promptDisplay = "Prompt: " + message + "\n\n";
        
        // Send to each enabled provider
        int expectedResponses = 0;
        
        if (providerCheckboxes.get("ChatGPT").isSelected() && providers.containsKey("ChatGPT")) {
            chatGPTColumn.setText(promptDisplay + "ChatGPT: Thinking...\n");
            expectedResponses++;
            sendToProvider("ChatGPT", message, chatGPTColumn);
        }
        
        if (providerCheckboxes.get("Gemini").isSelected() && providers.containsKey("Gemini")) {
            geminiColumn.setText(promptDisplay + "Gemini: Thinking...\n");
            expectedResponses++;
            sendToProvider("Gemini", message, geminiColumn);
        }
        
        if (providerCheckboxes.get("Claude").isSelected() && providers.containsKey("Claude")) {
            claudeColumn.setText(promptDisplay + "Claude: Thinking...\n");
            expectedResponses++;
            sendToProvider("Claude", message, claudeColumn);
        }
        
        // Store expected response count for analysis button enabling
        final int expected = expectedResponses;
        responseCount.set(0); // Reset counter
        
        // Clear input
        inputField.setText("");
        inputField.requestFocus();
    }
    
    private void sendToProvider(String providerName, String message, JTextArea column) {
        AIClient client = providers.get(providerName);
        if (client != null) {
            client.sendPrompt(message).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    String promptDisplay = "Prompt: " + message + "\n\n";
                    column.setText(promptDisplay + providerName + ": " + response + "\n");
                    
                    // Store response for analysis
                    lastResponses.put(providerName, response);
                    
                    // Check if all responses are received
                    int responses = responseCount.incrementAndGet();
                    if (responses >= getActiveProviderCount()) {
                        analyzeButton.setEnabled(true);
                    }
                });
            }).exceptionally(e -> {
                SwingUtilities.invokeLater(() -> {
                    String promptDisplay = "Prompt: " + message + "\n\n";
                    column.setText(promptDisplay + providerName + " Error: " + e.getMessage() + "\n");
                    
                    // Count error as a response for enabling analysis
                    int responses = responseCount.incrementAndGet();
                    if (responses >= getActiveProviderCount()) {
                        analyzeButton.setEnabled(true);
                    }
                });
                return null;
            });
        }
    }
    
    private int getActiveProviderCount() {
        int count = 0;
        for (Map.Entry<String, JCheckBox> entry : providerCheckboxes.entrySet()) {
            if (entry.getValue().isSelected() && providers.containsKey(entry.getKey())) {
                count++;
            }
        }
        return count;
    }
    
    private void analyzeResponses() {
        if (lastResponses.isEmpty()) {
            analysisPanel.setText("No responses to analyze.");
            return;
        }
        
        analysisPanel.setText("Analyzing differences...\n");
        
        // Construct analysis prompt
        StringBuilder analysisPrompt = new StringBuilder();
        analysisPrompt.append("You are an AI response analyzer. Compare these AI responses to the question: \"")
                      .append(lastPrompt)
                      .append("\"\n\n");
        
        if (lastResponses.containsKey("ChatGPT")) {
            analysisPrompt.append("CHATGPT RESPONSE:\n")
                          .append(lastResponses.get("ChatGPT"))
                          .append("\n\n");
        }
        
        if (lastResponses.containsKey("Gemini")) {
            analysisPrompt.append("GEMINI RESPONSE:\n")
                          .append(lastResponses.get("Gemini"))
                          .append("\n\n");
        }
        
        if (lastResponses.containsKey("Claude")) {
            analysisPrompt.append("CLAUDE RESPONSE:\n")
                          .append(lastResponses.get("Claude"))
                          .append("\n\n");
        }
        
        analysisPrompt.append("Please provide a concise analysis (under 250 words) covering:\n")
                      .append("1. Key differences in approach or perspective\n")
                      .append("2. Unique insights from each AI\n")
                      .append("3. Common ground - what they agree on\n")
                      .append("4. Which response is most helpful and why\n")
                      .append("5. Any errors or concerns\n\n")
                      .append("Format your response clearly with bullet points or sections.");
        
        // Send to Gemini for analysis
        AIClient geminiClient = providers.get("Gemini");
        if (geminiClient != null) {
            geminiClient.sendPrompt(analysisPrompt.toString()).thenAccept(analysis -> {
                SwingUtilities.invokeLater(() -> {
                    analysisPanel.setText("COMPARISON ANALYSIS:\n\n" + analysis);
                });
            }).exceptionally(e -> {
                SwingUtilities.invokeLater(() -> {
                    analysisPanel.setText("Analysis failed: " + e.getMessage());
                });
                return null;
            });
        } else {
            analysisPanel.setText("Gemini is not available for analysis.");
        }
    }
    
    private void clearAll() {
        chatGPTColumn.setText("");
        geminiColumn.setText("");
        claudeColumn.setText("");
        analysisPanel.setText("");
        inputField.setText("");
        lastResponses.clear();
        lastPrompt = "";
        analyzeButton.setEnabled(false);
    }
    
    public void run() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            inputField.requestFocus();
        });
    }
    
    @Override
    public void dispose() {
        executor.shutdown();
        super.dispose();
    }
}