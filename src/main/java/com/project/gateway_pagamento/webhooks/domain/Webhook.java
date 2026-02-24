package com.project.gateway_pagamento.webhooks.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "webhooks")
public class Webhook {

    @Id
    @Column(name = "merchant_id", nullable = false, length = 255)
    private String merchantId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Webhook() {}

    public Webhook(String merchantId, String url, boolean enabled) {
        this.merchantId = merchantId;
        this.url = url;
        this.enabled = enabled;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public String getMerchantId() { return merchantId; }
    public String getUrl() { return url; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setUrl(String url) {
        this.url = url;
        this.updatedAt = Instant.now();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }
}