package com.proyectx.focus;

import java.net.IDN;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Pure rule evaluation; it neither reads device state nor performs network I/O. */
public final class RuleEngine {
    public enum Decision {
        ALLOW,
        BLOCK,
        NO_MATCH
    }

    private RuleEngine() {}

    public static String normalizeDomain(String input) {
        if (input == null) throw new IllegalArgumentException("El dominio es obligatorio.");
        String value = input.trim().toLowerCase(Locale.ROOT);
        value = value.replaceFirst("^[a-z][a-z0-9+.-]*://", "");
        int delimiter = indexOfAny(value, '/', '?', '#');
        if (delimiter >= 0) value = value.substring(0, delimiter);
        if (value.endsWith(".")) value = value.substring(0, value.length() - 1);
        if (value.isEmpty() || value.length() > 253 || value.contains("@") || value.contains(":")) {
            throw new IllegalArgumentException("Escribe un dominio válido, sin ruta ni puerto.");
        }
        final String ascii;
        try {
            ascii = IDN.toASCII(value, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Escribe un dominio válido.", exception);
        }
        if (!ascii.contains(".") || ascii.startsWith(".") || ascii.endsWith(".")) {
            throw new IllegalArgumentException("Escribe un dominio completo.");
        }
        return ascii;
    }

    public static String normalizeKeyword(String input) {
        if (input == null) throw new IllegalArgumentException("La palabra es obligatoria.");
        String normalized = input.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (normalized.isEmpty() || normalized.length() > 80 || containsControlCharacter(normalized)) {
            throw new IllegalArgumentException("La palabra debe tener entre 1 y 80 caracteres imprimibles.");
        }
        return normalized;
    }

    public static String normalizePackageName(String input) {
        if (input == null) throw new IllegalArgumentException("El paquete Android es obligatorio.");
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+")) {
            throw new IllegalArgumentException("Escribe un paquete Android válido, por ejemplo com.ejemplo.app.");
        }
        return normalized;
    }

    public static Decision evaluate(String domain, String text, List<Rule> rules) {
        Objects.requireNonNull(rules, "rules");
        String normalizedDomain = domain == null || domain.isBlank() ? null : normalizeDomain(domain);
        String normalizedText = text == null ? "" : text.toLowerCase(Locale.ROOT);

        // Allow rules deliberately win: a user can make a specific exception to a broader block.
        if (matches(normalizedDomain, normalizedText, rules, Rule.Action.ALLOW)) return Decision.ALLOW;
        if (matches(normalizedDomain, normalizedText, rules, Rule.Action.BLOCK)) return Decision.BLOCK;
        return Decision.NO_MATCH;
    }

    public static Decision evaluatePackage(String packageName, List<Rule> rules) {
        Objects.requireNonNull(rules, "rules");
        String normalized = normalizePackageName(packageName);
        if (matchesPackage(normalized, rules, Rule.Action.ALLOW)) return Decision.ALLOW;
        if (matchesPackage(normalized, rules, Rule.Action.BLOCK)) return Decision.BLOCK;
        return Decision.NO_MATCH;
    }

    /** Evaluates domain and keyword rules only when their local schedule is active. */
    public static Decision evaluateAt(String domain, String text, List<Rule> rules, int day, int minuteOfDay) {
        Objects.requireNonNull(rules, "rules");
        String normalizedDomain = domain == null || domain.isBlank() ? null : normalizeDomain(domain);
        String normalizedText = text == null ? "" : text.toLowerCase(Locale.ROOT);

        if (matchesAt(normalizedDomain, normalizedText, rules, Rule.Action.ALLOW, day, minuteOfDay))
            return Decision.ALLOW;
        if (matchesAt(normalizedDomain, normalizedText, rules, Rule.Action.BLOCK, day, minuteOfDay))
            return Decision.BLOCK;
        return Decision.NO_MATCH;
    }

    /** Evaluates app rules only when their local schedule is active. */
    public static Decision evaluatePackageAt(String packageName, List<Rule> rules, int day, int minuteOfDay) {
        Objects.requireNonNull(rules, "rules");
        String normalized = normalizePackageName(packageName);
        if (matchesPackageAt(normalized, rules, Rule.Action.ALLOW, day, minuteOfDay)) return Decision.ALLOW;
        if (matchesPackageAt(normalized, rules, Rule.Action.BLOCK, day, minuteOfDay)) return Decision.BLOCK;
        return Decision.NO_MATCH;
    }

    private static boolean matches(String domain, String text, List<Rule> rules, Rule.Action action) {
        for (Rule rule : rules) {
            if (!rule.enabled() || rule.action() != action) continue;
            if (rule.type() == Rule.Type.DOMAIN && domain != null && matchesDomain(domain, rule.value())) return true;
            if (rule.type() == Rule.Type.KEYWORD && text.contains(rule.value())) return true;
        }
        return false;
    }

    private static boolean matchesAt(
            String domain, String text, List<Rule> rules, Rule.Action action, int day, int minuteOfDay) {
        for (Rule rule : rules) {
            if (!isActiveAt(rule, action, day, minuteOfDay)) continue;
            if (rule.type() == Rule.Type.DOMAIN && domain != null && matchesDomain(domain, rule.value())) return true;
            if (rule.type() == Rule.Type.KEYWORD && text.contains(rule.value())) return true;
        }
        return false;
    }

    private static boolean matchesPackage(String packageName, List<Rule> rules, Rule.Action action) {
        for (Rule rule : rules) {
            if (rule.enabled()
                    && rule.type() == Rule.Type.APP
                    && rule.action() == action
                    && rule.value().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesPackageAt(
            String packageName, List<Rule> rules, Rule.Action action, int day, int minuteOfDay) {
        for (Rule rule : rules) {
            if (isActiveAt(rule, action, day, minuteOfDay)
                    && rule.type() == Rule.Type.APP
                    && rule.value().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isActiveAt(Rule rule, Rule.Action action, int day, int minuteOfDay) {
        return rule.enabled() && rule.action() == action && rule.schedule().isActive(day, minuteOfDay);
    }

    static boolean matchesDomain(String candidate, String rule) {
        return candidate.equals(rule) || candidate.endsWith("." + rule);
    }

    private static int indexOfAny(String value, char... candidates) {
        int first = -1;
        for (char candidate : candidates) {
            int position = value.indexOf(candidate);
            if (position >= 0 && (first < 0 || position < first)) first = position;
        }
        return first;
    }

    private static boolean containsControlCharacter(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.isISOControl(value.charAt(index))) return true;
        }
        return false;
    }
}
