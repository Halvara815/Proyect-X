package com.proyectx.focus;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loads the deliberately small, licensed test policy bundled with the application. */
public final class BundledDomainPolicy {
    public static final String VERSION = "test-domain-policy-v1";
    private static final String ASSET_PATH = "policies/test-domain-policy-v1.txt";
    private static final String SHA_256 = "a7e3b377476b2f374d304dba4d3f18411afdc5cd1c40ed0a97d1c0728f8a453e";

    private BundledDomainPolicy() {}

    public static List<Rule> load(Context context) throws IOException {
        try (InputStream input = context.getAssets().open(ASSET_PATH)) {
            return parseVerified(readAll(input));
        }
    }

    static List<Rule> parseVerified(byte[] bytes) {
        if (!SHA_256.equals(sha256(bytes))) throw new IllegalStateException("La política incorporada no pasó SHA-256.");
        List<Rule> rules = new ArrayList<>();
        String[] lines = new String(bytes, StandardCharsets.UTF_8).split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] fields = line.split("\\s+");
            if (fields.length != 2 || !fields[1].endsWith(".invalid")) {
                throw new IllegalStateException("La política incorporada contiene una entrada no permitida.");
            }
            Rule.Action action;
            if ("block".equals(fields[0])) action = Rule.Action.BLOCK;
            else if ("allow".equals(fields[0])) action = Rule.Action.ALLOW;
            else throw new IllegalStateException("La política incorporada contiene una acción desconocida.");
            rules.add(new Rule("bundle-" + rules.size(), Rule.Type.DOMAIN, action, fields[1], true));
        }
        if (rules.isEmpty()) throw new IllegalStateException("La política incorporada está vacía.");
        return Collections.unmodifiableList(rules);
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1_024];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }
}
