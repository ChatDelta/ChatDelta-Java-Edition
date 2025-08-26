# Comparison Analysis Feature Specification

## Overview
The Comparison Analysis Panel is a unique meta-analysis feature that uses Gemini 2.5 Flash to analyze and summarize the differences between responses from ChatGPT, Gemini, and Claude. This provides users with an AI-powered comparative analysis of the three responses.

## Business Value
- **Insight Generation**: Automatically identifies key differences and similarities
- **Time Saving**: No need to manually compare three lengthy responses
- **Decision Support**: Helps users understand which AI provided the best response for their needs
- **Learning Tool**: Helps users understand each AI's strengths and characteristics

## Technical Architecture

### Component Structure
```java
public class ComparisonAnalyzer {
    private GeminiClient analyzerClient;
    private JTextArea analysisPanel;
    private JButton analyzeButton;
    private JButton clearAnalysisButton;
    
    public void analyzeResponses(
        String originalPrompt,
        String chatGPTResponse,
        String geminiResponse,
        String claudeResponse
    );
}
```

### Analysis Workflow

1. **Response Collection**
   - Wait for all three AIs to complete their responses
   - Store responses with their respective labels
   - Enable "Analyze" button once all responses are ready

2. **Analysis Request Construction**
   ```java
   String analysisPrompt = String.format(
       "You are an AI response analyzer. Compare these three AI responses to the question: \"%s\"\n\n" +
       "CHATGPT RESPONSE:\n%s\n\n" +
       "GEMINI RESPONSE:\n%s\n\n" +
       "CLAUDE RESPONSE:\n%s\n\n" +
       "Please provide a concise analysis covering:\n" +
       "1. Key differences in approach or perspective\n" +
       "2. Unique insights or information from each AI\n" +
       "3. Common ground - what all three agree on\n" +
       "4. Which response is most comprehensive or helpful\n" +
       "5. Any errors or concerning content\n" +
       "6. Overall recommendation for which response best answers the question\n\n" +
       "Keep your analysis under 300 words and be specific.",
       originalPrompt, chatGPTResponse, geminiResponse, claudeResponse
   );
   ```

3. **Analysis Execution**
   - Send constructed prompt to Gemini 2.5 Flash
   - Display "Analyzing..." status in the panel
   - Handle the response asynchronously
   - Display the analysis in the panel

4. **Error Handling**
   - If analysis fails, show error message in panel
   - Retry mechanism with exponential backoff
   - Fallback to simpler analysis prompt if needed

## UI/UX Specifications

### Panel Layout
- **Position**: Between the three AI columns and the prompt input
- **Size**: 
  - Width: Full window width
  - Height: 150-250px (user-resizable)
- **Components**:
  - Main text area (read-only, scrollable)
  - Button bar with "Analyze" and "Clear" buttons
  - Status indicator (analyzing/ready/error)

### Visual Design
- **Background**: Slightly different shade to distinguish from response columns
- **Border**: Clear separator from other panels
- **Font**: Same as response columns but possibly slightly smaller
- **Heading**: "Comparison Analysis (Powered by Gemini 2.5)"

### Interaction Design
1. **Analyze Button States**:
   - Disabled: While responses are still loading
   - Enabled: All responses received
   - Processing: During analysis (with spinner)
   
2. **Clear Button**: Clears only the analysis panel, not the responses

3. **Auto-Analysis Option**: Settings checkbox to automatically analyze after all responses complete

## Implementation Details

### Threading Considerations
- Analysis runs on separate thread
- UI updates via `SwingUtilities.invokeLater()`
- Cancel mechanism if new prompt sent before analysis completes

### Caching Strategy
- Cache analysis for each prompt set
- Clear cache when responses cleared
- Option to re-analyze with different parameters

### Performance Optimization
- Limit response text sent for analysis (e.g., first 1000 chars if too long)
- Batch multiple analysis requests if queued
- Progressive display of analysis as it streams

## Sample Analysis Output

```
COMPARISON ANALYSIS:

**Approach Differences:**
- ChatGPT provided a structured, educational response with clear sections
- Gemini focused on practical examples and real-world applications
- Claude emphasized potential risks and ethical considerations

**Unique Insights:**
- ChatGPT uniquely mentioned historical context
- Gemini provided specific code examples
- Claude discussed regulatory compliance aspects

**Consensus:**
All three AIs agree on the fundamental concept and its importance, though they emphasize different aspects.

**Most Comprehensive:**
Gemini's response appears most comprehensive, combining theory with practical examples.

**Recommendation:**
For technical implementation: Use Gemini's response
For understanding implications: Refer to Claude's response
For learning the basics: ChatGPT's response is clearest
```

## Configuration Options

### User Preferences
- Auto-analyze: On/Off
- Analysis detail level: Brief/Standard/Detailed
- Include confidence scores: Yes/No
- Highlight differences: Yes/No

### Advanced Settings
- Custom analysis prompts
- Choice of analyzer model (Gemini 2.5 Flash/Pro)
- Analysis response timeout
- Maximum tokens for analysis

## Success Metrics
- Analysis completes within 5 seconds
- Analysis accuracy >90% in identifying real differences
- User engagement with analysis panel >70%
- Analysis helps users choose best response >60% of time

## Future Enhancements
1. **Multi-Model Analysis**: Use Claude or ChatGPT as analyzers too
2. **Visual Diff**: Highlight specific different sections
3. **Scoring System**: Numerical scores for each response
4. **Export Analysis**: Save analysis with responses
5. **Learning Mode**: Track which AI users prefer after seeing analysis
6. **Custom Rubrics**: User-defined comparison criteria