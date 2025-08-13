package com.chatdelta.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeminiClient implements AIClient {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    private final OkHttpClient client;
    private final String apiKey;
    private final Gson gson;
    
    public GeminiClient(String apiKey) {
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
        
        JsonObject text = new JsonObject();
        text.addProperty("text", prompt);
        
        JsonObject part = new JsonObject();
        part.add("text", text);
        
        JsonArray parts = new JsonArray();
        parts.add(part);
        
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        
        JsonArray contents = new JsonArray();
        contents.add(content);
        
        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);
        
        String urlWithKey = API_URL + "?key=" + apiKey;
        
        Request request = new Request.Builder()
            .url(urlWithKey)
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
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
                    
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
        return "Gemini";
    }
}