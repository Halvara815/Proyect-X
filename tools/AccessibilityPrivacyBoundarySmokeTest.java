package com.proyectx.focus;

import java.nio.file.Files;
import java.nio.file.Path;

/** Guards the declared accessibility privacy boundary without requiring Android runtime access. */
public final class AccessibilityPrivacyBoundarySmokeTest {
    public static void main(String[] args) throws Exception {
        String xml = Files.readString(Path.of("app/src/main/res/xml/app_block_accessibility_service.xml"));
        String source =
                Files.readString(Path.of("app/src/main/java/com/proyectx/focus/AppBlockAccessibilityService.java"));
        require(xml.contains("android:accessibilityEventTypes=\"typeWindowStateChanged\""));
        require(xml.contains("android:canRetrieveWindowContent=\"false\""));
        require(source.contains("event.getPackageName()"));
        require(!source.contains("getSource("));
        require(!source.contains("getText("));
        require(!source.contains("getRootInActiveWindow("));
        System.out.println("Accessibility privacy boundary: PASS");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("Accessibility privacy boundary changed");
    }
}
