package com.proyectx.focus;

import java.util.List;

/** Regression cases that must remain stable before any DNS transport is introduced. */
public final class DomainPolicyRegressionSmokeTest {
    public static void main(String[] args) {
        Rule broad = Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "adult-test.invalid");
        Rule exception = Rule.create(Rule.Type.DOMAIN, Rule.Action.ALLOW, "safe.adult-test.invalid");
        assertOutcome(DomainPolicy.Outcome.BLOCK, "adult-test.invalid", List.of(broad, exception));
        assertOutcome(DomainPolicy.Outcome.BLOCK, "cdn.adult-test.invalid", List.of(broad, exception));
        assertOutcome(DomainPolicy.Outcome.ALLOW, "safe.adult-test.invalid", List.of(broad, exception));
        assertOutcome(DomainPolicy.Outcome.ALLOW, "img.safe.adult-test.invalid", List.of(broad, exception));
        assertOutcome(DomainPolicy.Outcome.NO_MATCH, "notadult-test.invalid", List.of(broad, exception));
        assertOutcome(DomainPolicy.Outcome.NO_MATCH, "new-domain.invalid", List.of(broad, exception));
        Rule idn = Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "bücher.invalid");
        assertOutcome(DomainPolicy.Outcome.BLOCK, "xn--bcher-kva.invalid", List.of(idn));
        assertOutcome(DomainPolicy.Outcome.BLOCK, "bücher.invalid", List.of(idn));
        System.out.println("DomainPolicy regression smoke tests: PASS");
    }

    private static void assertOutcome(DomainPolicy.Outcome expected, String domain, List<Rule> rules) {
        DomainPolicy.Outcome actual = DomainPolicy.evaluateAt(domain, rules, ScheduleWindow.MONDAY, 12 * 60)
                .outcome();
        if (actual != expected) throw new AssertionError(domain + ": expected " + expected + " but got " + actual);
    }
}
