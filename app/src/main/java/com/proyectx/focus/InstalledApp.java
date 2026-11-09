package com.proyectx.focus;

import java.util.Locale;
import java.util.Objects;

/** A launchable application visible to Android; this value is never persisted. */
public final class InstalledApp {
    private final String label;
    private final String packageName;

    public InstalledApp(String label, String packageName) {
        this.label = Objects.requireNonNull(label, "label").trim();
        this.packageName = RuleEngine.normalizePackageName(packageName);
    }

    public String label() {
        return label;
    }

    public String packageName() {
        return packageName;
    }

    public boolean matches(String query) {
        if (query == null || query.isBlank()) return true;
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return label.toLowerCase(Locale.ROOT).contains(normalized) || packageName.contains(normalized);
    }
}
