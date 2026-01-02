package com.nanoseller.api.modules.identity.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanoseller.api.modules.identity.domain.User;
import com.nanoseller.api.modules.identity.infra.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void loginAndRefreshFlow() throws Exception {
        UUID tenantId = UUID.randomUUID();

        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail("admin@loja.com");
        user.setRole("TENANT_ADMIN");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        userRepository.save(user);

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "admin@loja.com",
                "password", "123456",
                "tenantId", tenantId.toString()
        ));

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        String refreshBody = objectMapper.writeValueAsString(Map.of(
                "refreshToken", refreshToken
        ));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
