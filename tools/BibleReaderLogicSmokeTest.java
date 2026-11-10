package com.proyectx.focus;

/** JVM-only checks for local reader selections and the two-minute cooling timer. */
public final class BibleReaderLogicSmokeTest {
    public static void main(String[] args) {
        assertEquals(1, BibleReaderLogic.proverbChapterForDay(1));
        assertEquals(31, BibleReaderLogic.proverbChapterForDay(31));
        expectFailure(() -> BibleReaderLogic.proverbChapterForDay(0));
        expectFailure(() -> BibleReaderLogic.proverbChapterForDay(32));
        assertEquals(120, BibleReaderLogic.remainingCooldownSeconds(0L));
        assertEquals(119, BibleReaderLogic.remainingCooldownSeconds(1_500L));
        assertEquals(0, BibleReaderLogic.remainingCooldownSeconds(120_000L));
        assertEquals(0, BibleReaderLogic.remainingCooldownSeconds(130_000L));
        System.out.println("Bible reader logic smoke tests: PASS");
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
