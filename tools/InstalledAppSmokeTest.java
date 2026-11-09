package com.proyectx.focus;

/** JVM-only checks for the local app-picker value object. */
public final class InstalledAppSmokeTest {
    public static void main(String[] args) {
        InstalledApp app = new InstalledApp("YouTube", "com.google.android.youtube");
        assertTrue(app.matches("you"));
        assertTrue(app.matches("ANDROID.YOU"));
        assertTrue(app.matches(""));
        assertFalse(app.matches("maps"));
        assertEquals("com.google.android.youtube", app.packageName());
        System.out.println("InstalledApp smoke tests: PASS");
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
}
