package com.proyectx.focus;

/** JVM-only checks for daily-budget boundary behavior. */
public final class DailyBudgetSmokeTest {
    public static void main(String[] args) {
        DailyBudget budget = new DailyBudget("com.example.distraction", 25);
        DailyBudgetEngine.Assessment available = DailyBudgetEngine.assess(budget, 24 * 60_000L + 59_999L);
        assertEquals(DailyBudgetEngine.State.AVAILABLE, available.state());
        assertEquals(1L, available.remainingMillis());
        DailyBudgetEngine.Assessment exhausted = DailyBudgetEngine.assess(budget, 25 * 60_000L);
        assertEquals(DailyBudgetEngine.State.EXHAUSTED, exhausted.state());
        assertTrue(exhausted.shouldBlock());
        assertEquals(0L, exhausted.remainingMillis());
        assertEquals(0L, DailyBudgetEngine.assess(budget, 90 * 60_000L).remainingMillis());
        expectFailure(() -> new DailyBudget("com.example.distraction", 0));
        expectFailure(() -> DailyBudgetEngine.assess(budget, -1L));
        System.out.println("DailyBudget smoke tests: PASS");
    }

    private static void assertTrue(boolean value) {
        if (!value) throw new AssertionError("Expected true");
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
