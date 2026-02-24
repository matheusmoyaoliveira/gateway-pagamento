package com.project.gateway_pagamento.webhooks.infra;

import com.project.gateway_pagamento.webhooks.domain.WebhookEvent;
import com.project.gateway_pagamento.webhooks.domain.WebhookEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, String> {

    @Query("""
        select e from WebhookEvent e
        where e.status = 'PENDING'
           or (e.status = 'FAILED' and e.nextRetryAt is not null and e.nextRetryAt <= CURRENT_TIMESTAMP)
        order by e.createdAt asc
    """)
    List<WebhookEvent> findReadyToDispatch(Pageable pageable);
}