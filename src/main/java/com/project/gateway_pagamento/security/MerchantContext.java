package com.project.gateway_pagamento.security;

import com.project.gateway_pagamento.merchants.domain.Merchant;

public final class MerchantContext {
    private static final ThreadLocal<Merchant> CURRENT = new ThreadLocal<>();

    private MerchantContext() {}

    public static void set(Merchant merchant) {
        CURRENT.set(merchant);
    }

    public static Merchant get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
