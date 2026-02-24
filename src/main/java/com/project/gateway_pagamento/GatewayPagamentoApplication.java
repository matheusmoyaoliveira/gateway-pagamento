package com.project.gateway_pagamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class GatewayPagamentoApplication {

	@EnableScheduling
	public static void main(String[] args) {
		SpringApplication.run(GatewayPagamentoApplication.class, args);
	}

}
