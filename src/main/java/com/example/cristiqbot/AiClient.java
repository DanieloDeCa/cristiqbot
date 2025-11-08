package com.example.cristiqbot;

import org.bukkit.configuration.file.FileConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class AiClient {
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String provider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiClient(CristiqBot plugin) {
        FileConfiguration config = plugin.getPluginConfig();
        this.provider = config.getString("ai.provider", "openai");
        this.apiKey = config.getString("ai.api_key");
        this.model = config.getString("ai.model", "gpt-4o-mini");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        if (apiKey == null || apiKey.equals("YOUR_AI_KEY_HERE")) {
            plugin.getLogger().warning("AI API key not configured! Chat/search will fail.");
        }
    }

    public CompletableFuture<String> generateResponse(String prompt, String personality, String world) {
        String fullPrompt = buildPrompt(prompt, personality, world);
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        if (provider.equals("gemini")) {
            requestBody.putArray("contents")
                .addObject()
                .putArray("parts")
                .addObject()
                .put("text", fullPrompt);
            requestBody.put("model", model);
            requestBody.putObject("generationConfig")
                .put("temperature", 0.7)
                .put("maxOutputTokens", 150)
                .put("candidateCount", 1);
        } else {
            requestBody.put("model", model);
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", fullPrompt);
            requestBody.set("messages", objectMapper.createArrayNode().add(message));
            requestBody.put("max_tokens", 150);
            requestBody.put("temperature", 0.7);
        }

        try {
            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(response.body());
                                if (provider.equals("gemini")) {
                                    return jsonResponse.get("candidates").get(0)
                                        .get("content").get("parts").get(0)
                                        .get("text").asText().trim();
                                } else {
                                    return jsonResponse.get("choices").get(0)
                                        .get("message").get("content").asText().trim();
                                }
                            } catch (Exception e) {
                                return "Sorry, I couldn't process that. Error: " + e.getMessage();
                            }
                        } else {
                            return "AI service error: " + response.statusCode();
                        }
                    })
                    .exceptionally(throwable -> "Connection error: " + throwable.getMessage());
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Error preparing request: " + e.getMessage());
        }
    }

    private String buildPrompt(String userInput, String personality, String world) {
        String worldAdj = switch (world) {
            case "minecraft:the_nether" -> "fiery and demonic ";
            case "minecraft:the_end" -> "mysterious and ancient ";
            default -> "adventurous ";
        };
        return "You are CristiqBot, a " + worldAdj + personality + " AI companion in Minecraft. Respond wittily and helpfully to: " + userInput;
    }

    private String getApiUrl() {
        return switch (provider) {
            case "openai" -> "https://api.openai.com/v1/chat/completions";
            case "gemini" -> "https://api.together.xyz/inference";
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
