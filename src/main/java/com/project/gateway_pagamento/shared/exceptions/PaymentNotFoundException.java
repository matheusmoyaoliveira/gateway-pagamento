package com.project.gateway_pagamento.shared.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String id) {
        super("Payment not found with id: " + id);
    }
}
