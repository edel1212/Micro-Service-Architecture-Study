package com.yoo.gateway_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServerApplication.class, args);
	}

	/**
	 * ✅ 연결되어 있는 Micro Service의 상태를 받아 올 수 있게 끔함
	 * */
	@Bean
	public InMemoryHttpExchangeRepository httpTraceRepository() {
		return new InMemoryHttpExchangeRepository();
	}
}
