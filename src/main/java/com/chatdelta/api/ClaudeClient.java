package com.chatdelta.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClaudeClient implements AIClient {
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-3-5-sonnet-20241022";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private final OkHttpClient client;
    private final String apiKey;
    private final Gson gson;
    
    public ClaudeClient(String apiKey) {
        this.apiKey = apiKey;
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }
    
    @Override
    public CompletableFuture<String> sendPrompt(String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        
        JsonArray messages = new JsonArray();
        messages.add(message);
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.add("messages", messages);
        requestBody.addProperty("max_tokens", 1000);
        
        Request request = new Request.Builder()
            .url(API_URL)
            .header("x-api-key", apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json")
            ))
            .build();
            
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        future.completeExceptionally(
                            new IOException("API request failed: " + response.code())
                        );
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    
                    JsonArray content = jsonResponse.getAsJsonArray("content");
                    if (content != null && content.size() > 0) {
                        String text = content.get(0).getAsJsonObject()
                            .get("text").getAsString();
                        future.complete(text);
                    } else {
                        future.completeExceptionally(
                            new IOException("No content in response")
                        );
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });
        
        return future;
    }
    
    @Override
    public String getProviderName() {
        return "Claude";
    }
}