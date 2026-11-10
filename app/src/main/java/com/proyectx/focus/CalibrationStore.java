package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;

/** Local, optional focus-profile preferences. It is not a clinical assessment. */
public final class CalibrationStore {
    private static final String PREFERENCES = "proyect_x_calibration";
    private static final String COMPLETED = "completed";
    private static final String GOAL = "goal";
    private static final String WINDOW = "window";
    private static final String MODE = "mode";
    private final SharedPreferences preferences;

    public CalibrationStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean isCompleted() {
        return preferences.getBoolean(COMPLETED, false);
    }

    public Profile read() {
        return new Profile(
                preferences.getString(GOAL, ""), preferences.getString(WINDOW, ""), preferences.getString(MODE, ""));
    }

    public boolean save(Profile profile) {
        if (profile == null) return false;
        return preferences
                .edit()
                .putBoolean(COMPLETED, true)
                .putString(GOAL, profile.goal())
                .putString(WINDOW, profile.window())
                .putString(MODE, profile.mode())
                .commit();
    }

    public boolean skip() {
        return preferences.edit().putBoolean(COMPLETED, true).commit();
    }

    public static final class Profile {
        private final String goal;
        private final String window;
        private final String mode;

        public Profile(String goal, String window, String mode) {
            if (goal == null || window == null || mode == null) {
                throw new IllegalArgumentException("El perfil local debe estar completo.");
            }
            this.goal = goal;
            this.window = window;
            this.mode = mode;
        }

        public String goal() {
            return goal;
        }

        public String window() {
            return window;
        }

        public String mode() {
            return mode;
        }

        public boolean hasSelections() {
            return !goal.isBlank() && !window.isBlank() && !mode.isBlank();
        }
    }
}
