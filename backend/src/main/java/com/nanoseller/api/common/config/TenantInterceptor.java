package com.nanoseller.api.common.config;

import com.nanoseller.api.common.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    public TenantInterceptor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantIdHeader = request.getHeader("X-Tenant-ID");

        if (tenantIdHeader != null && !tenantIdHeader.isBlank()) {
            try {
                UUID tenantId = UUID.fromString(tenantIdHeader);
                TenantContext.setTenant(tenantId);

                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
            } catch (IllegalArgumentException e) {
                response.setStatus(400);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}