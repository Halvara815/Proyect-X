package com.proyectx.focus;

/**
 * Immutable local streak state. A day counts only after a person starts a
 * voluntary focus session on that calendar day; it is not a clinical outcome
 * or a record of browsing activity.
 */
public final class HabitStreak {
    private final long lastDayStartMillis;
    private final int currentDays;
    private final int bestDays;

    public HabitStreak(long lastDayStartMillis, int currentDays, int bestDays) {
        if (lastDayStartMillis < -1L || currentDays < 0 || bestDays < currentDays) {
            throw new IllegalArgumentException("Estado de racha local inválido.");
        }
        if (lastDayStartMillis == -1L && (currentDays != 0 || bestDays != 0)) {
            throw new IllegalArgumentException("Una racha vacía no puede tener días.");
        }
        if (lastDayStartMillis >= 0L && currentDays == 0) {
            throw new IllegalArgumentException("Una racha con día debe tener al menos un día contado.");
        }
        this.lastDayStartMillis = lastDayStartMillis;
        this.currentDays = currentDays;
        this.bestDays = bestDays;
    }

    public static HabitStreak empty() {
        return new HabitStreak(-1L, 0, 0);
    }

    /**
     * Records a local day. The caller provides the preceding local-day boundary
     * so this value object stays Android- and clock-independent for JVM tests.
     */
    public HabitStreak recordDay(long dayStartMillis, long previousDayStartMillis) {
        if (dayStartMillis < 0L || previousDayStartMillis < -1L) {
            throw new IllegalArgumentException("Día local inválido.");
        }
        if (lastDayStartMillis == dayStartMillis) return this;
        int nextCurrent = lastDayStartMillis == previousDayStartMillis ? currentDays + 1 : 1;
        return new HabitStreak(dayStartMillis, nextCurrent, Math.max(bestDays, nextCurrent));
    }

    public long lastDayStartMillis() {
        return lastDayStartMillis;
    }

    public int currentDays() {
        return currentDays;
    }

    public int bestDays() {
        return bestDays;
    }
}
