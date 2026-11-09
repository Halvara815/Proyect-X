package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Private configuration storage for daily budgets; usage totals are never stored here. */
public final class BudgetStore {
    private static final String PREFS = "proyect_x_budgets";
    private static final String BUDGETS = "budgets";
    private final SharedPreferences preferences;

    public BudgetStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized List<DailyBudget> list() {
        List<DailyBudget> result = new ArrayList<>();
        try {
            JSONArray values = new JSONArray(preferences.getString(BUDGETS, "[]"));
            for (int index = 0; index < values.length(); index++) {
                JSONObject value = values.getJSONObject(index);
                result.add(new DailyBudget(value.getString("package"), value.getInt("minutes")));
            }
        } catch (JSONException | IllegalArgumentException ignored) {
            // Corrupt local configuration is not used as a blocking decision.
        }
        return result;
    }

    public synchronized DailyBudget find(String packageName) {
        String normalized = RuleEngine.normalizePackageName(packageName);
        for (DailyBudget budget : list()) if (budget.packageName().equals(normalized)) return budget;
        return null;
    }

    /** Replaces a prior budget for the same package instead of creating ambiguous limits. */
    public synchronized boolean save(DailyBudget budget) {
        List<DailyBudget> values = list();
        removeForPackage(values, budget.packageName());
        values.add(budget);
        return write(values);
    }

    public synchronized boolean remove(String packageName) {
        String normalized = RuleEngine.normalizePackageName(packageName);
        List<DailyBudget> values = list();
        removeForPackage(values, normalized);
        return write(values);
    }

    private static void removeForPackage(List<DailyBudget> values, String packageName) {
        Iterator<DailyBudget> iterator = values.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().packageName().equals(packageName)) iterator.remove();
        }
    }

    private boolean write(List<DailyBudget> values) {
        JSONArray serialized = new JSONArray();
        for (DailyBudget budget : values) {
            JSONObject item = new JSONObject();
            try {
                item.put("package", budget.packageName());
                item.put("minutes", budget.limitMinutes());
                serialized.put(item);
            } catch (JSONException exception) {
                return false;
            }
        }
        return preferences.edit().putString(BUDGETS, serialized.toString()).commit();
    }
}
