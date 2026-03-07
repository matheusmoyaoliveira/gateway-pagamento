package com.project.gateway_pagamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GatewayPagamentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayPagamentoApplication.class, args);
	}

}
