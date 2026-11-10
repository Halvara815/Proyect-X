package com.proyectx.focus;

/** Pure reader calculations kept separate from Android UI for regression checks. */
public final class BibleReaderLogic {
    public static final int IMPULSE_COOLDOWN_SECONDS = 120;

    private BibleReaderLogic() {}

    public static int proverbChapterForDay(int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("El día debe estar entre 1 y 31.");
        }
        return dayOfMonth;
    }

    public static int remainingCooldownSeconds(long elapsedMillis) {
        if (elapsedMillis <= 0L) return IMPULSE_COOLDOWN_SECONDS;
        long elapsedSeconds = elapsedMillis / 1_000L;
        return (int) Math.max(0L, IMPULSE_COOLDOWN_SECONDS - elapsedSeconds);
    }
}
