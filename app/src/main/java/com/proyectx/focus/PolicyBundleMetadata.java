package com.proyectx.focus;

import java.util.Objects;

/** Immutable identity for a verified policy artifact, without its contents or source URL. */
public final class PolicyBundleMetadata {
    private final long version;
    private final String sha256Hex;

    public PolicyBundleMetadata(long version, String sha256Hex) {
        if (version < 1L) throw new IllegalArgumentException("La versión del bundle debe ser positiva.");
        if (sha256Hex == null || !sha256Hex.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("Se requiere un SHA-256 hexadecimal de 64 caracteres.");
        }
        this.version = version;
        this.sha256Hex = sha256Hex.toLowerCase(java.util.Locale.ROOT);
    }

    public long version() {
        return version;
    }

    public String sha256Hex() {
        return sha256Hex;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PolicyBundleMetadata)) return false;
        PolicyBundleMetadata that = (PolicyBundleMetadata) other;
        return version == that.version && sha256Hex.equals(that.sha256Hex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, sha256Hex);
    }
}
