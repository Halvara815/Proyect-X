package com.proyectx.focus;

import java.util.Objects;

/**
 * Pure DNS policy executor. An upstream is injected by a future transport; this class never opens a socket,
 * retains a query, or decides where network traffic goes.
 */
public final class DnsResolutionExecutor {
    public static final long MAX_TIMEOUT_MILLIS = 5_000L;
    private static final int MIN_DNS_PACKET_BYTES = 12;
    private static final int MAX_DNS_PACKET_BYTES = 4_096;

    public interface Cancellation {
        boolean isCancelled();
    }

    public interface Upstream {
        byte[] resolve(byte[] query, long timeoutMillis, Cancellation cancellation) throws Exception;
    }

    private DnsResolutionExecutor() {}

    /**
     * Resolves a policy result without modifying the query. A locally blocked domain never reaches the upstream.
     * Timeouts are an upper bound to be enforced by the injected transport, not a background task in this class.
     */
    public static byte[] execute(
            LocalDnsPolicy.Result decision,
            byte[] originalQuery,
            long timeoutMillis,
            Cancellation cancellation,
            Upstream upstream) {
        Objects.requireNonNull(decision, "decision");
        Objects.requireNonNull(originalQuery, "originalQuery");
        Objects.requireNonNull(cancellation, "cancellation");
        if (timeoutMillis < 1L || timeoutMillis > MAX_TIMEOUT_MILLIS) {
            throw new IllegalArgumentException("El timeout DNS debe estar entre 1 y " + MAX_TIMEOUT_MILLIS + " ms.");
        }
        if (cancellation.isCancelled()) throw new IllegalStateException("La resolución DNS fue cancelada.");
        if (decision.disposition() == LocalDnsPolicy.Disposition.BLOCKED_LOCALLY) {
            byte[] response = decision.localResponse();
            if (response == null) throw new IllegalStateException("Falta una respuesta local para el bloqueo.");
            return response;
        }
        Objects.requireNonNull(upstream, "upstream");
        final byte[] response;
        try {
            response = upstream.resolve(originalQuery.clone(), timeoutMillis, cancellation);
        } catch (Exception exception) {
            throw new IllegalStateException("El resolvedor DNS no respondió.", exception);
        }
        if (cancellation.isCancelled()) throw new IllegalStateException("La resolución DNS fue cancelada.");
        validateUpstreamResponse(originalQuery, response);
        return response.clone();
    }

    private static void validateUpstreamResponse(byte[] query, byte[] response) {
        if (response == null || response.length < MIN_DNS_PACKET_BYTES || response.length > MAX_DNS_PACKET_BYTES) {
            throw new IllegalStateException("El resolvedor devolvió un paquete DNS inválido.");
        }
        if (response[0] != query[0] || response[1] != query[1] || (response[2] & 0x80) == 0) {
            throw new IllegalStateException("La respuesta DNS no corresponde a la consulta.");
        }
    }
}
