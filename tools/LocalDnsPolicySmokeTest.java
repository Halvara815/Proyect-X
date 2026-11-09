package com.proyectx.focus;

import java.util.List;

public final class LocalDnsPolicySmokeTest {
    public static void main(String[] args) {
        Rule block = Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "adult-test.invalid");
        Rule allow = Rule.create(Rule.Type.DOMAIN, Rule.Action.ALLOW, "safe.adult-test.invalid");
        LocalDnsPolicy.Result blocked =
                LocalDnsPolicy.evaluate(query("adult-test.invalid"), List.of(block, allow), ScheduleWindow.MONDAY, 0);
        assertEquals(LocalDnsPolicy.Disposition.BLOCKED_LOCALLY, blocked.disposition());
        assertEquals(DomainPolicy.Outcome.BLOCK, blocked.policy().outcome());
        assertTrue(blocked.localResponse() != null);
        LocalDnsPolicy.Result allowed = LocalDnsPolicy.evaluate(
                query("safe.adult-test.invalid"), List.of(block, allow), ScheduleWindow.MONDAY, 0);
        assertEquals(LocalDnsPolicy.Disposition.FORWARD_REQUIRED, allowed.disposition());
        assertEquals(DomainPolicy.Outcome.ALLOW, allowed.policy().outcome());
        assertTrue(allowed.localResponse() == null);
        System.out.println("LocalDnsPolicy smoke tests: PASS");
    }

    private static byte[] query(String domain) {
        String[] labels = domain.split("\\.");
        int length = 17;
        for (String label : labels) length += label.length() + 1;
        byte[] data = new byte[length];
        data[5] = 1;
        int offset = 12;
        for (String label : labels) {
            data[offset++] = (byte) label.length();
            for (int i = 0; i < label.length(); i++) data[offset++] = (byte) label.charAt(i);
        }
        data[offset++] = 0;
        data[offset++] = 0;
        data[offset++] = 1;
        data[offset++] = 0;
        data[offset] = 1;
        return data;
    }

    private static void assertTrue(boolean value) {
        if (!value) throw new AssertionError("Expected true");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
