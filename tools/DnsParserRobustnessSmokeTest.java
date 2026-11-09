package com.proyectx.focus;

import java.util.Random;

/** Deterministic malformed-input sweep; accepted packets must still satisfy the strict parser contract. */
public final class DnsParserRobustnessSmokeTest {
    public static void main(String[] args) {
        Random random = new Random(0x50584cL);
        int attempts = 12_000;
        for (int index = 0; index < attempts; index++) {
            int length = random.nextInt(4_201);
            byte[] packet = new byte[length];
            random.nextBytes(packet);
            try {
                DnsQuestion question = DnsQuestionParser.parse(packet);
                if (question.domain().isBlank() || question.type() < 0 || question.dnsClass() != 1) {
                    throw new AssertionError("Parser accepted an invalid question at iteration " + index);
                }
            } catch (IllegalArgumentException expected) {
                // Rejection is the expected outcome for arbitrary untrusted bytes.
            }
        }
        System.out.println("Dns parser robustness sweep: " + attempts + " PASS");
    }
}
