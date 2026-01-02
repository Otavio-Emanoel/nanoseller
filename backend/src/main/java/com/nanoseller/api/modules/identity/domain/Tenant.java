package com.nanoseller.api.modules.identity.domain;

import com.nanoseller.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "slug", nullable = false, length = 80)
    private String slug;

    @Column(name = "api_key", nullable = false, length = 128)
    private String apiKey;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
