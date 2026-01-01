package com.nanoseller.api.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_EMAIL = "email";

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateAccessToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.accessTokenTtl());

        return Jwts.builder()
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .subject(user.userId() != null ? user.userId().toString() : null)
                .claim(CLAIM_EMAIL, user.email())
                .claim(CLAIM_ROLE, user.role())
                .claim(CLAIM_TENANT_ID, user.tenantId() != null ? user.tenantId().toString() : null)
                .signWith(signingKey())
                .compact();
    }

    public AuthenticatedUser parseUser(String jwt) {
        Claims claims = parse(jwt).getPayload();

        UUID userId = claims.getSubject() != null ? UUID.fromString(claims.getSubject()) : null;
        String email = claims.get(CLAIM_EMAIL, String.class);
        String role = claims.get(CLAIM_ROLE, String.class);
        String tenantIdRaw = claims.get(CLAIM_TENANT_ID, String.class);
        UUID tenantId = (tenantIdRaw == null || tenantIdRaw.isBlank()) ? null : UUID.fromString(tenantIdRaw);

        return new AuthenticatedUser(userId, tenantId, email, role);
    }

    public Jws<Claims> parse(String jwt) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(jwt);
    }

    private SecretKey signingKey() {
        // Para HS256, o segredo precisa ser suficientemente longo.
        // Recomendação prática: 32+ bytes.
        byte[] bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
