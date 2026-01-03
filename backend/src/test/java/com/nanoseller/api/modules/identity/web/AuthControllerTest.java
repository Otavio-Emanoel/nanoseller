package com.nanoseller.api.modules.identity.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanoseller.api.modules.identity.domain.User;
import com.nanoseller.api.modules.identity.infra.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Value("${local.server.port}")
    int port;

    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void loginAndRefreshFlow() throws Exception {
        UUID tenantId = UUID.randomUUID();

        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail("admin@loja.com");
        user.setRole("TENANT_ADMIN");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        userRepository.save(user);

        Map<String, Object> loginBody = Map.of(
                "email", "admin@loja.com",
                "password", "123456",
                "tenantId", tenantId.toString()
        );

        ResponseEntity<String> loginResponse = restClient()
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginBody)
            .retrieve()
            .toEntity(String.class);
        assert loginResponse.getStatusCode().value() == 200;

        String accessToken = objectMapper.readTree(loginResponse.getBody()).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginResponse.getBody()).get("refreshToken").asText();
        assert accessToken != null && !accessToken.isBlank();
        assert refreshToken != null && !refreshToken.isBlank();

        Map<String, Object> refreshBody = Map.of("refreshToken", refreshToken);
        ResponseEntity<String> refreshResponse = restClient()
            .post()
            .uri("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .body(refreshBody)
            .retrieve()
            .toEntity(String.class);
        assert refreshResponse.getStatusCode().value() == 200;

        String newAccessToken = objectMapper.readTree(refreshResponse.getBody()).get("accessToken").asText();
        assert newAccessToken != null && !newAccessToken.isBlank();
    }
}
