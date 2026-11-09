package com.proyectx.focus;

/** Creates a minimal local NXDOMAIN answer for an already-blocked DNS question. */
public final class DnsResponseBuilder {
    private static final int HEADER_BYTES = 12;
    private static final int FLAG_QR = 0x8000;
    private static final int FLAG_RD = 0x0100;
    private static final int RCODE_NXDOMAIN = 3;

    private DnsResponseBuilder() {}

    public static byte[] blockedNxDomain(byte[] query) {
        DnsQuestion parsed = DnsQuestionParser.parse(query);
        byte[] response = new byte[parsed.questionEndOffset()];
        System.arraycopy(query, 0, response, HEADER_BYTES, response.length - HEADER_BYTES);
        response[0] = (byte) (parsed.transactionId() >>> 8);
        response[1] = (byte) parsed.transactionId();
        int queryFlags = ((query[2] & 0xFF) << 8) | (query[3] & 0xFF);
        int responseFlags = FLAG_QR | (queryFlags & FLAG_RD) | RCODE_NXDOMAIN;
        response[2] = (byte) (responseFlags >>> 8);
        response[3] = (byte) responseFlags;
        response[4] = 0;
        response[5] = 1;
        // ANCOUNT, NSCOUNT and ARCOUNT stay zero: no resolver data or history is attached.
        return response;
    }
}
