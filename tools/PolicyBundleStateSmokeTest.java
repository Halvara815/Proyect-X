package com.proyectx.focus;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;

public final class PolicyBundleStateSmokeTest {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        KeyPair keys = generator.generateKeyPair();
        byte[] first = "block adult-test.invalid\n".getBytes(StandardCharsets.UTF_8);
        byte[] second = "block adult-test.invalid\nblock new-test.invalid\n".getBytes(StandardCharsets.UTF_8);
        PolicyBundleMetadata v1 = new PolicyBundleMetadata(1L, sha256(first));
        PolicyBundleMetadata v2 = new PolicyBundleMetadata(2L, sha256(second));
        PolicyBundleState state = PolicyBundleState.initial(v1);
        state = state.acceptVerified(v2, second, sign(second, keys), keys.getPublic());
        assertEquals(v2, state.active());
        assertEquals(v1, state.rollback());
        PolicyBundleState advanced = state;
        expectFailure(() -> advanced.acceptVerified(v1, first, signUnchecked(first, keys), keys.getPublic()));
        PolicyBundleState restored = state.rollbackAndDisableUpdates();
        assertEquals(v1, restored.active());
        assertFalse(restored.updatesEnabled());
        expectFailure(() -> restored.acceptVerified(v2, second, signUnchecked(second, keys), keys.getPublic()));
        assertTrue(restored.enableUpdates().updatesEnabled());
        System.out.println("PolicyBundleState smoke tests: PASS");
    }

    private static byte[] sign(byte[] input, KeyPair keys) throws Exception {
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(keys.getPrivate());
        signer.update(input);
        return signer.sign();
    }

    private static byte[] signUnchecked(byte[] input, KeyPair keys) {
        try {
            return sign(input, keys);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private static String sha256(byte[] input) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(input))
            value.append(String.format("%02x", item & 0xff));
        return value.toString();
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            return;
        }
        throw new AssertionError("Expected rejected transition");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }

    private static void assertFalse(boolean actual) {
        if (actual) throw new AssertionError("Expected false");
    }

    private static void assertTrue(boolean actual) {
        if (!actual) throw new AssertionError("Expected true");
    }
}
