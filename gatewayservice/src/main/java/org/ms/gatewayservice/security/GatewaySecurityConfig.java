package org.ms.gatewayservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity  // Note: WebFlux, not WebSecurity for Gateway
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for the gateway
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/auth/**").permitAll()  // Allow auth endpoints
                .anyExchange().permitAll()  // Or configure as needed
            )
            .build();
    }
}