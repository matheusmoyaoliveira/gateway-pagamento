package com.project.gateway_pagamento.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, IdempotencyKeyId> {
    Optional<IdempotencyKey> findByMerchantIdAndKey(String merchantId, String key);
}
