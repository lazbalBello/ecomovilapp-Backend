package com.ServiciosTransporte.Api_Gateway.Config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    /**
     * Resuelve la clave de rate limiting usando la IP del cliente.
     * Cada IP tiene su propio bucket de tokens independiente.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress()
                    .getHostAddress();
            return Mono.just(ip);
        };
    }

    /**
     * Rate limiter por defecto: 10 req/s, pico de 20.
     * Se aplica a rutas generales (admin, eureka, telemetría).
     */
    @Bean
    @Primary
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * Rate limiter estricto para rutas de autenticación: 5 req/s, pico de 10.
     * Protege contra ataques de fuerza bruta en login y registro.
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }
}
