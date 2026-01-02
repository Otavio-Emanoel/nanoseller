package com.nanoseller.api.modules.identity.web;

import com.nanoseller.api.common.context.TenantContext;
import com.nanoseller.api.modules.identity.application.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthService.LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UUID tenantId = request.tenantId != null ? request.tenantId : TenantContext.getTenant();
        AuthService.LoginRequest serviceRequest = new AuthService.LoginRequest(request.email, request.password, tenantId);
        return authService.login(serviceRequest);
    }

    @PostMapping("/refresh")
    public AuthService.RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(new AuthService.RefreshRequest(request.refreshToken));
    }

    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> invalidCredentials() {
        return Map.of("error", "invalid_credentials");
    }

    public static class LoginRequest {
        @NotBlank
        @Email
        public String email;

        @NotBlank
        public String password;

        public UUID tenantId;
    }

    public static class RefreshRequest {
        @NotBlank
        public String refreshToken;
    }
}
