package com.chhavi.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiClient {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String generateContent(String systemInstruction, String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Gemini API key is not configured. Falling back to default local model summary.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", systemInstruction + "\n\nUser request: " + prompt);
            
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            
            requestBody.put("contents", List.of(content));

            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> responseContent = (Map<String, Object>) candidate.get("content");
                    if (responseContent != null && responseContent.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) responseContent.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            return "No text response could be read from the AI generation server.";
        } catch (Exception e) {
            return "AI feature error: " + e.getMessage();
        }
    }
}
