package com.proyectx.focus;

import java.util.Arrays;

/** Immutable UDP/DNS payload decoded from a packet received through the VPN TUN interface. */
public final class DnsVpnPacket {
    private final int ipVersion;
    private final byte[] sourceAddress;
    private final byte[] destinationAddress;
    private final int sourcePort;
    private final int destinationPort;
    private final byte[] dnsPayload;

    DnsVpnPacket(
            int ipVersion,
            byte[] sourceAddress,
            byte[] destinationAddress,
            int sourcePort,
            int destinationPort,
            byte[] dnsPayload) {
        if (ipVersion != 4 && ipVersion != 6) throw new IllegalArgumentException("Unsupported IP version.");
        int addressLength = ipVersion == 4 ? 4 : 16;
        if (sourceAddress == null
                || sourceAddress.length != addressLength
                || destinationAddress == null
                || destinationAddress.length != addressLength) {
            throw new IllegalArgumentException("Invalid IP address length.");
        }
        if (sourcePort < 1 || sourcePort > 65_535 || destinationPort < 1 || destinationPort > 65_535) {
            throw new IllegalArgumentException("Invalid UDP port.");
        }
        if (dnsPayload == null || dnsPayload.length < 12 || dnsPayload.length > 4_096) {
            throw new IllegalArgumentException("Invalid DNS payload length.");
        }
        this.ipVersion = ipVersion;
        this.sourceAddress = sourceAddress.clone();
        this.destinationAddress = destinationAddress.clone();
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.dnsPayload = dnsPayload.clone();
    }

    public int ipVersion() {
        return ipVersion;
    }

    public byte[] sourceAddress() {
        return sourceAddress.clone();
    }

    public byte[] destinationAddress() {
        return destinationAddress.clone();
    }

    public int sourcePort() {
        return sourcePort;
    }

    public int destinationPort() {
        return destinationPort;
    }

    public byte[] dnsPayload() {
        return dnsPayload.clone();
    }

    boolean hasAddresses(byte[] source, byte[] destination) {
        return Arrays.equals(sourceAddress, source) && Arrays.equals(destinationAddress, destination);
    }
}
