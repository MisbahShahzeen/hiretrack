package com.hiretrack.job;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterviewQuestionServiceTest {

    // We can construct the service directly with dummy config values,
    // because the test only exercises extractText (no real HTTP call happens).
    private final InterviewQuestionService service =
            new InterviewQuestionService("http://dummy", "dummy-key", "dummy-model");

    @Test
    void extractText_shouldPullText_fromWellFormedResponse() {
        // Arrange: build a response map shaped exactly like Gemini's JSON:
        // { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
        Map<String, Object> response = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "1. Tell me about yourself."))))));

        // Act
        String result = service.extractText(response);

        // Assert
        assertThat(result).isEqualTo("1. Tell me about yourself.");
    }

    @Test
    void extractText_shouldThrow_whenResponseIsMalformed() {
        // Arrange: an empty/garbage response with none of the expected structure
        Map<String, Object> badResponse = Map.of("error", "something went wrong");

        // Act + Assert: parsing should fail with our clear exception, not a raw NPE
        assertThatThrownBy(() -> service.extractText(badResponse))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected response format");
    }
}