package com.proyectx.focus;

/** JVM-only regression checks for the local focus-session invariant. */
public final class FocusSessionSmokeTest {
    public static void main(String[] args) {
        FocusSession session = FocusSession.start(1_000L, 25);
        assertTrue(session.isActive(1_000L));
        assertTrue(session.isActive(session.endsAtMillis() - 1L));
        assertFalse(session.isActive(session.endsAtMillis()));
        assertEquals(0L, session.remainingMillis(session.endsAtMillis() + 100L));
        expectFailure(() -> FocusSession.start(1_000L, 4));
        expectFailure(() -> new FocusSession(2_000L, 1_000L));
        System.out.println("FocusSession smoke tests: PASS");
    }

    private static void assertTrue(boolean value) {
        if (!value) throw new AssertionError("Expected true");
    }

    private static void assertFalse(boolean value) {
        if (value) throw new AssertionError("Expected false");
    }

    private static void assertEquals(long expected, long actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + " but got " + actual);
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
