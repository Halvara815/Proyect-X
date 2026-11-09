package com.proyectx.focus;

import java.util.List;
import java.util.Objects;

/**
 * Explainable local policy for domain rules. It deliberately has no resolver,
 * network client, persistence or UI dependency.
 */
public final class DomainPolicy {
    public enum Outcome {
        ALLOW,
        BLOCK,
        NO_MATCH
    }

    public static final class Evaluation {
        private final Outcome outcome;
        private final String normalizedDomain;
        private final Rule matchingRule;

        private Evaluation(Outcome outcome, String normalizedDomain, Rule matchingRule) {
            this.outcome = outcome;
            this.normalizedDomain = normalizedDomain;
            this.matchingRule = matchingRule;
        }

        public Outcome outcome() {
            return outcome;
        }

        public String normalizedDomain() {
            return normalizedDomain;
        }

        public Rule matchingRule() {
            return matchingRule;
        }

        public boolean isMatch() {
            return matchingRule != null;
        }
    }

    private DomainPolicy() {}

    public static Evaluation evaluateAt(String domain, List<Rule> rules, int day, int minuteOfDay) {
        Objects.requireNonNull(rules, "rules");
        String normalized = RuleEngine.normalizeDomain(domain);
        Rule allow = firstMatch(normalized, rules, Rule.Action.ALLOW, day, minuteOfDay);
        if (allow != null) return new Evaluation(Outcome.ALLOW, normalized, allow);
        Rule block = firstMatch(normalized, rules, Rule.Action.BLOCK, day, minuteOfDay);
        if (block != null) return new Evaluation(Outcome.BLOCK, normalized, block);
        return new Evaluation(Outcome.NO_MATCH, normalized, null);
    }

    private static Rule firstMatch(String domain, List<Rule> rules, Rule.Action action, int day, int minuteOfDay) {
        for (Rule rule : rules) {
            if (!rule.enabled() || rule.type() != Rule.Type.DOMAIN || rule.action() != action) continue;
            if (!rule.schedule().isActive(day, minuteOfDay)) continue;
            if (RuleEngine.matchesDomain(domain, rule.value())) return rule;
        }
        return null;
    }
}
