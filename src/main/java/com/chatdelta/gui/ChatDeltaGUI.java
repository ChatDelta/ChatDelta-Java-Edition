package com.chatdelta.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.chatdelta.models.ProviderState;
import com.chatdelta.api.*;

public class ChatDeltaGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JPanel providerPanel;
    private Map<String, JCheckBox> providerCheckboxes;
    private Map<String, ProviderState> providerStates;
    private Map<String, AIClient> providers;
    private ExecutorService executor;
    
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
        setTitle("ChatDelta - Multi-AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create provider selection panel
        createProviderPanel();
        mainPanel.add(providerPanel, BorderLayout.NORTH);
        
        // Create chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatArea.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chat"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Your Message"));
        
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set window properties
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));
        
        // Focus on input field
        inputField.requestFocusInWindow();
    }
    
    private void createProviderPanel() {
        providerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        providerPanel.setBorder(BorderFactory.createTitledBorder("AI Providers"));
        
        for (String provider : providerStates.keySet()) {
            JCheckBox checkbox = new JCheckBox(provider);
            checkbox.setEnabled(providerStates.get(provider) == ProviderState.ENABLED);
            checkbox.setSelected(providerStates.get(provider) == ProviderState.ENABLED);
            
            if (providerStates.get(provider) == ProviderState.DISABLED) {
                checkbox.setToolTipText(provider + " API key not found");
            }
            
            providerCheckboxes.put(provider, checkbox);
            providerPanel.add(checkbox);
        }
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Add user message to chat
        appendToChat("You: " + message + "\n");
        inputField.setText("");
        
        // Send to selected providers
        for (Map.Entry<String, JCheckBox> entry : providerCheckboxes.entrySet()) {
            String providerName = entry.getKey();
            JCheckBox checkbox = entry.getValue();
            
            if (checkbox.isSelected() && checkbox.isEnabled()) {
                AIClient client = providers.get(providerName);
                if (client != null) {
                    appendToChat("\n" + providerName + " is typing...\n");
                    client.sendPrompt(message).thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            // Replace typing message with actual response
                            String text = chatArea.getText();
                            text = text.replace(providerName + " is typing...", 
                                              providerName + ": " + response);
                            chatArea.setText(text);
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                    }).exceptionally(e -> {
                        SwingUtilities.invokeLater(() -> {
                            String text = chatArea.getText();
                            text = text.replace(providerName + " is typing...", 
                                              providerName + " Error: " + e.getMessage());
                            chatArea.setText(text);
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                        return null;
                    });
                }
            }
        }
    }
    
    private void appendToChat(String text) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(text);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    public void run() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
    
    @Override
    public void dispose() {
        executor.shutdown();
        super.dispose();
    }
}