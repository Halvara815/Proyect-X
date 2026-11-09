package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores a minimal local recovery acknowledgement. It deliberately never changes rules,
 * permissions or VPN state: restarting a plan must not weaken the user's safeguards.
 */
public final class RecoveryStore {
    private static final String PREFS = "proyect_x_recovery";
    private static final String COUNT = "count";
    private static final String LAST_REASON = "last_reason";
    private final SharedPreferences preferences;

    public RecoveryStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized void record(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 48) {
            throw new IllegalArgumentException("Elige una opción de reinicio válida.");
        }
        boolean saved = preferences
                .edit()
                .putInt(COUNT, Math.max(0, preferences.getInt(COUNT, 0)) + 1)
                .putString(LAST_REASON, reason)
                .commit();
        if (!saved) throw new IllegalStateException("No se pudo guardar el reinicio local.");
    }

    public synchronized String summary() {
        int count = Math.max(0, preferences.getInt(COUNT, 0));
        if (count == 0) return "Aún no has usado un reinicio local.";
        String reason = preferences.getString(LAST_REASON, "");
        return count
                + (count == 1 ? " reinicio local" : " reinicios locales")
                + (reason == null || reason.isBlank() ? "." : ". Último motivo: " + reason + ".");
    }
}
