package com.project.gateway_pagamento.security;

import com.project.gateway_pagamento.merchants.MerchantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.MDC;

import java.util.List;
import java.io.IOException;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final MerchantRepository merchantRepository;

    private final RateLimiterService rateLimiterService;

    private static final int RATE_LIMIT_PER_MINUTE = 60;

    private static final String RATE_LIMIT_CODE = "rate_limit_exceeded";

    public ApiKeyAuthFilter(MerchantRepository merchantRepository, RateLimiterService rateLimiterService) {

        this.merchantRepository = merchantRepository;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var merchantOpt = merchantRepository.findByApiKey(apiKey);
        if (merchantOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var merchant = merchantOpt.get();
        if (!merchant.isActive()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        var decision = rateLimiterService.consume(merchant.getId());
        if (!decision.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
            response.setHeader("X-RateLimit-Limit", "60");
            response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remainingTokens()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(decision.retryAfterSeconds()));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"code\":\"rate_limit_exceeded\",\"message\":\"Too many requests\",\"retryAfterSeconds\":"
                            + decision.retryAfterSeconds() + "}"
            );
            return;
        }

        try {
            MerchantContext.set(merchant);

            MDC.put("merchantId", merchant.getId());

            var auth = new UsernamePasswordAuthenticationToken(
                    merchant.getId(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
            MerchantContext.clear();
            MDC.remove("merchantId");
        }
    }
}
