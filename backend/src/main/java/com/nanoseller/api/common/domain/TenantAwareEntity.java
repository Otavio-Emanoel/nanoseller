package com.nanoseller.api.common.domain;

import com.nanoseller.api.common.context.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity extends BaseEntity {

    @Column(name = "tenant_id", updatable = false, nullable = false)
    private UUID tenantId;

    @PrePersist
    public void onPrePersist() {
        if (this.tenantId == null && TenantContext.getTenant() != null) {
            this.tenantId = TenantContext.getTenant();
        }
    }
}
