package com.project.gateway_pagamento.shared.exceptions;

public class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException(String merchantId) {
        super("Merchant not found with id: " + merchantId);
    }
}
