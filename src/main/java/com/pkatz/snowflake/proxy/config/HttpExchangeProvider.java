package com.pkatz.snowflake.proxy.config;

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpExchangeProvider {
    @Bean
    InMemoryHttpExchangeRepository provideHttpExchange() {
        return new InMemoryHttpExchangeRepository();
    }
}
