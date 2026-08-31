package com.acme.hrms.common.tenant;

import java.util.UUID;

/**
 * Holder for the current request's tenant ID, backed by a ThreadLocal.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    public static UUID getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
