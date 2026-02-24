package com.project.gateway_pagamento.webhooks.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "webhook_events")
public class WebhookEvent {

    @Id
    @Column(nullable = false, length = 255)
    private String id;

    @Column(name = "merchant_id", nullable = false, length = 255)
    private String merchantId;

    @Column(name = "payment_id", nullable = false, length = 255)
    private String paymentId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEventStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    protected WebhookEvent() {}

    public WebhookEvent(
            String id,
            String merchantId,
            String paymentId,
            String eventType,
            String payload
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.payload = payload;

        this.status = WebhookEventStatus.PENDING;
        this.attempts = 0;
        this.createdAt = Instant.now();
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public String getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public String getPaymentId() { return paymentId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public WebhookEventStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public Instant getCreatedAt() { return createdAt; }
    public String getLastError() { return lastError; }

    public void markSent() {
        this.status = WebhookEventStatus.SENT;
        this.lastError = null;
        this.nextRetryAt = null;
    }

    public void markFailed(String error, Instant nextRetryAt) {
        this.status = WebhookEventStatus.FAILED;
        this.attempts++;
        this.lastError = error;
        this.nextRetryAt = nextRetryAt;
    }
}