package com.project.gateway_pagamento.payments.api;

import com.project.gateway_pagamento.shared.PageResponse;
import com.project.gateway_pagamento.idempotency.IdempotencyKey;
import com.project.gateway_pagamento.idempotency.IdempotencyService;
import com.project.gateway_pagamento.merchants.domain.Merchant;
import com.project.gateway_pagamento.payments.domain.Payment;
import com.project.gateway_pagamento.payments.domain.PaymentService;
import com.project.gateway_pagamento.payments.domain.PaymentStatus;
import com.project.gateway_pagamento.security.MerchantContext;
import com.project.gateway_pagamento.shared.exceptions.BusinessException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    public PaymentController(PaymentService paymentService,
                             IdempotencyService idempotencyService) {
        this.paymentService = paymentService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        Merchant merchant = MerchantContext.get();

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {


            if (merchant == null) {
                throw new IllegalStateException("Merchant não autenticado");
            }

            Optional<IdempotencyKey> existing = idempotencyService.find(merchant.getId(), idempotencyKey);

            if (existing.isPresent()) {
                IdempotencyKey entity = existing.get();

                String incomingHash = idempotencyService.hashRequest(request);

                if (!incomingHash.equals(entity.getRequestHash())) {
                    throw BusinessException.conflict(
                            "idempotency_conflict",
                            "Idempotency-Key recused with different payload"
                    );
                }

                PaymentResponse response =
                        idempotencyService.parseResponse(entity, PaymentResponse.class);

                return ResponseEntity
                        .status(entity.getResponseStatus())
                        .body(response);
            }
        }

        Payment payment = paymentService.createPayment(request);

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getMerchantId()
        );

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.save(merchant.getId(), idempotencyKey, request, response, HttpStatus.CREATED.value());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentResponse>> listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)PaymentStatus status
    ) {
        Merchant merchant = MerchantContext.get();

        if (merchant == null) {
            throw new IllegalStateException("Merchant não autenticado");
        }

        log.info("listPayments called");

        Page<Payment> paymentsPage = paymentService.listPayments(merchant.getId(), status, page, size);

        Page<PaymentResponse> responsePage = paymentsPage.map(PaymentResponse::from);

        PageResponse<PaymentResponse> body = new PageResponse<>(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages()
        );

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {

        Payment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new com.project.gateway_pagamento.shared.exceptions.PaymentNotFoundException(id));

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getMerchantId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable String id) {
        Payment payment = paymentService.refundPayment(id);

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getMerchantId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable String id) {
        Payment payment = paymentService.capturePayment(id);

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getMerchantId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable String id) {
        Payment payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}