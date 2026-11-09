package com.proyectx.focus;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BundledDomainPolicySmokeTest {
    public static void main(String[] args) {
        byte[] policy = ("# Proyect X test policy v1\n"
                        + "# License: CC0-1.0. Authored only for automated and emulator validation.\n"
                        + "# Format: action domain\n"
                        + "block adult-test.invalid\n"
                        + "block media-test.invalid\n"
                        + "allow safe.adult-test.invalid\n")
                .getBytes(StandardCharsets.UTF_8);
        List<Rule> rules = BundledDomainPolicy.parseVerified(policy);
        assertEquals(3, rules.size());
        assertEquals(Rule.Action.BLOCK, rules.get(0).action());
        assertEquals(Rule.Action.ALLOW, rules.get(2).action());
        expectFailure(() -> BundledDomainPolicy.parseVerified("altered".getBytes(StandardCharsets.UTF_8)));
        System.out.println("BundledDomainPolicy smoke tests: PASS");
    }

    private static void expectFailure(Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalStateException expected) {
            return;
        }
        throw new AssertionError("Expected policy verification to fail.");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
