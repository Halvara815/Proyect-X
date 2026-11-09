package com.proyectx.focus;

import java.util.Arrays;

/**
 * Strict codec for the small VPN surface used by Proyect X: unfragmented IPv4/IPv6 UDP packets
 * carrying one DNS message. It has no Android, socket, storage, or logging dependency.
 */
public final class DnsVpnPacketCodec {
    public static final int DNS_PORT = 53;
    public static final int MAX_PACKET_BYTES = 4_096 + 48;
    private static final int UDP_PROTOCOL = 17;
    private static final int IPV4_MIN_HEADER = 20;
    private static final int IPV6_HEADER = 40;
    private static final int UDP_HEADER = 8;

    private DnsVpnPacketCodec() {}

    /** Returns null for packets outside the DNS/UDP contract; malformed packets never throw to the TUN loop. */
    public static DnsVpnPacket parseDnsQuery(byte[] packet, int length) {
        if (packet == null || length < IPV4_MIN_HEADER || length > packet.length || length > MAX_PACKET_BYTES)
            return null;
        int version = (packet[0] >>> 4) & 0x0f;
        return switch (version) {
            case 4 -> parseIpv4(packet, length);
            case 6 -> parseIpv6(packet, length);
            default -> null;
        };
    }

    /** Builds a reply addressed to the original app. The reply never includes input IP options or extensions. */
    public static byte[] responseFor(DnsVpnPacket query, byte[] dnsResponse) {
        if (query == null || dnsResponse == null || dnsResponse.length < 12 || dnsResponse.length > 4_096) {
            throw new IllegalArgumentException("Invalid DNS response.");
        }
        return query.ipVersion() == 4 ? ipv4Response(query, dnsResponse) : ipv6Response(query, dnsResponse);
    }

    private static DnsVpnPacket parseIpv4(byte[] packet, int length) {
        int headerLength = (packet[0] & 0x0f) * 4;
        if (headerLength < IPV4_MIN_HEADER || headerLength > length) return null;
        int totalLength = unsignedShort(packet, 2);
        if (totalLength < headerLength + UDP_HEADER || totalLength > length) return null;
        if ((packet[9] & 0xff) != UDP_PROTOCOL) return null;
        int flagsAndOffset = unsignedShort(packet, 6);
        if ((flagsAndOffset & 0x3fff) != 0) return null;
        return parseUdp(packet, headerLength, totalLength, 4, 12, 16);
    }

    private static DnsVpnPacket parseIpv6(byte[] packet, int length) {
        if (length < IPV6_HEADER || (packet[6] & 0xff) != UDP_PROTOCOL) return null;
        int payloadLength = unsignedShort(packet, 4);
        int totalLength = IPV6_HEADER + payloadLength;
        if (payloadLength < UDP_HEADER || totalLength > length) return null;
        // Extension headers are intentionally not accepted. The VPN only advertises its own DNS endpoints.
        return parseUdp(packet, IPV6_HEADER, totalLength, 6, 8, 24);
    }

    private static DnsVpnPacket parseUdp(
            byte[] packet,
            int udpOffset,
            int packetEnd,
            int version,
            int sourceAddressOffset,
            int destinationAddressOffset) {
        int sourcePort = unsignedShort(packet, udpOffset);
        int destinationPort = unsignedShort(packet, udpOffset + 2);
        int udpLength = unsignedShort(packet, udpOffset + 4);
        if (destinationPort != DNS_PORT || udpLength < UDP_HEADER || udpOffset + udpLength > packetEnd) return null;
        int dnsLength = udpLength - UDP_HEADER;
        if (dnsLength < 12 || dnsLength > 4_096) return null;
        int addressLength = version == 4 ? 4 : 16;
        byte[] source = Arrays.copyOfRange(packet, sourceAddressOffset, sourceAddressOffset + addressLength);
        byte[] destination =
                Arrays.copyOfRange(packet, destinationAddressOffset, destinationAddressOffset + addressLength);
        byte[] dns = Arrays.copyOfRange(packet, udpOffset + UDP_HEADER, udpOffset + udpLength);
        try {
            return new DnsVpnPacket(version, source, destination, sourcePort, destinationPort, dns);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static byte[] ipv4Response(DnsVpnPacket query, byte[] dnsResponse) {
        int totalLength = IPV4_MIN_HEADER + UDP_HEADER + dnsResponse.length;
        byte[] packet = new byte[totalLength];
        packet[0] = 0x45;
        packet[1] = 0;
        putUnsignedShort(packet, 2, totalLength);
        packet[8] = 64;
        packet[9] = UDP_PROTOCOL;
        byte[] source = query.destinationAddress();
        byte[] destination = query.sourceAddress();
        System.arraycopy(source, 0, packet, 12, 4);
        System.arraycopy(destination, 0, packet, 16, 4);
        putUnsignedShort(packet, 10, internetChecksum(packet, 0, IPV4_MIN_HEADER));
        writeUdp(packet, IPV4_MIN_HEADER, query.destinationPort(), query.sourcePort(), dnsResponse);
        // IPv4 permits a zero UDP checksum. It avoids an incorrect checksum on fragmented legacy networks.
        return packet;
    }

    private static byte[] ipv6Response(DnsVpnPacket query, byte[] dnsResponse) {
        int udpLength = UDP_HEADER + dnsResponse.length;
        byte[] packet = new byte[IPV6_HEADER + udpLength];
        packet[0] = 0x60;
        putUnsignedShort(packet, 4, udpLength);
        packet[6] = UDP_PROTOCOL;
        packet[7] = 64;
        byte[] source = query.destinationAddress();
        byte[] destination = query.sourceAddress();
        System.arraycopy(source, 0, packet, 8, 16);
        System.arraycopy(destination, 0, packet, 24, 16);
        writeUdp(packet, IPV6_HEADER, query.destinationPort(), query.sourcePort(), dnsResponse);
        int checksum = ipv6UdpChecksum(packet, IPV6_HEADER, udpLength);
        putUnsignedShort(packet, IPV6_HEADER + 6, checksum == 0 ? 0xffff : checksum);
        return packet;
    }

    private static void writeUdp(byte[] packet, int offset, int sourcePort, int destinationPort, byte[] dns) {
        putUnsignedShort(packet, offset, sourcePort);
        putUnsignedShort(packet, offset + 2, destinationPort);
        putUnsignedShort(packet, offset + 4, UDP_HEADER + dns.length);
        System.arraycopy(dns, 0, packet, offset + UDP_HEADER, dns.length);
    }

    private static int ipv6UdpChecksum(byte[] packet, int udpOffset, int udpLength) {
        long sum = 0;
        sum = addWords(sum, packet, 8, 32);
        sum += (udpLength >>> 16) & 0xffff;
        sum += udpLength & 0xffff;
        sum += UDP_PROTOCOL;
        sum = addWords(sum, packet, udpOffset, udpLength);
        return finishChecksum(sum);
    }

    private static int internetChecksum(byte[] packet, int offset, int length) {
        return finishChecksum(addWords(0, packet, offset, length));
    }

    private static long addWords(long sum, byte[] bytes, int offset, int length) {
        for (int index = 0; index < length; index += 2) {
            int high = bytes[offset + index] & 0xff;
            int low = index + 1 < length ? bytes[offset + index + 1] & 0xff : 0;
            sum += (high << 8) | low;
            sum = (sum & 0xffffL) + (sum >>> 16);
        }
        return sum;
    }

    private static int finishChecksum(long sum) {
        while ((sum >>> 16) != 0) sum = (sum & 0xffffL) + (sum >>> 16);
        return (int) (~sum) & 0xffff;
    }

    private static int unsignedShort(byte[] packet, int offset) {
        return ((packet[offset] & 0xff) << 8) | (packet[offset + 1] & 0xff);
    }

    private static void putUnsignedShort(byte[] packet, int offset, int value) {
        packet[offset] = (byte) (value >>> 8);
        packet[offset + 1] = (byte) value;
    }
}
