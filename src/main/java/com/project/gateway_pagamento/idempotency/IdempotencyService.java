package com.project.gateway_pagamento.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.objectMapper = mapper;
    }

    public Optional<IdempotencyKey> find(String merchantId, String key) {

        return repository.findByMerchantIdAndKey(merchantId, key);
    }

    public void save(String merchantId, String key, Object request, Object responseBody, int status) {
        String requestHash = hash(request);
        String responseJson = toJson(responseBody);

        IdempotencyKey entity = new IdempotencyKey(merchantId, key, requestHash, responseJson, status);
        repository.save(entity);
    }

    public <T> T parseResponse(IdempotencyKey entity, Class<T> type) {
        try {
            return objectMapper.readValue(entity.getResponseBody(), type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse idempotent response", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    private String hash(Object obj) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(toJson(obj).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing idempotency request", e);
        }
    }

    public String hashRequest(Object request) {
        return hash(request);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
