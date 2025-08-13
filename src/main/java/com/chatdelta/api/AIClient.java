package com.chatdelta.api;

import java.util.concurrent.CompletableFuture;

public interface AIClient {
    CompletableFuture<String> sendPrompt(String prompt);
    String getProviderName();
}