package com.project.gateway_pagamento.webhooks.infra;

import com.project.gateway_pagamento.webhooks.domain.WebhookEvent;
import com.project.gateway_pagamento.webhooks.domain.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, String> {


    List<WebhookEvent> findTop50ByStatusInAndNextRetryAtIsNullOrNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            List<WebhookEventStatus> statuses,
            Instant now
    );
}