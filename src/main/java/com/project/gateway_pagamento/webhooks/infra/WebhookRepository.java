package com.project.gateway_pagamento.webhooks.infra;

import com.project.gateway_pagamento.webhooks.domain.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook, String> {
    Optional<Webhook> findByMerchantIdAndEnabledTrue(String merchantId);
}