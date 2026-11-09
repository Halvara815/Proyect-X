package com.proyectx.focus;

import java.util.List;

public final class DnsResolutionExecutorSmokeTest {
    public static void main(String[] args) {
        Rule block = Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "adult-test.invalid");
        byte[] blockedQuery = query("adult-test.invalid", 0x11, 0x22);
        LocalDnsPolicy.Result blocked = LocalDnsPolicy.evaluate(blockedQuery, List.of(block), ScheduleWindow.MONDAY, 0);
        byte[] local = DnsResolutionExecutor.execute(
                blocked, blockedQuery, 1_000L, () -> false, (query, timeout, cancellation) -> {
                    throw new AssertionError("No debe llamar al upstream bloqueado");
                });
        assertEquals(0x80, local[2] & 0x80);
        assertEquals(3, local[3] & 0x0f);

        byte[] allowedQuery = query("safe-test.invalid", 0x33, 0x44);
        LocalDnsPolicy.Result allowed = LocalDnsPolicy.evaluate(allowedQuery, List.of(block), ScheduleWindow.MONDAY, 0);
        final long[] timeoutSeen = {0L};
        byte[] forwarded = DnsResolutionExecutor.execute(
                allowed, allowedQuery, 750L, () -> false, (query, timeout, cancellation) -> {
                    timeoutSeen[0] = timeout;
                    byte[] response = query.clone();
                    response[2] = (byte) 0x80;
                    return response;
                });
        assertEquals(750L, timeoutSeen[0]);
        assertEquals(0x33, forwarded[0] & 0xff);
        expectFailure(() -> DnsResolutionExecutor.execute(
                allowed, allowedQuery, 750L, () -> true, (query, timeout, cancellation) -> query));
        expectFailure(() -> DnsResolutionExecutor.execute(
                allowed, allowedQuery, 750L, () -> false, (query, timeout, cancellation) -> new byte[] {0, 1}));
        expectFailure(() -> DnsResolutionExecutor.execute(
                allowed, allowedQuery, 5_001L, () -> false, (query, timeout, cancellation) -> query));
        System.out.println("DnsResolutionExecutor smoke tests: PASS");
    }

    private static byte[] query(String domain, int idHigh, int idLow) {
        String[] labels = domain.split("\\.");
        int length = 17;
        for (String label : labels) length += label.length() + 1;
        byte[] data = new byte[length];
        data[0] = (byte) idHigh;
        data[1] = (byte) idLow;
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

    private static void expectFailure(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            return;
        }
        throw new AssertionError("Expected failure");
    }

    private static void assertEquals(long expected, long actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
