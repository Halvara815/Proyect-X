package com.proyectx.focus;

import java.security.PublicKey;

/**
 * Pure, immutable state for a future local policy updater.
 * It accepts only a verified higher version, retains exactly one rollback target and never fetches data.
 */
public final class PolicyBundleState {
    private final PolicyBundleMetadata active;
    private final PolicyBundleMetadata rollback;
    private final boolean updatesEnabled;

    private PolicyBundleState(PolicyBundleMetadata active, PolicyBundleMetadata rollback, boolean updatesEnabled) {
        this.active = active;
        this.rollback = rollback;
        this.updatesEnabled = updatesEnabled;
    }

    public static PolicyBundleState initial(PolicyBundleMetadata bundledPolicy) {
        if (bundledPolicy == null) throw new NullPointerException("bundledPolicy");
        return new PolicyBundleState(bundledPolicy, null, true);
    }

    public PolicyBundleMetadata active() {
        return active;
    }

    public PolicyBundleMetadata rollback() {
        return rollback;
    }

    public boolean updatesEnabled() {
        return updatesEnabled;
    }

    /** Validates bytes before advancing state; callers retain ownership of any artifact storage. */
    public PolicyBundleState acceptVerified(
            PolicyBundleMetadata candidate, byte[] bundle, byte[] signature, PublicKey publicKey) {
        if (!updatesEnabled) throw new IllegalStateException("Las actualizaciones de políticas están desactivadas.");
        if (candidate == null) throw new NullPointerException("candidate");
        if (candidate.version() <= active.version()) {
            throw new IllegalArgumentException("La versión de política debe avanzar de forma estricta.");
        }
        PolicyBundleVerifier.verify(bundle, candidate.sha256Hex(), signature, publicKey);
        return new PolicyBundleState(candidate, active, true);
    }

    /** Returns to the exact prior verified metadata and leaves updates disabled until the user opts in again. */
    public PolicyBundleState rollbackAndDisableUpdates() {
        if (rollback == null) throw new IllegalStateException("No existe una política anterior para restaurar.");
        return new PolicyBundleState(rollback, null, false);
    }

    public PolicyBundleState enableUpdates() {
        return new PolicyBundleState(active, rollback, true);
    }
}
