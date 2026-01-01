package com.nanoseller.api.common.context;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public static void setTenant(UUID tenantId) {
        currentTenant.set(tenantId);
    }
    public static UUID getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}