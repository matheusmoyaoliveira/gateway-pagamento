package com.project.gateway_pagamento.merchants.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name="api_key", nullable = false, unique = true, length = 255)
    private String apiKey;

    public Merchant() {
    }

    public Merchant(String id, String name, MerchantStatus status, Instant createdAt, String apiKey) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.apiKey = apiKey;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MerchantStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == MerchantStatus.ACTIVE;
    }

    public String getApiKey() {
        return apiKey;
    }
}
