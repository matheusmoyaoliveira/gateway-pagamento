package com.project.gateway_pagamento;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gateway_pagamento.merchants.MerchantRepository;
import com.project.gateway_pagamento.merchants.domain.Merchant;
import com.project.gateway_pagamento.merchants.domain.MerchantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class PaymentsIntegrationTest extends BaseIntegrationTest {

    private static final String API_KEY_LOJA2 = "sk_live_loja2_456";
    private static final String API_KEY_LOJAX = "sk_live_lojax_999";
    private static final String IDEMPOTENCY_KEY = "idem-test-1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MerchantRepository merchantRepository;

    @BeforeEach
    void seedMerchants() {
        merchantRepository.deleteAll();

        merchantRepository.save(new Merchant(
                "loja2",
                "Loja 2",
                MerchantStatus.ACTIVE,
                Instant.now(),
                API_KEY_LOJA2
        ));

        merchantRepository.save(new Merchant(
                "lojax",
                "Loja X",
                MerchantStatus.ACTIVE,
                Instant.now(),
                API_KEY_LOJAX
        ));
    }

    @Test
    void idempotency_replay_shouldReturnSamePaymentId() throws Exception {
        String payload = """
            {
              "amount": 5000,
              "currency": "BRL",
              "paymentMethod": "card",
              "cardToken": "tok_test_123"
            }
            """;


        String body1 = mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();


        String body2 = mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json1 = objectMapper.readTree(body1);
        JsonNode json2 = objectMapper.readTree(body2);

        assertThat(json1.get("id").asText()).isEqualTo(json2.get("id").asText());
    }

    @Test
    void idempotency_conflict_sameKeyDifferentPayload_shouldReturn409() throws Exception {
        String payloadA = """
            {
              "amount": 5000,
              "currency": "BRL",
              "paymentMethod": "card",
              "cardToken": "tok_test_123"
            }
            """;

        String payloadB = """
            {
              "amount": 6000,
              "currency": "BRL",
              "paymentMethod": "card",
              "cardToken": "tok_test_123"
            }
            """;

        mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", "idem-conflict-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadA))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", "idem-conflict-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadB))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("idempotency_conflict"));
    }

    @Test
    void idempotency_multiTenant_sameKeyDifferentMerchants_shouldCreateTwoDifferentPayments() throws Exception {
        String payload = """
            {
              "amount": 5000,
              "currency": "BRL",
              "paymentMethod": "card",
              "cardToken": "tok_test_123"
            }
            """;

        String bodyLoja2 = mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", "multi-tenant-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value("loja2"))
                .andReturn().getResponse().getContentAsString();

        String bodyLojaX = mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJAX)
                        .header("Idempotency-Key", "multi-tenant-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value("lojax"))
                .andReturn().getResponse().getContentAsString();

        JsonNode j1 = objectMapper.readTree(bodyLoja2);
        JsonNode j2 = objectMapper.readTree(bodyLojaX);

        assertThat(j1.get("id").asText()).isNotEqualTo(j2.get("id").asText());
    }

    @Test
    void listPayments_shouldReturnOnlyPaymentsOfAuthenticatedMerchant() throws Exception {

        mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", "list-seed-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 5000,
                              "currency": "BRL",
                              "paymentMethod": "card",
                              "cardToken": "tok_test_123"
                            }
                            """))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJAX)
                        .header("Idempotency-Key", "list-seed-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 7000,
                              "currency": "BRL",
                              "paymentMethod": "card",
                              "cardToken": "tok_test_123"
                            }
                            """))
                .andExpect(status().isCreated());


        mockMvc.perform(get("/v1/payments?page=0&size=10")
                        .header("X-API-Key", API_KEY_LOJA2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].merchantId").value("loja2"));


        mockMvc.perform(get("/v1/payments?page=0&size=10")
                        .header("X-API-Key", API_KEY_LOJAX))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].merchantId").value("lojax"));
    }

    @Test
    void cancelPayment_authorized_shouldReturnCanceled() throws Exception {
        String payload = """
            {
              "amount": 5000,
              "currency": "BRL",
              "paymentMethod": "card",
              "cardToken": "tok_test_123"
            }
            """;

        String createBody = mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", "cancel-seed-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(createBody).get("id").asText();

        mockMvc.perform(post("/v1/payments/{id}/cancel", paymentId)
                        .header("X-API-Key", API_KEY_LOJA2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.merchantId").value("loja2"));
    }

    @Test
    void capturePayment_canceled_shouldReturn400() throws Exception {
        String payload = """
            {
              "amount": 5000,
              "currency": "BRL",
              "paymentMethod": "card",
              "cardToken": "tok_test_123"
            }
            """;

        String createBody = mockMvc.perform(post("/v1/payments")
                        .header("X-API-Key", API_KEY_LOJA2)
                        .header("Idempotency-Key", "cancel-seed-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(createBody).get("id").asText();

        mockMvc.perform(post("/v1/payments/{id}/cancel", paymentId)
                        .header("X-API-Key", API_KEY_LOJA2))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.merchantId").value("loja2"));


        mockMvc.perform(post("/v1/payments/{id}/capture", paymentId)
                        .header("X-API-Key", API_KEY_LOJA2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_payment_status_for_capture"));
    }
}