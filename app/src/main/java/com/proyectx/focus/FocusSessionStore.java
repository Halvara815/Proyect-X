package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;

/** Private local persistence for a voluntary focus timer; no data leaves the device. */
public final class FocusSessionStore {
    private static final String PREFS = "proyect_x_focus_session";
    private static final String STARTED_AT = "started_at";
    private static final String ENDS_AT = "ends_at";
    private final SharedPreferences preferences;

    public FocusSessionStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized FocusSession current(long nowMillis) {
        long startedAt = preferences.getLong(STARTED_AT, -1L);
        long endsAt = preferences.getLong(ENDS_AT, -1L);
        try {
            FocusSession session = new FocusSession(startedAt, endsAt);
            if (session.isActive(nowMillis)) return session;
        } catch (IllegalArgumentException ignored) {
            // Invalid local state is never treated as an active session.
        }
        clear();
        return null;
    }

    public synchronized FocusSession start(int minutes, long nowMillis) {
        FocusSession session = FocusSession.start(nowMillis, minutes);
        boolean saved = preferences
                .edit()
                .putLong(STARTED_AT, session.startedAtMillis())
                .putLong(ENDS_AT, session.endsAtMillis())
                .commit();
        if (!saved) throw new IllegalStateException("No se pudo guardar la sesión local.");
        return session;
    }

    public synchronized void clear() {
        preferences.edit().remove(STARTED_AT).remove(ENDS_AT).apply();
    }
}
