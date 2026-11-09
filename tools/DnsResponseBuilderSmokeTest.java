package com.proyectx.focus;

public final class DnsResponseBuilderSmokeTest {
    public static void main(String[] args) {
        byte[] query = query("adult-test.invalid");
        query[0] = 0x12;
        query[1] = 0x34;
        query[2] = 0x01; // recursion desired
        byte[] response = DnsResponseBuilder.blockedNxDomain(query);
        assertEquals(0x12, response[0] & 0xFF);
        assertEquals(0x34, response[1] & 0xFF);
        assertEquals(0x81, response[2] & 0xFF);
        assertEquals(0x03, response[3] & 0xFF);
        assertEquals(1, unsignedShort(response, 4));
        assertEquals(0, unsignedShort(response, 6));
        assertEquals(0, unsignedShort(response, 8));
        assertEquals(0, unsignedShort(response, 10));
        assertEquals(query.length, response.length);
        System.out.println("DnsResponseBuilder smoke tests: PASS");
    }

    private static byte[] query(String domain) {
        String[] labels = domain.split("\\.");
        int length = 12 + 1 + 4;
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

    private static int unsignedShort(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
