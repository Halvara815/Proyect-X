package com.proyectx.focus;

import java.util.Arrays;

/** Regression checks for the untrusted packet boundary before Android's VPN service consumes it. */
public final class DnsVpnPacketCodecSmokeTest {
    private static final byte[] DNS_QUERY = {
        0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 'e', 'x', 'a', 'm', 'p', 'l', 'e',
        0x03, 'c', 'o', 'm', 0x00, 0x00, 0x01, 0x00, 0x01
    };

    public static void main(String[] args) {
        verifyIpv4RoundTrip();
        verifyIpv6RoundTrip();
        verifyRejectsUnsafePackets();
        System.out.println("DNS VPN packet codec: PASS");
    }

    private static void verifyIpv4RoundTrip() {
        byte[] query = ipv4Query();
        DnsVpnPacket decoded = DnsVpnPacketCodec.parseDnsQuery(query, query.length);
        require(decoded != null && decoded.ipVersion() == 4, "IPv4 DNS query should decode");
        require(decoded.sourcePort() == 42_000 && decoded.destinationPort() == 53, "IPv4 ports should survive");
        byte[] response = DnsResponseBuilder.blockedNxDomain(decoded.dnsPayload());
        byte[] packet = DnsVpnPacketCodec.responseFor(decoded, response);
        require(packet.length == 20 + 8 + response.length, "IPv4 response length should match DNS");
        require((packet[0] >>> 4) == 4 && (packet[9] & 0xff) == 17, "IPv4 response header should be UDP");
        require(Arrays.equals(Arrays.copyOfRange(packet, 12, 16), new byte[] {10, 0, 0, 1}), "IPv4 source should swap");
        require(
                Arrays.equals(Arrays.copyOfRange(packet, 16, 20), new byte[] {10, 0, 0, 2}),
                "IPv4 destination should swap");
        require(
                unsignedShort(packet, 20) == 53 && unsignedShort(packet, 22) == 42_000,
                "IPv4 response ports should swap");
        require(checksum(packet, 0, 20) == 0xffff, "IPv4 header checksum should validate");
        require(packet[28] == DNS_QUERY[0] && packet[29] == DNS_QUERY[1], "IPv4 DNS transaction ID should survive");
    }

    private static void verifyIpv6RoundTrip() {
        byte[] query = ipv6Query();
        DnsVpnPacket decoded = DnsVpnPacketCodec.parseDnsQuery(query, query.length);
        require(decoded != null && decoded.ipVersion() == 6, "IPv6 DNS query should decode");
        byte[] response = DnsResponseBuilder.blockedNxDomain(decoded.dnsPayload());
        byte[] packet = DnsVpnPacketCodec.responseFor(decoded, response);
        require(packet.length == 40 + 8 + response.length, "IPv6 response length should match DNS");
        require((packet[0] >>> 4) == 6 && (packet[6] & 0xff) == 17, "IPv6 response header should be UDP");
        require(unsignedShort(packet, 46) != 0, "IPv6 requires a nonzero UDP checksum");
        require(ipv6UdpChecksum(packet) == 0xffff, "IPv6 UDP checksum should validate");
        require(packet[48] == DNS_QUERY[0] && packet[49] == DNS_QUERY[1], "IPv6 DNS transaction ID should survive");
    }

    private static void verifyRejectsUnsafePackets() {
        byte[] wrongPort = ipv4Query();
        wrongPort[22] = 0x35;
        wrongPort[23] = 0x34;
        require(DnsVpnPacketCodec.parseDnsQuery(wrongPort, wrongPort.length) == null, "Non-DNS port must be ignored");
        byte[] fragmented = ipv4Query();
        fragmented[6] = 0x20;
        require(
                DnsVpnPacketCodec.parseDnsQuery(fragmented, fragmented.length) == null,
                "Fragmented IPv4 must be ignored");
        byte[] oversized = new byte[DnsVpnPacketCodec.MAX_PACKET_BYTES + 1];
        require(
                DnsVpnPacketCodec.parseDnsQuery(oversized, oversized.length) == null,
                "Oversized packet must be ignored");
    }

    private static byte[] ipv4Query() {
        byte[] packet = new byte[20 + 8 + DNS_QUERY.length];
        packet[0] = 0x45;
        packet[2] = 0;
        packet[3] = (byte) packet.length;
        packet[8] = 64;
        packet[9] = 17;
        packet[12] = 10;
        packet[15] = 2;
        packet[16] = 10;
        packet[19] = 1;
        writeUdp(packet, 20, 42_000, 53);
        return packet;
    }

    private static byte[] ipv6Query() {
        byte[] packet = new byte[40 + 8 + DNS_QUERY.length];
        packet[0] = 0x60;
        int payload = packet.length - 40;
        packet[4] = (byte) (payload >>> 8);
        packet[5] = (byte) payload;
        packet[6] = 17;
        packet[7] = 64;
        packet[23] = 2;
        packet[39] = 1;
        writeUdp(packet, 40, 42_001, 53);
        return packet;
    }

    private static void writeUdp(byte[] packet, int offset, int source, int destination) {
        packet[offset] = (byte) (source >>> 8);
        packet[offset + 1] = (byte) source;
        packet[offset + 2] = (byte) (destination >>> 8);
        packet[offset + 3] = (byte) destination;
        int length = 8 + DNS_QUERY.length;
        packet[offset + 4] = (byte) (length >>> 8);
        packet[offset + 5] = (byte) length;
        System.arraycopy(DNS_QUERY, 0, packet, offset + 8, DNS_QUERY.length);
    }

    private static int unsignedShort(byte[] packet, int offset) {
        return ((packet[offset] & 0xff) << 8) | (packet[offset + 1] & 0xff);
    }

    private static int checksum(byte[] packet, int offset, int length) {
        long sum = 0;
        for (int index = 0; index < length; index += 2) {
            int high = packet[offset + index] & 0xff;
            int low = index + 1 < length ? packet[offset + index + 1] & 0xff : 0;
            sum += (high << 8) | low;
            sum = (sum & 0xffffL) + (sum >>> 16);
        }
        while ((sum >>> 16) != 0) sum = (sum & 0xffffL) + (sum >>> 16);
        return (int) sum & 0xffff;
    }

    private static int ipv6UdpChecksum(byte[] packet) {
        int udpLength = unsignedShort(packet, 4);
        byte[] pseudoAndUdp = new byte[40 + udpLength];
        System.arraycopy(packet, 8, pseudoAndUdp, 0, 32);
        pseudoAndUdp[34] = (byte) (udpLength >>> 8);
        pseudoAndUdp[35] = (byte) udpLength;
        pseudoAndUdp[39] = 17;
        System.arraycopy(packet, 40, pseudoAndUdp, 40, udpLength);
        return checksum(pseudoAndUdp, 0, pseudoAndUdp.length);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
