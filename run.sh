#!/bin/bash

# ChatDelta startup script

# Set Java 21 path
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
JAVA_CMD="$JAVA_HOME/bin/java"

# Check if Java 21 is available
if [ ! -f "$JAVA_CMD" ]; then
    echo "Error: Java 21 not found at $JAVA_HOME"
    echo "Please install Java 21 or update the JAVA_HOME path in this script"
    exit 1
fi

# Build the project if JAR doesn't exist
if [ ! -f "target/chatdelta-java-0.1.0.jar" ]; then
    echo "Building ChatDelta..."
    export JAVA_HOME
    mvn clean package
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check the error messages above."
        exit 1
    fi
fi

# Check for API keys and provide instructions if missing
echo "Checking API keys..."
API_KEYS_FOUND=false

if [ -n "$CHATGPT_API_KEY" ]; then
    echo "✓ ChatGPT API key found"
    API_KEYS_FOUND=true
fi

if [ -n "$GEMINI_API_KEY" ]; then
    echo "✓ Gemini API key found"
    API_KEYS_FOUND=true
fi

if [ -n "$CLAUDE_API_KEY" ]; then
    echo "✓ Claude API key found"
    API_KEYS_FOUND=true
fi

if [ "$API_KEYS_FOUND" = false ]; then
    echo ""
    echo "WARNING: No API keys found!"
    echo "To use ChatDelta, set at least one of the following environment variables:"
    echo "  export CHATGPT_API_KEY='your-openai-key'"
    echo "  export GEMINI_API_KEY='your-gemini-key'"
    echo "  export CLAUDE_API_KEY='your-claude-key'"
    echo ""
    echo "Starting ChatDelta without any providers enabled..."
fi

echo ""
echo "Starting ChatDelta GUI..."
"$JAVA_CMD" -jar target/chatdelta-java-0.1.0.jar