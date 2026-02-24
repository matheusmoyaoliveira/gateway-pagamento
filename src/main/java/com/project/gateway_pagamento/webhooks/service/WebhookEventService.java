package com.project.gateway_pagamento.webhooks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gateway_pagamento.payments.domain.Payment;
import com.project.gateway_pagamento.webhooks.domain.WebhookEvent;
import com.project.gateway_pagamento.webhooks.infra.WebhookEventRepository;
import com.project.gateway_pagamento.webhooks.infra.WebhookRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookEventService {

    private final WebhookRepository webhookRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    public WebhookEventService(
            WebhookRepository webhookRepository,
            WebhookEventRepository webhookEventRepository,
            ObjectMapper objectMapper
    ) {
        this.webhookRepository = webhookRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.objectMapper = objectMapper;
    }

    public void enqueuePaymentEvent(Payment payment, String eventType) {
        String merchantId = payment.getMerchantId();


        boolean hasWebhook = webhookRepository.findByMerchantIdAndEnabledTrue(merchantId).isPresent();
        if (!hasWebhook) return;

        String payload = buildPayload(payment, eventType);

        WebhookEvent event = new WebhookEvent(
                UUID.randomUUID().toString(),
                merchantId,
                payment.getId(),
                eventType,
                payload
        );

        webhookEventRepository.save(event);
    }

    private String buildPayload(Payment payment, String eventType) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("type", eventType);
            root.put("occurredAt", Instant.now().toString());

            String correlationId = MDC.get("correlationId");
            if (correlationId != null && !correlationId.isBlank()) {
                root.put("correlationId", correlationId);
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", payment.getId());
            data.put("status", payment.getStatus().name());
            data.put("amount", payment.getAmount());
            data.put("currency", payment.getCurrency());
            data.put("paymentMethod", payment.getPaymentMethod());
            data.put("merchantId", payment.getMerchantId());

            root.put("data", data);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize webhook payload", e);
        }
    }
}