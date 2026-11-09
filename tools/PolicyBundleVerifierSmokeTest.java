package com.proyectx.focus;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;

public final class PolicyBundleVerifierSmokeTest {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        KeyPair keys = generator.generateKeyPair();
        byte[] bundle = "block adult-test.invalid\n".getBytes(StandardCharsets.UTF_8);
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(keys.getPrivate());
        signer.update(bundle);
        byte[] signature = signer.sign();
        String hash = hex(MessageDigest.getInstance("SHA-256").digest(bundle));
        PolicyBundleVerifier.verify(bundle, hash, signature, keys.getPublic());
        expectSecurity(() -> PolicyBundleVerifier.verify(
                "changed".getBytes(StandardCharsets.UTF_8), hash, signature, keys.getPublic()));
        expectFailure(() -> PolicyBundleVerifier.verify(bundle, "invalid", signature, keys.getPublic()));
        System.out.println("PolicyBundleVerifier smoke tests: PASS");
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format("%02x", value & 0xFF));
        return result.toString();
    }

    private static void expectSecurity(Runnable operation) {
        try {
            operation.run();
        } catch (SecurityException expected) {
            return;
        }
        throw new AssertionError("Expected SecurityException");
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
