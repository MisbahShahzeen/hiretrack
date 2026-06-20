package com.hiretrack.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class InterviewQuestionService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public InterviewQuestionService(
            @Value("${app.gemini.base-url}") String baseUrl,
            @Value("${app.gemini.api-key}") String apiKey,
            @Value("${app.gemini.model}") String model) {
        this.restClient = RestClient.create(baseUrl);
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateQuestions(String company, String role) {
        String prompt = """
                You are an experienced technical interviewer.
                Generate 5 likely interview questions for a candidate applying \
                for the role of "%s" at "%s".
                Include a mix of technical and behavioral questions.
                Return them as a numbered list, with no preamble.
                """.formatted(role, company);

        // Build the Gemini request body: { "contents": [ { "parts": [ { "text": prompt } ] } ] }
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))));

        try {
            // POST to /models/{model}:generateContent with the API key header
            Map<String, Object> response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractText(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate interview questions: " + e.getMessage(), e);
        }
    }

    // Dig the generated text out of Gemini's nested response:
    // candidates[0].content.parts[0].text
    @SuppressWarnings("unchecked")
    String extractText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected response format from Gemini", e);
        }
    }
}