package com.project.gateway_pagamento.webhooks.api;

import com.project.gateway_pagamento.webhooks.domain.Webhook;

import java.time.Instant;

public class WebhookResponse {

    private String merchantId;
    private String url;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public static WebhookResponse from(Webhook w) {
        WebhookResponse r = new WebhookResponse();
        r.merchantId = w.getMerchantId();
        r.url = w.getUrl();
        r.enabled = w.isEnabled();
        r.createdAt = w.getCreatedAt();
        r.updatedAt = w.getUpdatedAt();
        return r;
    }

    public String getMerchantId() { return merchantId; }
    public String getUrl() { return url; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}