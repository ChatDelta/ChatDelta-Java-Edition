# ChatDelta GUI Specification: Three-Column Layout

## Product Vision
Transform ChatDelta into a side-by-side AI comparison tool with dedicated columns for each AI provider, allowing users to easily compare responses from ChatGPT, Gemini, and Claude in real-time.

## User Interface Layout

### Overall Structure (Top to Bottom)

```
+------------------------------------------------------------------+
|                          ChatDelta                              |
+------------------------------------------------------------------+
|  ChatGPT Column    |   Gemini Column    |    Claude Column      |
|                    |                     |                       |
|  [Scrollable]      |   [Scrollable]      |   [Scrollable]       |
|                    |                     |                       |
|                    |                     |                       |
|                    |                     |                       |
+------------------------------------------------------------------+
|                  Comparison Analysis Panel                       |
|              (Gemini analyzes differences between responses)     |
|                         [Analyze]  [Clear]                       |
+------------------------------------------------------------------+
|                      Prompt Entry Area                           |
|  [Type your prompt here...]                           [Send]     |
+------------------------------------------------------------------+
```

## Detailed Component Specifications

### 1. Three AI Response Columns
- **Layout**: Three equal-width columns arranged horizontally
- **Width**: Each column takes 33.33% of available width
- **Height**: Flexible, occupies majority of window height (60-70%)
- **Features**:
  - Individual vertical scrollbars for each column
  - Column headers showing provider name and status indicator
  - Real-time streaming of responses
  - Independently scrollable content

#### Column Content Format:
- Each response prefixed with provider name:
  - "ChatGPT: [response text]"
  - "Gemini: [response text]"  
  - "Claude: [response text]"
- Timestamp for each response
- Visual separator between multiple responses

### 2. Comparison Analysis Panel (Meta-Analysis Feature)
- **Location**: Below the three columns
- **Height**: Flexible (~150-200px, resizable)
- **Width**: Full width of window
- **Purpose**: Displays Gemini's analysis of the differences between the three AI responses
- **Components**:
  - Wide text area for displaying Gemini's comparison analysis
  - "Analyze" button to trigger comparison
  - "Clear" button to clear the analysis
  - Horizontal scrollbar if needed
- **Content Format**:
  - Gemini summarizes key differences between responses
  - Highlights unique insights from each AI
  - Points out consensus vs disagreement
  - Identifies strengths/weaknesses of each response

### 3. Prompt Entry Area
- **Location**: Bottom of window
- **Components**:
  - Multi-line text field (expandable)
  - Send button
  - Clear button
  - Provider selection checkboxes (optional - to disable specific providers)
- **Behavior**:
  - Enter key + Shift for new line
  - Enter key alone (or Send button) submits to all enabled providers
  - Auto-focus after sending prompt

## Functional Requirements

### Response Handling
1. **Simultaneous Queries**: Single prompt sent to all three AIs concurrently
2. **Streaming Responses**: Each column updates in real-time as responses arrive
3. **Async Display**: Fast providers don't wait for slow ones
4. **Error Handling**: Failed providers show error in their column, others continue

### Comparison Analysis (Key Feature)
1. **Automatic Trigger**: After all three AIs respond, enable "Analyze" button
2. **Analysis Prompt Construction**: 
   - Combine all three responses with clear labels
   - Send to Gemini with analysis instructions
   - Request structured comparison focusing on differences
3. **Analysis Display**: Show Gemini's meta-analysis in the wide panel
4. **Smart Prompting**: Use a template like:
   ```
   Compare these three AI responses to the question "[original prompt]":
   
   ChatGPT response: [chatgpt response]
   Gemini response: [gemini response]  
   Claude response: [claude response]
   
   Please analyze and summarize:
   1. Key differences in approach or content
   2. Unique insights from each AI
   3. Areas of agreement/consensus
   4. Which response is most comprehensive/accurate
   5. Notable strengths or weaknesses
   ```

### Scrolling Behavior
1. **Independent Scrolling**: Each column scrolls independently
2. **Auto-scroll**: New content auto-scrolls to bottom (with toggle option)
3. **Scroll Lock**: User can scroll up to read, stops auto-scroll
4. **Synchronized Scroll Option**: Optional mode to scroll all columns together

### User Interactions
1. **Send Prompt**: Sends to all enabled providers
2. **Copy Response**: Right-click menu to copy individual responses
3. **Clear History**: Button to clear all columns
4. **Export Responses**: Save comparison to file
5. **Resize Columns**: Draggable dividers between columns

## Technical Implementation Notes

### Swing Components
- **Main Container**: `JFrame` with `BorderLayout`
- **Column Container**: `JPanel` with `GridLayout(1, 3)` for equal columns
- **Each Column**: `JScrollPane` containing `JTextArea` or `JTextPane`
- **Status Panel**: `JPanel` with `FlowLayout` or `GridBagLayout`
- **Prompt Area**: `JPanel` with `BorderLayout` containing `JTextArea` and `JButton`

### Threading
- UI updates via `SwingUtilities.invokeLater()`
- Separate thread for each AI provider API call
- Thread-safe response handling

### Styling
- Distinct subtle background colors for each column
- Provider names in bold at start of each response
- Monospace font for code responses
- Clear visual hierarchy

## Success Metrics
- Users can easily compare responses side-by-side
- No response text is lost due to layout issues
- Clear visual indication of which AI provided which response
- Responsive UI that doesn't freeze during API calls
- Intuitive scrolling that doesn't interfere with reading

## Future Enhancements
- Markdown rendering in responses
- Syntax highlighting for code blocks
- Response diff highlighting
- Save/load conversation sessions
- Resizable column widths
- Dark mode theme
- Response voting/rating system