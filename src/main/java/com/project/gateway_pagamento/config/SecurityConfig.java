package com.project.gateway_pagamento.config;

import com.project.gateway_pagamento.security.ApiKeyAuthFilter;
import com.project.gateway_pagamento.merchants.MerchantRepository;
import com.project.gateway_pagamento.security.CorrelationIdFilter;
import com.project.gateway_pagamento.security.RateLimiterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            MerchantRepository merchantRepository,
            RateLimiterService rateLimiterService
    ) throws Exception {

        var apiKeyFilter = new ApiKeyAuthFilter(merchantRepository, rateLimiterService);

        var correlationFilter = new CorrelationIdFilter();

        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(correlationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
