package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the person's explicit local choice to show the optional faith-reading module. */
public final class FaithModeStore {
    private static final String PREFS = "proyect_x_faith_mode";
    private static final String ENABLED = "faith_reading_enabled";
    private final SharedPreferences preferences;

    public FaithModeStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized boolean isEnabled() {
        return preferences.getBoolean(ENABLED, false);
    }

    public synchronized boolean setEnabled(boolean enabled) {
        return preferences.edit().putBoolean(ENABLED, enabled).commit();
    }
}
