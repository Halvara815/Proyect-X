package com.proyectx.focus;

import java.util.Calendar;

/** Computes a local calendar-day window without retaining timestamps. */
public final class LocalDayWindow {
    private final long startMillis;
    private final long endMillis;

    private LocalDayWindow(long startMillis, long endMillis) {
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    public static LocalDayWindow containing(long nowMillis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(nowMillis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return new LocalDayWindow(start.getTimeInMillis(), nowMillis);
    }

    public long startMillis() {
        return startMillis;
    }

    public long endMillis() {
        return endMillis;
    }
}
