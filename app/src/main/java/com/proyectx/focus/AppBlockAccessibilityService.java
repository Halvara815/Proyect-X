package com.proyectx.focus;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import java.util.Calendar;

/**
 * Optional Android accessibility integration for app blocking.
 * It intentionally uses package names only and never asks Android for window content.
 */
public final class AppBlockAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence rawPackage = event.getPackageName();
        if (rawPackage == null) return;
        try {
            String packageName = RuleEngine.normalizePackageName(rawPackage.toString());
            if (packageName.equals(getPackageName())) return;
            Calendar now = Calendar.getInstance();
            int day = ScheduleWindow.dayIndexFromCalendarDayOfWeek(now.get(Calendar.DAY_OF_WEEK));
            int minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            RuleEngine.Decision decision =
                    RuleEngine.evaluatePackageAt(packageName, new RuleStore(this).list(), day, minuteOfDay);
            if (decision == RuleEngine.Decision.BLOCK) {
                showIntervention(packageName, InterventionActivity.REASON_RULE);
                return;
            }
            // An explicit allow remains an exception to both package rules and a local budget.
            if (decision != RuleEngine.Decision.ALLOW && isDailyBudgetExhausted(packageName)) {
                showIntervention(packageName, InterventionActivity.REASON_DAILY_BUDGET);
            }
        } catch (IllegalArgumentException ignored) {
            // System UI packages without a conventional identifier are ignored.
        }
    }

    @Override
    public void onInterrupt() {
        // No long-running work or retained event content to cancel.
    }

    private boolean isDailyBudgetExhausted(String packageName) {
        try {
            UsageStatsRepository usage = new UsageStatsRepository(this);
            if (!usage.hasUsageAccess()) return false;
            DailyBudget budget = new BudgetStore(this).find(packageName);
            return budget != null
                    && DailyBudgetEngine.assess(
                                    budget, usage.todayForegroundMillis(packageName, System.currentTimeMillis()))
                            .shouldBlock();
        } catch (RuntimeException ignored) {
            // Revoked access, OEM service failure or malformed local configuration must fail open.
            return false;
        }
    }

    private void showIntervention(String packageName, String reason) {
        try {
            Intent intervention = new Intent(this, InterventionActivity.class);
            intervention.putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, packageName);
            intervention.putExtra(InterventionActivity.EXTRA_REASON, reason);
            intervention.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intervention);
        } catch (RuntimeException ignored) {
            // If an OEM blocks activity launch from the service, return to a neutral screen.
            performGlobalAction(GLOBAL_ACTION_HOME);
        }
    }
}
