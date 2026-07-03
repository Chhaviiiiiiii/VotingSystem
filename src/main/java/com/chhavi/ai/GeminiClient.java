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
        return generateContent(systemInstruction, prompt, "en");
    }

    @SuppressWarnings("unchecked")
    public String generateContent(String systemInstruction, String prompt, String lang) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            if (systemInstruction != null && systemInstruction.toLowerCase().contains("summarize the provided candidate manifesto")) {
                return generateLocalFallbackSummary(prompt, lang);
            }
            return generateOfflineChatResponse(systemInstruction, prompt, lang);
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

    private String generateLocalFallbackSummary(String manifesto, String lang) {
        if (manifesto == null || manifesto.trim().isEmpty()) {
            return "hi".equalsIgnoreCase(lang) ? "कोई घोषणापत्र प्रदान नहीं किया गया।" : "No manifesto provided.";
        }
        
        // Simple sentence splitter
        String[] sentences = manifesto.split("(?<=[.!?])\\s+");
        java.util.List<String> keyPoints = new java.util.ArrayList<>();
        
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            if (lower.contains("promise") || lower.contains("will") || lower.contains("aim") || 
                lower.contains("focus") || lower.contains("goal") || lower.contains("support") || 
                lower.contains("provide") || lower.contains("improve") || lower.contains("create") ||
                lower.contains("strengthen") || lower.contains("ensure") || lower.contains("develop")) {
                keyPoints.add(sentence.trim());
            }
            if (keyPoints.size() >= 4) {
                break;
            }
        }
        
        if (keyPoints.isEmpty()) {
            for (int i = 0; i < Math.min(sentences.length, 3); i++) {
                keyPoints.add(sentences[i].trim());
            }
        }
        
        // Translate key points to the target language
        java.util.List<String> translatedPoints = new java.util.ArrayList<>();
        for (String point : keyPoints) {
            String translatedPoint = translateText(point, lang);
            translatedPoints.add("• " + translatedPoint);
        }
        
        String header = getLocalFallbackHeader(lang);
        
        return header + "\n\n" + String.join("\n", translatedPoints);
    }

    private String translateText(String text, String targetLang) {
        if (targetLang == null || "en".equalsIgnoreCase(targetLang)) {
            return text;
        }
        try {
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" 
                    + targetLang + "&dt=t&q=" + java.net.URLEncoder.encode(text, "UTF-8");
            
            RestTemplate restTemplateForTranslation = new RestTemplate();
            java.util.List<?> response = restTemplateForTranslation.getForObject(url, java.util.List.class);
            if (response != null && !response.isEmpty()) {
                java.util.List<?> outerList = (java.util.List<?>) response.get(0);
                StringBuilder translated = new StringBuilder();
                for (Object item : outerList) {
                    java.util.List<?> innerList = (java.util.List<?>) item;
                    if (innerList != null && !innerList.isEmpty()) {
                        translated.append(innerList.get(0).toString());
                    }
                }
                return translated.toString();
            }
        } catch (Exception e) {
            System.err.println("Translation error: " + e.getMessage());
        }
        return text; // fallback to original
    }

    private String getLocalFallbackHeader(String lang) {
        switch (lang.toLowerCase()) {
            case "hi": return "उम्मीदवार के घोषणापत्र से मुख्य विशेषताएं:";
            case "bn": return "প্রার্থীর ইশতেহার থেকে মূল হাইলাইট:";
            case "te": return "అభ్యర్థి మేనిఫెస్టో నుండి ముఖ్య ముఖ్యాంశాలు:";
            case "ta": return "வேட்பாளரின் தேர்தல் அறிக்கையிலிருந்து முக்கிய சிறப்பம்சங்கள்:";
            case "mr": return "उमेदवाराच्या जाहीरनाम्यातील ठळक मुद्दे:";
            case "gu": return "ઉમેદવારના ઢંઢેરામાંથી મુખ્ય હાઇલાઇટ્સ:";
            case "kn": return "ಅಭ್ಯರ್ಥಿಯ ಪ್ರಣಾಳಿಕೆಯಿಂದ ಪ್ರಮುಖ ಮುಖ್ಯಾಂಶಗಳು:";
            case "ml": return "സ്ഥാനാർത്ഥിയുടെ മാനിഫെസ്റ്റോയിൽ നിന്നുള്ള പ്രധാന ഹൈലൈറ്റുകൾ:";
            case "pa": return "ਉਮੀਦਵਾਰ ਦੇ ਮਨੋਰਥ ਪੱਤਰ ਤੋਂ ਮੁੱਖ ਨੁਕਤੇ:";
            case "or": return "ପ୍ରାର୍ଥୀଙ୍କ ଇସ୍ତାହାରରୁ ମୁଖ୍ୟ ଆକର୍ଷଣ:";
            default: return "Key highlights from the candidate's manifesto:";
        }
    }

    private String generateOfflineChatResponse(String systemInstruction, String userMessage, String lang) {
        String englishMsg = translateText(userMessage, "en").toLowerCase();
        String response = "";
        
        if (englishMsg.contains("last election") || englishMsg.contains("result") || englishMsg.contains("winner") || englishMsg.contains("past") || englishMsg.contains("closed") || englishMsg.contains("jeeta") || englishMsg.contains("pichla")) {
            int start = systemInstruction.indexOf("Past Election Results:");
            if (start != -1) {
                response = systemInstruction.substring(start);
            } else {
                response = "No past election results are currently available.";
            }
        } else if (englishMsg.contains("election") || englishMsg.contains("poll") || englishMsg.contains("chunav") || englishMsg.contains("chal raha")) {
            if (systemInstruction.contains("Active Election exists: Yes")) {
                int start = systemInstruction.indexOf("title: ") + 7;
                int end = systemInstruction.indexOf("\n", start);
                String title = systemInstruction.substring(start, end);
                response = "Currently, there is an active election: \"" + title + "\".";
            } else {
                response = "Currently, there are no active elections.";
            }
        } else if (englishMsg.contains("candidate") || englishMsg.contains("ummeedwar") || englishMsg.contains("party") || englishMsg.contains("neta") || englishMsg.contains("list")) {
            int start = systemInstruction.indexOf("Available candidates: ") + 22;
            int end = systemInstruction.indexOf("\n", start);
            String candidates = systemInstruction.substring(start, end);
            if (candidates.trim().isEmpty() || candidates.equalsIgnoreCase("None")) {
                response = "There are no candidates registered for the current election.";
            } else {
                response = "The registered candidates for the election are: " + candidates;
            }
        } else if (englishMsg.contains("how to vote") || englishMsg.contains("vote kaise") || englishMsg.contains("process") || englishMsg.contains("tarika") || englishMsg.contains("voted")) {
            response = "To cast your vote, please follow these steps:\n"
                     + "1. Go to the 'Cast Vote' page on your dashboard.\n"
                     + "2. Choose your preferred candidate.\n"
                     + "3. Click the 'Submit Vote' button.";
        } else if (englishMsg.contains("hello") || englishMsg.contains("hi") || englishMsg.contains("hey")) {
            response = "Hello! I am your offline AI Voting Assistant. You can ask me about candidate manifestos, active elections, how to vote, or who the candidates are.";
        } else {
            response = "I am operating in offline mode. I can help you with active election status, voting process, and candidate information. For general queries, please configure the Gemini API key.";
        }
        
        return translateText(response, lang);
    }
}
