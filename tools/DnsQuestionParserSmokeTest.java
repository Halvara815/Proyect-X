package com.proyectx.focus;

/** JVM-only malformed-packet and ordinary DNS-query checks. */
public final class DnsQuestionParserSmokeTest {
    public static void main(String[] args) {
        byte[] query = query("Example.COM", 1, 1);
        DnsQuestion parsed = DnsQuestionParser.parse(query);
        assertEquals("example.com", parsed.domain());
        assertEquals(1, parsed.type());
        assertEquals(1, parsed.dnsClass());

        byte[] response = query("example.com", 1, 1);
        response[2] = (byte) 0x80;
        expectFailure(() -> DnsQuestionParser.parse(response));
        byte[] twoQuestions = query("example.com", 1, 1);
        twoQuestions[5] = 2;
        expectFailure(() -> DnsQuestionParser.parse(twoQuestions));
        expectFailure(() -> DnsQuestionParser.parse(new byte[11]));
        byte[] compressed = query("example.com", 1, 1);
        compressed[12] = (byte) 0xC0;
        expectFailure(() -> DnsQuestionParser.parse(compressed));
        byte[] wrongClass = query("example.com", 1, 3);
        expectFailure(() -> DnsQuestionParser.parse(wrongClass));
        System.out.println("DnsQuestionParser smoke tests: PASS");
    }

    private static byte[] query(String domain, int type, int dnsClass) {
        String[] labels = domain.split("\\.");
        int length = 12 + 1 + 4;
        for (String label : labels) length += 1 + label.length();
        byte[] packet = new byte[length];
        packet[5] = 1; // QDCOUNT
        int offset = 12;
        for (String label : labels) {
            packet[offset++] = (byte) label.length();
            for (int index = 0; index < label.length(); index++) packet[offset++] = (byte) label.charAt(index);
        }
        packet[offset++] = 0;
        packet[offset++] = (byte) (type >>> 8);
        packet[offset++] = (byte) type;
        packet[offset++] = (byte) (dnsClass >>> 8);
        packet[offset] = (byte) dnsClass;
        return packet;
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
