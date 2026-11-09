package com.proyectx.focus;

import java.util.Calendar;

public final class LocalDayWindowSmokeTest {
    public static void main(String[] args) {
        Calendar sample = Calendar.getInstance();
        sample.set(2026, Calendar.NOVEMBER, 8, 15, 42, 12);
        sample.set(Calendar.MILLISECOND, 456);
        LocalDayWindow window = LocalDayWindow.containing(sample.getTimeInMillis());
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(window.startMillis());
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));
        assertTrue(window.endMillis() >= window.startMillis());
        System.out.println("LocalDayWindow smoke tests: PASS");
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + " but got " + actual);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("Expected true");
    }
}
