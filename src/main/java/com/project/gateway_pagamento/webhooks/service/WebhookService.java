package com.project.gateway_pagamento.webhooks.service;

import com.project.gateway_pagamento.webhooks.domain.Webhook;
import com.project.gateway_pagamento.webhooks.infra.WebhookRepository;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private final WebhookRepository webhookRepository;

    public WebhookService(WebhookRepository webhookRepository) {
        this.webhookRepository = webhookRepository;
    }

    public Webhook getOrNull(String merchantId) {
        return webhookRepository.findById(merchantId).orElse(null);
    }

    public Webhook upsert(String merchantId, String url, boolean enabled) {
        Webhook existing = webhookRepository.findById(merchantId).orElse(null);

        if (existing == null) {
            return webhookRepository.save(new Webhook(merchantId, url, enabled));
        }

        existing.setUrl(url);
        existing.setEnabled(enabled);
        return webhookRepository.save(existing);
    }
}