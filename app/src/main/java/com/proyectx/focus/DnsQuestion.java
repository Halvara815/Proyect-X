package com.proyectx.focus;

import java.util.Objects;

/** One validated DNS question decoded from an untrusted wire packet. */
public final class DnsQuestion {
    private final String domain;
    private final int type;
    private final int dnsClass;
    private final int transactionId;
    private final int questionEndOffset;

    DnsQuestion(String domain, int type, int dnsClass, int transactionId, int questionEndOffset) {
        this.domain = Objects.requireNonNull(domain, "domain");
        this.type = type;
        this.dnsClass = dnsClass;
        this.transactionId = transactionId;
        this.questionEndOffset = questionEndOffset;
    }

    public String domain() {
        return domain;
    }

    public int type() {
        return type;
    }

    public int dnsClass() {
        return dnsClass;
    }

    int transactionId() {
        return transactionId;
    }

    int questionEndOffset() {
        return questionEndOffset;
    }
}
