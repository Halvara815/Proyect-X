package com.proyectx.focus;

/**
 * Immutable, on-device focus-session state. It deliberately has no Android or
 * network dependency so its time invariants can be tested on the JVM.
 */
public final class FocusSession {
    private static final long MIN_DURATION_MILLIS = 5L * 60L * 1000L;
    private static final long MAX_DURATION_MILLIS = 12L * 60L * 60L * 1000L;

    private final long startedAtMillis;
    private final long endsAtMillis;

    public FocusSession(long startedAtMillis, long endsAtMillis) {
        if (startedAtMillis < 0 || endsAtMillis <= startedAtMillis) {
            throw new IllegalArgumentException("La sesión debe tener un inicio y un fin válidos.");
        }
        long duration = endsAtMillis - startedAtMillis;
        if (duration < MIN_DURATION_MILLIS || duration > MAX_DURATION_MILLIS) {
            throw new IllegalArgumentException("La sesión debe durar entre 5 minutos y 12 horas.");
        }
        this.startedAtMillis = startedAtMillis;
        this.endsAtMillis = endsAtMillis;
    }

    public static FocusSession start(long nowMillis, int minutes) {
        if (minutes < 5 || minutes > 720) {
            throw new IllegalArgumentException("Elige una duración entre 5 y 720 minutos.");
        }
        return new FocusSession(nowMillis, nowMillis + minutes * 60L * 1000L);
    }

    public long startedAtMillis() {
        return startedAtMillis;
    }

    public long endsAtMillis() {
        return endsAtMillis;
    }

    public boolean isActive(long nowMillis) {
        return nowMillis >= startedAtMillis && nowMillis < endsAtMillis;
    }

    public long remainingMillis(long nowMillis) {
        return Math.max(0L, endsAtMillis - nowMillis);
    }
}
