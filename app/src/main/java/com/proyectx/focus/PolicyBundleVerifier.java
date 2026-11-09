package com.proyectx.focus;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;

/** Verifies a local policy artifact before a future updater is allowed to use it. */
public final class PolicyBundleVerifier {
    private PolicyBundleVerifier() {}

    public static void verify(byte[] bundle, String expectedSha256Hex, byte[] signature, PublicKey publicKey) {
        if (bundle == null || signature == null || publicKey == null) {
            throw new IllegalArgumentException("El bundle, firma y clave pública son obligatorios.");
        }
        byte[] expected = decodeSha256(expectedSha256Hex);
        final byte[] actual;
        try {
            actual = MessageDigest.getInstance("SHA-256").digest(bundle);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
        if (!MessageDigest.isEqual(expected, actual)) throw new SecurityException("El hash del bundle no coincide.");
        try {
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(publicKey);
            verifier.update(bundle);
            if (!verifier.verify(signature)) throw new SecurityException("La firma del bundle no es válida.");
        } catch (GeneralSecurityException exception) {
            throw new SecurityException("No se pudo verificar la firma del bundle.", exception);
        }
    }

    private static byte[] decodeSha256(String value) {
        if (value == null || !value.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("Se requiere un SHA-256 hexadecimal de 64 caracteres.");
        }
        byte[] bytes = new byte[32];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(value.substring(index * 2, index * 2 + 2), 16);
        }
        return bytes;
    }
}
