package com.project.gateway_pagamento.payments.api;

import com.project.gateway_pagamento.payments.domain.Payment;

public class PaymentResponse {

    private String id;
    private String status;
    private Long amount;
    private String currency;
    private String paymentMethod;
    private String merchantId;

    public PaymentResponse(String id, String status, Long amount, String currency, String paymentMethod, String merchantId) {
        this.id = id;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.merchantId = merchantId;
    }

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getMerchantId()
        );
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getMerchantId() {
        return merchantId;
    }
}
