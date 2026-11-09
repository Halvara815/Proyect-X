package com.proyectx.focus;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import java.util.Map;

/** Reads one aggregate only after Android grants Usage Access; it never stores or transmits it. */
public final class UsageStatsRepository {
    private final Context context;

    public UsageStatsRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        return appOps != null
                && appOps.checkOpNoThrow(
                                AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(),
                                context.getPackageName())
                        == AppOpsManager.MODE_ALLOWED;
    }

    /** Call only from a worker thread after {@link #hasUsageAccess()} returns true. */
    public long todayForegroundMillis(String packageName, long nowMillis) {
        String normalized = RuleEngine.normalizePackageName(packageName);
        if (!hasUsageAccess()) throw new SecurityException("El acceso de uso no está habilitado.");
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return 0L;
        LocalDayWindow day = LocalDayWindow.containing(nowMillis);
        Map<String, UsageStats> stats = manager.queryAndAggregateUsageStats(day.startMillis(), day.endMillis());
        if (stats == null) return 0L;
        UsageStats usage = stats.get(normalized);
        return usage == null ? 0L : Math.max(0L, usage.getTotalTimeInForeground());
    }
}
