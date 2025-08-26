# ChatDelta-Java-Edition

Advanced AI comparison tool that enables side-by-side evaluation of responses from ChatGPT, Gemini, and Claude, with intelligent difference analysis.

## 🌟 Key Features

### Three-Column Comparison View
- **Side-by-side responses** from ChatGPT, Gemini 2.5 Flash, and Claude
- **Independent scrolling** for each AI column
- **Clear response attribution** with "ChatGPT:", "Gemini:", "Claude:" prefixes
- **Real-time streaming** of responses as they arrive

### AI-Powered Comparison Analysis
- **Automatic difference analysis** using Gemini 2.5 Flash
- **Identifies unique insights** from each AI model
- **Highlights consensus** and disagreements
- **Recommends best response** for your specific needs
- **One-click analysis** after all responses complete

### Enhanced User Experience
- 🖱️ **Modern Swing GUI** with intuitive layout
- ⌨️ **Keyboard shortcuts** (Ctrl+Enter to send)
- 📝 **Multi-line input** for complex prompts
- 🔐 **Automatic provider detection** based on API keys
- ⚡ **Concurrent API calls** for fastest response times

## 📦 Installation

### Option 1: Download Pre-built Release
Download the latest release for your platform from the [Releases page](https://github.com/ChatDelta/ChatDelta-Java-Edition/releases).

### Option 2: Build from Source

#### Prerequisites
- Java 17 or higher
- Maven 3.6+
- API keys for the providers you want to use:
  - OpenAI API key for ChatGPT
  - Google API key for Gemini
  - Anthropic API key for Claude

#### Building
```bash
git clone https://github.com/ChatDelta/ChatDelta-Java-Edition.git
cd ChatDelta-Java-Edition
mvn clean package
```

This creates an executable JAR in `target/chatdelta-java-0.1.0.jar`

## 🚀 Running ChatDelta

### Quick Start
```bash
# Set your API keys (use the ones you have)
export CHATGPT_API_KEY="your-openai-key"
export GEMINI_API_KEY="your-gemini-key"
export CLAUDE_API_KEY="your-claude-key"

# Run the application
./run.sh
```

### Manual Execution
```bash
java -jar target/chatdelta-java-0.1.0.jar
```

## 💡 How to Use

1. **Launch the application** - A window opens with three columns
2. **Type your prompt** in the input area at the bottom
3. **Press Ctrl+Enter or click "Send to All"** to query all AIs
4. **Watch responses appear** in real-time in their respective columns
5. **Click "Analyze Differences"** to get Gemini's comparison analysis
6. **Read the analysis** in the panel below the columns

### Understanding the Analysis
The comparison analysis provides:
- **Key differences** in approach or perspective
- **Unique insights** that only specific AIs mentioned
- **Common ground** where all AIs agree
- **Recommendation** on which response best answers your question
- **Potential concerns** or errors in any response

## 🏗️ Architecture

### Technology Stack
- **Language**: Java 17
- **GUI Framework**: Swing
- **Build Tool**: Maven
- **HTTP Client**: OkHttp
- **JSON Processing**: Gson

### Project Structure
```
src/main/java/com/chatdelta/
├── Main.java              # Application entry point
├── api/                   # AI provider integrations
│   ├── AIClient.java      # Common interface
│   ├── OpenAIClient.java  # ChatGPT integration
│   ├── GeminiClient.java  # Gemini 2.5 Flash integration
│   └── ClaudeClient.java  # Claude integration
├── gui/                   # User interface
│   └── ChatDeltaGUI.java  # Three-column GUI with analysis
├── models/                # Data models
│   ├── Provider.java
│   └── ProviderState.java
└── specs/                 # Product specifications
    ├── gui-three-column-layout.md
    ├── comparison-analysis-feature.md
    └── implementation-plan.md
```

## 🔧 Configuration

### Environment Variables
- `CHATGPT_API_KEY` - Your OpenAI API key
- `GEMINI_API_KEY` - Your Google Gemini API key
- `CLAUDE_API_KEY` - Your Anthropic Claude API key

### Supported Models
- **ChatGPT**: GPT-3.5-turbo (via OpenAI API)
- **Gemini**: 2.5 Flash (Google's latest model)
- **Claude**: Claude-2 (via Anthropic API)

## 📈 Version History

### v0.3.1 (Latest)
- Updated documentation and automated releases
- GitHub Actions workflow for multi-platform builds

### v0.3.0
- Complete GUI redesign with three-column layout
- AI-powered comparison analysis feature
- Independent scrolling for each column
- Multi-line input with keyboard shortcuts

### v0.2.0
- Upgraded to Gemini 2.5 Flash
- Enhanced error handling
- Added helper scripts

### v0.1.0
- Initial release with Swing GUI
- Basic multi-AI querying

## 🤝 Contributing

We welcome contributions! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- OpenAI for ChatGPT API
- Google for Gemini API
- Anthropic for Claude API
- The Java and Maven communities

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on [GitHub Issues](https://github.com/ChatDelta/ChatDelta-Java-Edition/issues)
- Check the [specs/](specs/) directory for detailed documentation

---

**ChatDelta** - Where AI meets AI for smarter comparisons 🤖🔍🤖