package com.project.gateway_pagamento.idempotency;

import java.io.Serializable;
import java.util.Objects;

public class IdempotencyKeyId implements Serializable {
    private String merchantId;
    private String key;

    public IdempotencyKeyId() {}

    public IdempotencyKeyId(String merchantId, String key) {
        this.merchantId = merchantId;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdempotencyKeyId that)) return false;
        return Objects.equals(merchantId, that.merchantId) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantId, key);
    }
}
