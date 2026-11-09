package com.proyectx.focus;

/**
 * A local weekly window for a rule. Days use Monday = 0 through Sunday = 6.
 * The end minute is exclusive so adjacent windows never overlap at the edge.
 */
public final class ScheduleWindow {
    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;
    public static final int SATURDAY = 5;
    public static final int SUNDAY = 6;
    public static final int ALL_DAYS = 0x7F;

    private final boolean always;
    private final int dayMask;
    private final int startMinute;
    private final int endMinute;

    private ScheduleWindow(boolean always, int dayMask, int startMinute, int endMinute) {
        this.always = always;
        this.dayMask = dayMask;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public static ScheduleWindow always() {
        return new ScheduleWindow(true, ALL_DAYS, 0, 0);
    }

    public static ScheduleWindow of(int dayMask, int startMinute, int endMinute) {
        if ((dayMask & ~ALL_DAYS) != 0 || dayMask == 0) {
            throw new IllegalArgumentException("A schedule needs at least one valid day");
        }
        validateMinute(startMinute);
        validateMinute(endMinute);
        if (startMinute == endMinute) {
            throw new IllegalArgumentException("Start and end cannot be equal");
        }
        return new ScheduleWindow(false, dayMask, startMinute, endMinute);
    }

    public boolean isAlways() {
        return always;
    }

    public int getDayMask() {
        return dayMask;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public boolean isActive(int day, int minuteOfDay) {
        validateDay(day);
        validateMinute(minuteOfDay);
        if (always) {
            return true;
        }

        if (startMinute < endMinute) {
            return hasDay(day) && minuteOfDay >= startMinute && minuteOfDay < endMinute;
        }

        return (hasDay(day) && minuteOfDay >= startMinute) || (hasDay(previousDay(day)) && minuteOfDay < endMinute);
    }

    public String toDisplayText() {
        if (always) {
            return "Siempre";
        }
        return daysDisplayText() + " · " + timeDisplayText(startMinute) + "–" + timeDisplayText(endMinute);
    }

    private String daysDisplayText() {
        String[] labels = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        if (dayMask == ALL_DAYS) {
            return "Todos los días";
        }
        StringBuilder text = new StringBuilder();
        for (int day = MONDAY; day <= SUNDAY; day++) {
            if (hasDay(day)) {
                if (text.length() > 0) {
                    text.append(", ");
                }
                text.append(labels[day]);
            }
        }
        return text.toString();
    }

    public static String timeDisplayText(int minuteOfDay) {
        validateMinute(minuteOfDay);
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
    }

    private boolean hasDay(int day) {
        return (dayMask & (1 << day)) != 0;
    }

    private static int previousDay(int day) {
        return day == MONDAY ? SUNDAY : day - 1;
    }

    private static void validateDay(int day) {
        if (day < MONDAY || day > SUNDAY) {
            throw new IllegalArgumentException("Day must be Monday through Sunday");
        }
    }

    private static void validateMinute(int minute) {
        if (minute < 0 || minute >= 24 * 60) {
            throw new IllegalArgumentException("Minute must be within a day");
        }
    }
}
