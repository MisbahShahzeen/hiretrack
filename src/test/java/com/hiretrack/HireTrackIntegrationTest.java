package com.hiretrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HireTrackIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("app.jwt.secret",
                () -> "ThisIsADevelopmentSecretKeyMakeItLongEnoughForHS256Algorithm");
        registry.add("app.gemini.api-key", () -> "dummy-test-key");
    }

    @LocalServerPort
    private int port;

    @Test
    void registerAndLogin_shouldReturnToken() {
        RestClient client = RestClient.create("http://localhost:" + port);

        String registerBody = "{\"email\":\"integration@example.com\",\"password\":\"secret123\",\"fullName\":\"Integration Test\"}";
        var registerStatus = client.post()
                .uri("/api/auth/register")
                .header("Content-Type", "application/json")
                .body(registerBody)
                .retrieve()
                .toBodilessEntity()
                .getStatusCode();
        assertThat(registerStatus.value()).isEqualTo(201);

        String loginBody = "{\"email\":\"integration@example.com\",\"password\":\"secret123\"}";
        String loginResponse = client.post()
                .uri("/api/auth/login")
                .header("Content-Type", "application/json")
                .body(loginBody)
                .retrieve()
                .body(String.class);

        assertThat(loginResponse).isNotBlank();
    }
}