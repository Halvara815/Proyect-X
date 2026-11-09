package com.proyectx.focus;

import java.nio.charset.StandardCharsets;

/**
 * Bounds-checked parser for one ordinary DNS query question. It does not resolve,
 * log, forward or otherwise perform I/O with the packet.
 */
public final class DnsQuestionParser {
    private static final int HEADER_BYTES = 12;
    private static final int MAX_PACKET_BYTES = 4_096;
    private static final int DNS_CLASS_IN = 1;

    private DnsQuestionParser() {}

    public static DnsQuestion parse(byte[] packet) {
        if (packet == null) throw new IllegalArgumentException("El paquete DNS es obligatorio.");
        if (packet.length < HEADER_BYTES || packet.length > MAX_PACKET_BYTES) {
            throw new IllegalArgumentException("El paquete DNS tiene un tamaño inválido.");
        }
        int flags = unsignedShort(packet, 2);
        if ((flags & 0x8000) != 0 || (flags & 0x7800) != 0) {
            throw new IllegalArgumentException("Solo se aceptan consultas DNS estándar.");
        }
        if (unsignedShort(packet, 4) != 1) {
            throw new IllegalArgumentException("La consulta DNS debe contener una sola pregunta.");
        }
        if (unsignedShort(packet, 6) != 0) {
            throw new IllegalArgumentException("La consulta DNS no debe incluir respuestas.");
        }

        int offset = HEADER_BYTES;
        StringBuilder wireName = new StringBuilder();
        while (true) {
            requireAvailable(packet, offset, 1);
            int length = packet[offset++] & 0xFF;
            if (length == 0) break;
            if ((length & 0xC0) != 0 || length > 63) {
                throw new IllegalArgumentException("La pregunta DNS usa una codificación no admitida.");
            }
            requireAvailable(packet, offset, length);
            if (wireName.length() > 0) wireName.append('.');
            for (int index = 0; index < length; index++) {
                int value = packet[offset + index] & 0xFF;
                if (value > 0x7F) throw new IllegalArgumentException("El nombre DNS debe usar ASCII en el cable.");
            }
            wireName.append(new String(packet, offset, length, StandardCharsets.US_ASCII));
            offset += length;
            if (wireName.length() > 253) throw new IllegalArgumentException("El nombre DNS es demasiado largo.");
        }
        requireAvailable(packet, offset, 4);
        int type = unsignedShort(packet, offset);
        int dnsClass = unsignedShort(packet, offset + 2);
        if (dnsClass != DNS_CLASS_IN) throw new IllegalArgumentException("Solo se admite la clase DNS IN.");
        return new DnsQuestion(
                RuleEngine.normalizeDomain(wireName.toString()), type, dnsClass, unsignedShort(packet, 0), offset + 4);
    }

    private static int unsignedShort(byte[] packet, int offset) {
        requireAvailable(packet, offset, 2);
        return ((packet[offset] & 0xFF) << 8) | (packet[offset + 1] & 0xFF);
    }

    private static void requireAvailable(byte[] packet, int offset, int length) {
        if (offset < 0 || length < 0 || offset > packet.length - length) {
            throw new IllegalArgumentException("La pregunta DNS está truncada.");
        }
    }
}
