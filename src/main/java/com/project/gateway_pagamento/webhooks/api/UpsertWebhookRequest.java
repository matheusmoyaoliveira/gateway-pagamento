package com.project.gateway_pagamento.webhooks.api;

import jakarta.validation.constraints.NotBlank;

public class UpsertWebhookRequest {

    @NotBlank
    private String url;

    private boolean enabled = true;

    public String getUrl() { return url; }
    public boolean isEnabled() { return enabled; }

    public void setUrl(String url) { this.url = url; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}