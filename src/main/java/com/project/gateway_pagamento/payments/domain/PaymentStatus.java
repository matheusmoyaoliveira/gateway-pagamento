package com.project.gateway_pagamento.payments.domain;

public enum PaymentStatus {
    CREATED,
    AUTHORIZED,
    CAPTURED,
    REFUNDED,
    CANCELED,
    DECLINED
}
