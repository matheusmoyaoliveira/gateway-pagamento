package com.project.gateway_pagamento.merchants;

import java.util.Optional;
import com.project.gateway_pagamento.merchants.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByApiKey(String apiKey);
}
