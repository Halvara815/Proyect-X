package com.proyectx.focus;

import java.util.List;

/** No Android runtime required; run with javac/java as documented in README. */
public final class RuleEngineSmokeTest {
    public static void main(String[] args) {
        assertEquals("example.com", RuleEngine.normalizeDomain(" HTTPS://Example.COM/path "));
        assertEquals("mañana foco", RuleEngine.normalizeKeyword("  MAÑANA   FOCO "));
        Rule blocked = Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "example.com");
        Rule allowed = Rule.create(Rule.Type.DOMAIN, Rule.Action.ALLOW, "docs.example.com");
        assertEquals(RuleEngine.Decision.BLOCK, RuleEngine.evaluate("shop.example.com", null, List.of(blocked)));
        assertEquals(
                RuleEngine.Decision.ALLOW, RuleEngine.evaluate("docs.example.com", null, List.of(blocked, allowed)));
        assertEquals(RuleEngine.Decision.NO_MATCH, RuleEngine.evaluate("example.net", null, List.of(blocked)));
        Rule blockedApp = Rule.create(Rule.Type.APP, Rule.Action.BLOCK, "com.example.distraction");
        Rule allowedApp = Rule.create(Rule.Type.APP, Rule.Action.ALLOW, "com.example.distraction");
        assertEquals(
                RuleEngine.Decision.BLOCK, RuleEngine.evaluatePackage("com.example.distraction", List.of(blockedApp)));
        assertEquals(
                RuleEngine.Decision.ALLOW,
                RuleEngine.evaluatePackage("com.example.distraction", List.of(blockedApp, allowedApp)));
        expectFailure(() -> RuleEngine.normalizeDomain("example.com:443"));
        expectFailure(() -> RuleEngine.normalizePackageName("not a package"));
        System.out.println("RuleEngine smoke tests: PASS");
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
