package com.proyectx.focus;

import java.util.List;

/** Controlled .invalid-domain laboratory; it measures policy behavior only, never web coverage. */
public final class DnsCoverageLabSmokeTest {
    public static void main(String[] args) {
        List<Rule> rules = List.of(
                Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "adult-test.invalid"),
                Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "media-test.invalid"),
                Rule.create(Rule.Type.DOMAIN, Rule.Action.ALLOW, "safe.adult-test.invalid"));
        Case[] cases = {
            new Case("adult-test.invalid", LocalDnsPolicy.Disposition.BLOCKED_LOCALLY),
            new Case("cdn.adult-test.invalid", LocalDnsPolicy.Disposition.BLOCKED_LOCALLY),
            new Case("media-test.invalid", LocalDnsPolicy.Disposition.BLOCKED_LOCALLY),
            new Case("safe.adult-test.invalid", LocalDnsPolicy.Disposition.FORWARD_REQUIRED),
            new Case("unrelated-test.invalid", LocalDnsPolicy.Disposition.FORWARD_REQUIRED)
        };
        int passed = 0;
        for (Case item : cases) {
            LocalDnsPolicy.Result result = LocalDnsPolicy.evaluate(query(item.domain), rules, ScheduleWindow.MONDAY, 0);
            if (result.disposition() != item.expected)
                throw new AssertionError(item.domain + " produced " + result.disposition());
            passed++;
        }
        if (passed != cases.length) throw new AssertionError("Laboratorio DNS incompleto");
        System.out.println("Dns coverage lab: " + passed + "/" + cases.length + " PASS");
    }

    private static final class Case {
        final String domain;
        final LocalDnsPolicy.Disposition expected;

        Case(String domain, LocalDnsPolicy.Disposition expected) {
            this.domain = domain;
            this.expected = expected;
        }
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
            for (int index = 0; index < label.length(); index++) data[offset++] = (byte) label.charAt(index);
        }
        data[offset++] = 0;
        data[offset++] = 0;
        data[offset++] = 1;
        data[offset++] = 0;
        data[offset] = 1;
        return data;
    }
}
