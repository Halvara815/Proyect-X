package com.proyectx.focus;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Private on-device storage. There is no account, analytics or network sync. */
public final class RuleStore {
    private static final String PREFS = "proyect_x_rules";
    private static final String RULES = "rules";
    private final SharedPreferences preferences;

    public RuleStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized List<Rule> list() {
        List<Rule> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(RULES, "[]"));
            for (int index = 0; index < array.length(); index++) {
                JSONObject item = array.getJSONObject(index);
                ScheduleWindow schedule = readSchedule(item);
                result.add(new Rule(
                        item.getString("id"),
                        Rule.Type.valueOf(item.getString("type")),
                        Rule.Action.valueOf(item.getString("action")),
                        item.getString("value"),
                        item.optBoolean("enabled", true),
                        schedule));
            }
        } catch (JSONException | IllegalArgumentException ignored) {
            // Corrupt local configuration is treated as empty rather than trusted.
        }
        return result;
    }

    public synchronized boolean save(Rule rule) {
        List<Rule> rules = list();
        rules.add(rule);
        return write(rules);
    }

    public synchronized boolean remove(String id) {
        List<Rule> rules = list();
        Iterator<Rule> iterator = rules.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().id().equals(id)) iterator.remove();
        }
        return write(rules);
    }

    private boolean write(List<Rule> rules) {
        JSONArray array = new JSONArray();
        for (Rule rule : rules) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", rule.id());
                item.put("type", rule.type().name());
                item.put("action", rule.action().name());
                item.put("value", rule.value());
                item.put("enabled", rule.enabled());
                JSONObject schedule = new JSONObject();
                schedule.put("always", rule.schedule().isAlways());
                schedule.put("days", rule.schedule().getDayMask());
                schedule.put("startMinute", rule.schedule().getStartMinute());
                schedule.put("endMinute", rule.schedule().getEndMinute());
                item.put("schedule", schedule);
                array.put(item);
            } catch (JSONException exception) {
                return false;
            }
        }
        return preferences.edit().putString(RULES, array.toString()).commit();
    }

    private static ScheduleWindow readSchedule(JSONObject item) throws JSONException {
        if (!item.has("schedule")) {
            // Version 1 rules were always active; preserve that behavior on migration.
            return ScheduleWindow.always();
        }
        JSONObject schedule = item.getJSONObject("schedule");
        if (schedule.optBoolean("always", false)) {
            return ScheduleWindow.always();
        }
        return ScheduleWindow.of(schedule.getInt("days"), schedule.getInt("startMinute"), schedule.getInt("endMinute"));
    }
}
