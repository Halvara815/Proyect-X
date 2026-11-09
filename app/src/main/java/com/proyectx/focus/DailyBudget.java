package com.proyectx.focus;

import java.util.Objects;

/** A user-configured local daily allowance. It does not collect usage data. */
public final class DailyBudget {
    private final String packageName;
    private final int limitMinutes;

    public DailyBudget(String packageName, int limitMinutes) {
        this.packageName = RuleEngine.normalizePackageName(packageName);
        if (limitMinutes < 1 || limitMinutes > 24 * 60) {
            throw new IllegalArgumentException("El límite diario debe estar entre 1 y 1440 minutos.");
        }
        this.limitMinutes = limitMinutes;
    }

    public String packageName() {
        return packageName;
    }

    public int limitMinutes() {
        return limitMinutes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DailyBudget)) return false;
        DailyBudget that = (DailyBudget) other;
        return limitMinutes == that.limitMinutes && packageName.equals(that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, limitMinutes);
    }
}
