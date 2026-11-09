package com.proyectx.focus;

import java.util.Objects;
import java.util.UUID;

public final class Rule {
    public enum Type {
        DOMAIN,
        KEYWORD,
        APP
    }

    public enum Action {
        BLOCK,
        ALLOW
    }

    private final String id;
    private final Type type;
    private final Action action;
    private final String value;
    private final boolean enabled;
    private final ScheduleWindow schedule;

    public Rule(String id, Type type, Action action, String value, boolean enabled) {
        this(id, type, action, value, enabled, ScheduleWindow.always());
    }

    public Rule(String id, Type type, Action action, String value, boolean enabled, ScheduleWindow schedule) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.action = Objects.requireNonNull(action, "action");
        this.value = Objects.requireNonNull(value, "value");
        this.enabled = enabled;
        this.schedule = Objects.requireNonNull(schedule, "schedule");
    }

    public static Rule create(Type type, Action action, String value) {
        return create(type, action, value, ScheduleWindow.always());
    }

    public static Rule create(Type type, Action action, String value, ScheduleWindow schedule) {
        String normalized = switch (type) {
            case DOMAIN -> RuleEngine.normalizeDomain(value);
            case KEYWORD -> RuleEngine.normalizeKeyword(value);
            case APP -> RuleEngine.normalizePackageName(value);
        };
        return new Rule(UUID.randomUUID().toString(), type, action, normalized, true, schedule);
    }

    public String id() {
        return id;
    }

    public Type type() {
        return type;
    }

    public Action action() {
        return action;
    }

    public String value() {
        return value;
    }

    public boolean enabled() {
        return enabled;
    }

    public ScheduleWindow schedule() {
        return schedule;
    }

    /** Keeps identity, value and schedule while changing only the active state. */
    public Rule withEnabled(boolean enabled) {
        return new Rule(id, type, action, value, enabled, schedule);
    }
}
