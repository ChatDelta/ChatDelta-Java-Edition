package com.chatdelta.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OpenAIClient implements AIClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    private final OkHttpClient client;
    private final String apiKey;
    private final Gson gson;
    
    public OpenAIClient(String apiKey) {
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
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1000);
        
        Request request = new Request.Builder()
            .url(API_URL)
            .header("Authorization", "Bearer " + apiKey)
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
                    
                    String content = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
                    
                    future.complete(content);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });
        
        return future;
    }
    
    @Override
    public String getProviderName() {
        return "ChatGPT";
    }
}