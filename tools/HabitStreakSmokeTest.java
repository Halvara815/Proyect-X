package com.proyectx.focus;

/** JVM-only regression checks for local streak progression. */
public final class HabitStreakSmokeTest {
    public static void main(String[] args) {
        HabitStreak empty = HabitStreak.empty();
        HabitStreak first = empty.recordDay(300L, 200L);
        assertEquals(1, first.currentDays());
        assertEquals(1, first.bestDays());
        assertSame(first, first.recordDay(300L, 200L));

        HabitStreak consecutive = first.recordDay(400L, 300L);
        assertEquals(2, consecutive.currentDays());
        assertEquals(2, consecutive.bestDays());

        HabitStreak afterGap = consecutive.recordDay(900L, 800L);
        assertEquals(1, afterGap.currentDays());
        assertEquals(2, afterGap.bestDays());
        expectFailure(() -> new HabitStreak(-1L, 1, 1));
        System.out.println("HabitStreak smoke tests: PASS");
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) throw new AssertionError("Expected the same instance for one local day.");
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
