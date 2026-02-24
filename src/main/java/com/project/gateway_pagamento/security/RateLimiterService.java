package com.project.gateway_pagamento.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public record RateLimitDecision(boolean allowed, long remainingTokens, long retryAfterSeconds) {}

    private Bucket newBucket() {
        Refill refill = Refill.intervally(60, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(60, refill);
        return Bucket.builder().addLimit(limit).build();
    }


    public boolean tryConsume(String merchantId) {
        Bucket bucket = buckets.computeIfAbsent(merchantId, id -> newBucket());
        return bucket.tryConsume(1);
    }

    public RateLimitDecision consume(String merchantId) {
        Bucket bucket = buckets.computeIfAbsent(merchantId, id -> newBucket());

        var probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return new RateLimitDecision(true, probe.getRemainingTokens(), 0);
        }

        long nanosToWait = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = (long) Math.ceil(nanosToWait / 1_000_000_000.0);

        return new RateLimitDecision(false, probe.getRemainingTokens(), retryAfterSeconds);
    }

}