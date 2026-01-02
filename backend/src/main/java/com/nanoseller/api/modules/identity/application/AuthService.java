package com.nanoseller.api.modules.identity.application;

import com.nanoseller.api.common.security.AuthenticatedUser;
import com.nanoseller.api.common.security.JwtService;
import com.nanoseller.api.modules.identity.domain.User;
import com.nanoseller.api.modules.identity.infra.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        UUID tenantId = request.tenantId();

        User user = (tenantId == null)
                ? userRepository.findByTenantIdIsNullAndEmailIgnoreCase(request.email()).orElse(null)
                : userRepository.findByTenantIdAndEmailIgnoreCase(tenantId, request.email()).orElse(null);

        if (user == null || !user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        AuthenticatedUser principal = new AuthenticatedUser(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getRole()
        );

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        return new LoginResponse(accessToken, refreshToken, "Bearer", Instant.now().toString());
    }

    public RefreshResponse refresh(RefreshRequest request) {
        AuthenticatedUser principal = jwtService.parseRefreshToken(request.refreshToken());
        String newAccessToken = jwtService.generateAccessToken(principal);
        return new RefreshResponse(newAccessToken, "Bearer");
    }

    public record LoginRequest(String email, String password, UUID tenantId) {}

    public record LoginResponse(String accessToken, String refreshToken, String tokenType, String issuedAt) {}

    public record RefreshRequest(String refreshToken) {}

    public record RefreshResponse(String accessToken, String tokenType) {}

    public static class InvalidCredentialsException extends RuntimeException {}
}
