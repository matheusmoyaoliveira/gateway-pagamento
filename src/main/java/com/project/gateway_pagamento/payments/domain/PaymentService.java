package com.project.gateway_pagamento.payments.domain;

import com.project.gateway_pagamento.merchants.MerchantRepository;
import com.project.gateway_pagamento.merchants.domain.Merchant;
import com.project.gateway_pagamento.security.MerchantContext;
import com.project.gateway_pagamento.payments.api.CreatePaymentRequest;
import com.project.gateway_pagamento.payments.infra.PaymentRepository;
import com.project.gateway_pagamento.payments.domain.PaymentStatus;
import com.project.gateway_pagamento.webhooks.service.WebhookEventService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.project.gateway_pagamento.shared.exceptions.BusinessException;
import com.project.gateway_pagamento.shared.exceptions.PaymentNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookEventService webhookEventService;

    public PaymentService(PaymentRepository paymentRepository,
                          MerchantRepository merchantRepository,
                          WebhookEventService webhookEventService) {

        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.webhookEventService = webhookEventService;
    }

    public Payment createPayment(CreatePaymentRequest request) {
        Merchant merchant = MerchantContext.get();
        if (merchant == null) {
            throw new IllegalStateException("Merchant não autenticado");
        }

        String id = UUID.randomUUID().toString();

        Payment payment = new Payment(
                id,
                merchant.getId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod(),
                PaymentStatus.AUTHORIZED,
                Instant.now()
        );

        Payment saved = paymentRepository.save(payment);
        webhookEventService.enqueuePaymentEvent(saved, "payment.authorized");
        return saved;
    }

    public Page<Payment> listPayments(
            String merchantId,
            PaymentStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null) {
            return paymentRepository.findAllByMerchantIdAndStatus(merchantId, status, pageable);
        }

        return paymentRepository.findAllByMerchantId(merchantId, pageable);
    }

    public Payment refundPayment(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw BusinessException.badRequest(
                    "invalid_payment_status_for_refund",
                    "Payment with id " + id + " cannot be refunded from status " + payment.getStatus()
            );
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        webhookEventService.enqueuePaymentEvent(saved, "payment.refunded");
        return saved;
    }

    public Payment capturePayment(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw BusinessException.badRequest(
                    "invalid_payment_status_for_capture",
                    "Payment with id " + id + " cannot be captured from status " + payment.getStatus()
            );
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        Payment saved = paymentRepository.save(payment);
        webhookEventService.enqueuePaymentEvent(saved, "payment.captured");
        return saved;
    }

    public Optional<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }
}
