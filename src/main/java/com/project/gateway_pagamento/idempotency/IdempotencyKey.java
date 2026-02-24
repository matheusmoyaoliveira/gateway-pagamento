package com.project.gateway_pagamento.idempotency;

import jakarta.persistence.*;
import java.time.Instant;

@IdClass(IdempotencyKeyId.class)
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @Column(name = "merchant_id", nullable = false, length = 255)
    private String merchantId;

    @Id
    @Column(length = 100)
    private String key;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private Integer responseStatus;

    @Column(nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(String merchantId, String key, String requestHash, String responseBody, int responseStatus) {
        this.merchantId = merchantId;
        this.key = key;
        this.requestHash = requestHash;
        this.responseBody = responseBody;
        this.responseStatus = responseStatus;
        this.createdAt = Instant.now();
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getKey() {
        return key;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
