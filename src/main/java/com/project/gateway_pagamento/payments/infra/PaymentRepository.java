package com.project.gateway_pagamento.payments.infra;

import com.project.gateway_pagamento.payments.domain.Payment;
import com.project.gateway_pagamento.payments.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Page<Payment> findAllByMerchantId(String merchantId, Pageable pageable);

    Page<Payment> findAllByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findAllByMerchantIdAndStatus(String merchantId, PaymentStatus status, Pageable pageable);
}
