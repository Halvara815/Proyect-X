package com.proyectx.focus;

import java.nio.file.Files;
import java.nio.file.Path;

/** Guards the narrow DNS-VPN contract without starting a network tunnel in CI. */
public final class DnsVpnPrivacyBoundarySmokeTest {
    public static void main(String[] args) throws Exception {
        String manifest = Files.readString(Path.of("app/src/main/AndroidManifest.xml"));
        String source = Files.readString(Path.of("app/src/main/java/com/proyectx/focus/DnsVpnService.java"));
        String activity = Files.readString(Path.of("app/src/main/java/com/proyectx/focus/MainActivity.java"));
        require(manifest.contains("android:name=\".DnsVpnService\""));
        require(manifest.contains("android:permission=\"android.permission.BIND_VPN_SERVICE\""));
        require(manifest.contains("android:foregroundServiceType=\"specialUse\""));
        require(manifest.contains("android.permission.FOREGROUND_SERVICE_SPECIAL_USE"));
        require(source.contains("void onRevoke()"));
        require(source.contains("stopFilter(STATE_REVOKED"));
        require(source.contains("recoverIfNeeded("));
        require(source.contains("killSwitch("));
        require(source.contains("VpnService.SERVICE_INTERFACE"));
        require(source.contains("if (!protect(socket))"));
        require(source.contains("DnsVpnPacketCodec.parseDnsQuery"));
        require(source.contains("DnsResolutionExecutor.execute"));
        require(!source.contains("android.util.Log"));
        require(!source.contains("System.out"));
        require(activity.contains("VpnService.prepare(this)"));
        require(activity.contains("POST_NOTIFICATIONS"));
        require(activity.contains("DnsVpnService.recoverIfNeeded(this)"));
        require(activity.contains("DnsVpnService.killSwitch(this)"));
        require(activity.contains("REINTENTAR FILTRO DNS"));
        System.out.println("DNS VPN privacy boundary: PASS");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("DNS VPN privacy boundary changed");
    }
}
