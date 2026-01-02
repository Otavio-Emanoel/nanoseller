package com.nanoseller.api.modules.identity.infra;

import com.nanoseller.api.modules.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
    Optional<User> findByTenantIdIsNullAndEmailIgnoreCase(String email);
}
