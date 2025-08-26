package com.chatdelta.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeminiClient implements AIClient {
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final String modelName;
    private final OkHttpClient client;
    private final String apiKey;
    private final Gson gson;
    
    public GeminiClient(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }
    
    public GeminiClient(String apiKey, String modelName) {
        this.apiKey = apiKey;
        this.modelName = modelName != null ? modelName : DEFAULT_MODEL;
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
        
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        
        JsonArray parts = new JsonArray();
        parts.add(part);
        
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        
        JsonArray contents = new JsonArray();
        contents.add(content);
        
        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);
        
        String urlWithKey = API_BASE_URL + modelName + ":generateContent?key=" + apiKey;
        
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
                    String responseBody = response.body().string();
                    
                    if (!response.isSuccessful()) {
                        System.err.println("Gemini API error response: " + responseBody);
                        future.completeExceptionally(
                            new IOException("Gemini API request failed: " + response.code() + " - " + responseBody)
                        );
                        return;
                    }
                    
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    
                    // Check if there's an error in the response
                    if (jsonResponse.has("error")) {
                        JsonObject error = jsonResponse.getAsJsonObject("error");
                        String errorMessage = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                        future.completeExceptionally(new IOException("Gemini API error: " + errorMessage));
                        return;
                    }
                    
                    // Check if candidates array exists and is not empty
                    if (!jsonResponse.has("candidates") || jsonResponse.getAsJsonArray("candidates").size() == 0) {
                        future.completeExceptionally(new IOException("No response candidates from Gemini API"));
                        return;
                    }
                    
                    String content = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
                    
                    future.complete(content);
                } catch (Exception e) {
                    future.completeExceptionally(new IOException("Error parsing Gemini response: " + e.getMessage(), e));
                }
            }
        });
        
        return future;
    }
    
    @Override
    public String getProviderName() {
        return "Gemini (2.5 Flash)";
    }
}