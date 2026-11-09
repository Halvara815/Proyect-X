package com.proyectx.focus;

import java.util.List;

/** JVM-only checks that policy explanations preserve the rule-engine contract. */
public final class DomainPolicySmokeTest {
    public static void main(String[] args) {
        Rule broadBlock = Rule.create(Rule.Type.DOMAIN, Rule.Action.BLOCK, "example.com");
        Rule specificAllow = Rule.create(Rule.Type.DOMAIN, Rule.Action.ALLOW, "docs.example.com");
        DomainPolicy.Evaluation allowed = DomainPolicy.evaluateAt(
                "https://docs.example.com/start", List.of(broadBlock, specificAllow), ScheduleWindow.MONDAY, 12 * 60);
        assertEquals(DomainPolicy.Outcome.ALLOW, allowed.outcome());
        assertEquals(specificAllow.id(), allowed.matchingRule().id());
        assertEquals("docs.example.com", allowed.normalizedDomain());

        DomainPolicy.Evaluation blocked = DomainPolicy.evaluateAt(
                "shop.example.com", List.of(broadBlock, specificAllow), ScheduleWindow.MONDAY, 12 * 60);
        assertEquals(DomainPolicy.Outcome.BLOCK, blocked.outcome());
        assertEquals(broadBlock.id(), blocked.matchingRule().id());

        Rule timed = Rule.create(
                Rule.Type.DOMAIN,
                Rule.Action.BLOCK,
                "night.example",
                ScheduleWindow.of(1 << ScheduleWindow.FRIDAY, 22 * 60, 2 * 60));
        assertEquals(
                DomainPolicy.Outcome.NO_MATCH,
                DomainPolicy.evaluateAt("night.example", List.of(timed), ScheduleWindow.FRIDAY, 21 * 60)
                        .outcome());
        assertEquals(
                DomainPolicy.Outcome.BLOCK,
                DomainPolicy.evaluateAt("night.example", List.of(timed), ScheduleWindow.SATURDAY, 60)
                        .outcome());
        assertEquals(
                RuleEngine.Decision.BLOCK,
                RuleEngine.evaluateAt("night.example", null, List.of(timed), ScheduleWindow.SATURDAY, 60));

        assertEquals(
                "xn--bcher-kva.example",
                DomainPolicy.evaluateAt("bücher.example", List.of(), ScheduleWindow.MONDAY, 0)
                        .normalizedDomain());
        System.out.println("DomainPolicy smoke tests: PASS");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
