package com.proyectx.focus;

import java.util.List;
import java.util.Objects;

/** Bridges an untrusted DNS question with the local policy, without network I/O. */
public final class LocalDnsPolicy {
    public enum Disposition {
        BLOCKED_LOCALLY,
        FORWARD_REQUIRED
    }

    public static final class Result {
        private final DnsQuestion question;
        private final DomainPolicy.Evaluation policy;
        private final Disposition disposition;
        private final byte[] localResponse;

        private Result(
                DnsQuestion question, DomainPolicy.Evaluation policy, Disposition disposition, byte[] localResponse) {
            this.question = question;
            this.policy = policy;
            this.disposition = disposition;
            this.localResponse = localResponse;
        }

        public DnsQuestion question() {
            return question;
        }

        public DomainPolicy.Evaluation policy() {
            return policy;
        }

        public Disposition disposition() {
            return disposition;
        }

        public byte[] localResponse() {
            return localResponse == null ? null : localResponse.clone();
        }
    }

    private LocalDnsPolicy() {}

    public static Result evaluate(byte[] packet, List<Rule> rules, int day, int minuteOfDay) {
        Objects.requireNonNull(rules, "rules");
        DnsQuestion question = DnsQuestionParser.parse(packet);
        DomainPolicy.Evaluation policy = DomainPolicy.evaluateAt(question.domain(), rules, day, minuteOfDay);
        if (policy.outcome() == DomainPolicy.Outcome.BLOCK) {
            return new Result(
                    question, policy, Disposition.BLOCKED_LOCALLY, DnsResponseBuilder.blockedNxDomain(packet));
        }
        return new Result(question, policy, Disposition.FORWARD_REQUIRED, null);
    }
}
