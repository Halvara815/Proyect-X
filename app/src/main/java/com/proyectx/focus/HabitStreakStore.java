package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/** Private device-only persistence for the voluntary focus-session streak. */
public final class HabitStreakStore {
    private static final String PREFS = "proyect_x_habit_streak";
    private static final String LAST_DAY_START = "last_day_start";
    private static final String CURRENT_DAYS = "current_days";
    private static final String BEST_DAYS = "best_days";
    private final SharedPreferences preferences;

    public HabitStreakStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized HabitStreak read() {
        try {
            return new HabitStreak(
                    preferences.getLong(LAST_DAY_START, -1L),
                    preferences.getInt(CURRENT_DAYS, 0),
                    preferences.getInt(BEST_DAYS, 0));
        } catch (IllegalArgumentException ignored) {
            return HabitStreak.empty();
        }
    }

    /** Records one local day only after the focus-session start has been persisted. */
    public synchronized HabitStreak recordFocusStart(long nowMillis) {
        long todayStart = LocalDayWindow.containing(nowMillis).startMillis();
        HabitStreak updated = read().recordDay(todayStart, previousDayStart(nowMillis));
        boolean saved = preferences
                .edit()
                .putLong(LAST_DAY_START, updated.lastDayStartMillis())
                .putInt(CURRENT_DAYS, updated.currentDays())
                .putInt(BEST_DAYS, updated.bestDays())
                .commit();
        if (!saved) throw new IllegalStateException("No se pudo guardar la racha local.");
        return updated;
    }

    private static long previousDayStart(long nowMillis) {
        Calendar previous = Calendar.getInstance();
        previous.setTimeInMillis(nowMillis);
        previous.add(Calendar.DATE, -1);
        return LocalDayWindow.containing(previous.getTimeInMillis()).startMillis();
    }
}
