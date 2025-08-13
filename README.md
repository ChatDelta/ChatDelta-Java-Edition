# ChatDelta-Java-Edition

Java implementation of ChatDelta - a TUI application for querying multiple AI APIs simultaneously.

## Features

- 🎯 Query multiple AI providers simultaneously (OpenAI, Google Gemini, Anthropic Claude)
- 📊 Side-by-side response comparison in a terminal UI
- 🔍 Delta analysis showing differences between responses
- ⌨️ Interactive terminal interface with keyboard navigation
- 🔐 Automatic provider detection based on API keys

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- API keys for the providers you want to use:
  - OpenAI API key for ChatGPT
  - Google API key for Gemini
  - Anthropic API key for Claude

## Building

```bash
mvn clean package
```

This will create an executable JAR in `target/chatdelta-java-0.1.0.jar`

## Running

Set your API keys as environment variables and run:

```bash
# Set API keys (use the ones you have)
export CHATGPT_API_KEY="your-openai-key"
export GEMINI_API_KEY="your-gemini-key"
export CLAUDE_API_KEY="your-claude-key"

# Run the application
java -jar target/chatdelta-java-0.1.0.jar
```

## Usage

- **Tab**: Switch between columns
- **Enter**: Send prompt to all enabled providers
- **Arrow Up/Down**: Scroll in selected column
- **Ctrl+C**: Quit application
- **Backspace**: Delete character from input

## Project Structure

```
src/main/java/com/chatdelta/
├── Main.java           # Entry point
├── api/               # API client implementations
│   ├── AIClient.java
│   ├── OpenAIClient.java
│   ├── GeminiClient.java
│   └── ClaudeClient.java
├── models/            # Data models
│   ├── Provider.java
│   └── ProviderState.java
└── tui/              # Terminal UI
    └── ChatDeltaTUI.java
```

## Dependencies

- **Lanterna**: Terminal UI library
- **OkHttp**: HTTP client for API calls
- **Gson**: JSON parsing
- **JUnit**: Testing framework
