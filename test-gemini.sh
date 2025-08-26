#!/bin/bash

# Test script for Gemini API

if [ -z "$GEMINI_API_KEY" ]; then
    echo "Error: GEMINI_API_KEY is not set"
    echo "Please set it with: export GEMINI_API_KEY='your-api-key'"
    exit 1
fi

echo "Testing Gemini API with key: ${GEMINI_API_KEY:0:10}..."
echo ""

# Test the API directly with curl (using Gemini 2.5 Flash)
echo "Testing with Gemini 2.5 Flash..."
curl -s -X POST \
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{
        "text": "Hello, please respond with a simple greeting and mention which Gemini model version you are."
      }]
    }]
  }' | python3 -m json.tool

echo ""
echo "If you see a JSON response above with generated content, the API is working."
echo "If you see an error, check your API key and ensure it has access to Gemini 1.5 Pro."