package com.ServiciosTransporte.Api_Gateway.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers(HttpMethod.OPTIONS, "/**" )
                                                .permitAll()
                                                .pathMatchers(
                                                                "/auth/**",
                                                                "/publico/**",
                                                                "/telemetria/v1/webhook",
                                                "/telemetria/v1/public-token",
                                                                "/admin/Vehiculo/v1/listartodo",
                                                                "/admin/Conductor/v1/listar",
                                                                "/admin/Asignacion/v1/listar",
                                                                "/admin/Ruta/v1/listar/mapa",
                                                                "/admin/Parada/v1/listar/mapa"
                                                )
                                                .permitAll()
                                                .anyExchange()
                                                .authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(Customizer.withDefaults()))
                                .build();
        }
}



