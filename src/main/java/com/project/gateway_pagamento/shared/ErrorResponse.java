package com.project.gateway_pagamento.shared;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final String code;
    private final String message;
    private final Map<String, String> fields;
    private final Instant timestamp;

    public ErrorResponse(String code, String message, Map<String, String> fields) {
        this.code = code;
        this.message = message;
        this.fields = fields;
        this.timestamp = Instant.now();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
