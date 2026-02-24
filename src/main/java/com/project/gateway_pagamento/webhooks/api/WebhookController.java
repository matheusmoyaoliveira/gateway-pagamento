package com.project.gateway_pagamento.webhooks.api;

import com.project.gateway_pagamento.merchants.domain.Merchant;
import com.project.gateway_pagamento.security.MerchantContext;
import com.project.gateway_pagamento.webhooks.domain.Webhook;
import com.project.gateway_pagamento.webhooks.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping
    public ResponseEntity<WebhookResponse> get() {
        Merchant merchant = MerchantContext.get();
        if (merchant == null) throw new IllegalStateException("Merchant não autenticado");

        Webhook webhook = webhookService.getOrNull(merchant.getId());
        if (webhook == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(WebhookResponse.from(webhook));
    }

    @PutMapping
    public ResponseEntity<WebhookResponse> upsert(@Valid @RequestBody UpsertWebhookRequest request) {
        Merchant merchant = MerchantContext.get();
        if (merchant == null) throw new IllegalStateException("Merchant não autenticado");


        String url = request.getUrl();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ResponseEntity.badRequest().build();
        }

        Webhook saved = webhookService.upsert(merchant.getId(), url, request.isEnabled());
        return ResponseEntity.ok(WebhookResponse.from(saved));
    }
}