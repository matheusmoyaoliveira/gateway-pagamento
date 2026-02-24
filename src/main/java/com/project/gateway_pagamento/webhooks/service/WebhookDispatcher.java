package com.project.gateway_pagamento.webhooks.service;

import com.project.gateway_pagamento.webhooks.domain.WebhookEvent;
import com.project.gateway_pagamento.webhooks.domain.WebhookEventStatus;
import com.project.gateway_pagamento.webhooks.infra.WebhookEventRepository;
import com.project.gateway_pagamento.webhooks.infra.WebhookRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;

@Service
public class WebhookDispatcher {

    private final WebhookRepository webhookRepository;
    private final WebhookEventRepository eventRepository;
    private final WebClient webhookWebClient;

    public WebhookDispatcher(WebhookRepository webhookRepository, WebhookEventRepository eventRepository, WebClient webhookWebClient) {
        this.webhookRepository = webhookRepository;
        this.eventRepository = eventRepository;
        this.webhookWebClient = webhookWebClient;
    }

    @Scheduled(fixedDelay = 5000)
    public void dispatch() {
        List<WebhookEvent> events = eventRepository.findReadyToDispatch(PageRequest.of(0, 50));
        for (WebhookEvent event : events) {
            dispatchOne(event);
        }
    }

    private void dispatchOne(WebhookEvent event) {
        var webhookOpt = webhookRepository.findByMerchantIdAndEnabledTrue(event.getMerchantId());
        if (webhookOpt.isEmpty()) {
            event.markFailed("Webhook not configured/enabled", nextRetry(event.getAttempts()));
            eventRepository.save(event);
            return;
        }

        String url = webhookOpt.get().getUrl();

        try {
            webhookWebClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("X-Event-Type", event.getEventType())
                    .bodyValue(event.getPayload())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            event.markSent();
            eventRepository.save(event);

        } catch (Exception e) {
            event.markFailed(shortMsg(e), nextRetry(event.getAttempts()));
            eventRepository.save(event);
        }
    }

    private Instant nextRetry(int attempts) {

        long seconds;
        if (attempts <= 0) seconds = 10;
        else if (attempts == 1) seconds = 30;
        else if (attempts == 2) seconds = 120;
        else if (attempts == 3) seconds = 600;
        else seconds = 1800;

        return Instant.now().plusSeconds(seconds);
    }

    private String shortMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return e.getClass().getSimpleName();
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
