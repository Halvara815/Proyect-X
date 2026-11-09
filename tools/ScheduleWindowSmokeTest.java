package com.proyectx.focus;

import java.util.List;

/** JVM-only checks for time-window boundaries and rule precedence. */
public final class ScheduleWindowSmokeTest {
    public static void main(String[] args) {
        ScheduleWindow workdays =
                ScheduleWindow.of((1 << ScheduleWindow.MONDAY) | (1 << ScheduleWindow.TUESDAY), 9 * 60, 17 * 60);
        assertTrue(workdays.isActive(ScheduleWindow.MONDAY, 9 * 60));
        assertTrue(workdays.isActive(ScheduleWindow.TUESDAY, 16 * 60 + 59));
        assertFalse(workdays.isActive(ScheduleWindow.MONDAY, 8 * 60 + 59));
        assertFalse(workdays.isActive(ScheduleWindow.MONDAY, 17 * 60));

        ScheduleWindow overnightFriday = ScheduleWindow.of(1 << ScheduleWindow.FRIDAY, 22 * 60, 2 * 60);
        assertTrue(overnightFriday.isActive(ScheduleWindow.FRIDAY, 23 * 60));
        assertTrue(overnightFriday.isActive(ScheduleWindow.SATURDAY, 60));
        assertFalse(overnightFriday.isActive(ScheduleWindow.SATURDAY, 2 * 60));
        assertFalse(overnightFriday.isActive(ScheduleWindow.SUNDAY, 60));

        Rule block = Rule.create(Rule.Type.APP, Rule.Action.BLOCK, "com.example.distraction", overnightFriday);
        Rule allow = Rule.create(
                Rule.Type.APP,
                Rule.Action.ALLOW,
                "com.example.distraction",
                ScheduleWindow.of(1 << ScheduleWindow.SATURDAY, 0, 90));
        assertEquals(
                RuleEngine.Decision.BLOCK,
                RuleEngine.evaluatePackageAt(
                        "com.example.distraction", List.of(block), ScheduleWindow.FRIDAY, 23 * 60));
        assertEquals(
                RuleEngine.Decision.ALLOW,
                RuleEngine.evaluatePackageAt(
                        "com.example.distraction", List.of(block, allow), ScheduleWindow.SATURDAY, 60));
        assertEquals(
                RuleEngine.Decision.BLOCK,
                RuleEngine.evaluatePackageAt(
                        "com.example.distraction", List.of(block, allow), ScheduleWindow.SATURDAY, 100));

        Rule legacy = new Rule("legacy", Rule.Type.APP, Rule.Action.BLOCK, "com.example.legacy", true);
        assertEquals(
                RuleEngine.Decision.BLOCK,
                RuleEngine.evaluatePackageAt("com.example.legacy", List.of(legacy), ScheduleWindow.SUNDAY, 3 * 60));
        expectFailure(() -> ScheduleWindow.of(1 << ScheduleWindow.MONDAY, 8 * 60, 8 * 60));
        assertEquals(ScheduleWindow.MONDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.MONDAY));
        assertEquals(ScheduleWindow.TUESDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.TUESDAY));
        assertEquals(
                ScheduleWindow.WEDNESDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.WEDNESDAY));
        assertEquals(
                ScheduleWindow.THURSDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.THURSDAY));
        assertEquals(ScheduleWindow.FRIDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.FRIDAY));
        assertEquals(
                ScheduleWindow.SATURDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.SATURDAY));
        assertEquals(ScheduleWindow.SUNDAY, ScheduleWindow.dayIndexFromCalendarDayOfWeek(java.util.Calendar.SUNDAY));
        expectFailure(() -> ScheduleWindow.dayIndexFromCalendarDayOfWeek(0));
        expectFailure(() -> ScheduleWindow.dayIndexFromCalendarDayOfWeek(8));
        System.out.println("ScheduleWindow smoke tests: PASS");
    }

    private static void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("Expected true");
    }

    private static void assertFalse(boolean condition) {
        if (condition) throw new AssertionError("Expected false");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }

    private static void expectFailure(Runnable operation) {
        try {
            operation.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
