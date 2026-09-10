package com.qualitrace.backend.shared.infrastructure.audit;

public final class AuditContext {
    private static final ThreadLocal<String> CURRENT_EVENT = new ThreadLocal<>();

    private AuditContext() {
    }

    public static void setEvent(String event) {
        CURRENT_EVENT.set(event);
    }

    public static String getEvent() {
        return CURRENT_EVENT.get();
    }

    public static void clear() {
        CURRENT_EVENT.remove();
    }
}
