package com.ServiciosTransporte.Api_Gateway.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

        @Value("${microservicios.gestionUri}")
        private String gestionUri;

        @Value("${microservicios.discoveriUri}")
        private String dicoveriUri;

        @Value("${microservicios.controlDeIdentidad}")
        private String controlDeIdentidadUri;

        @Value("${microservicios.distribucionTelemetriaUri}")
        private String distribucionTelemetriaUri;

        @Value("${microservicios.admin-secret}")
        private String adminSecret;

        @Value("${microservicios.user-secret}")
        private String userSecret;

        private final KeyResolver ipKeyResolver;
        private final RedisRateLimiter defaultRateLimiter;
        private final RedisRateLimiter authRateLimiter;

        public GatewayConfig(
                        KeyResolver ipKeyResolver,
                        @Qualifier("defaultRateLimiter") RedisRateLimiter defaultRateLimiter,
                        @Qualifier("authRateLimiter") RedisRateLimiter authRateLimiter) {
                this.ipKeyResolver = ipKeyResolver;
                this.defaultRateLimiter = defaultRateLimiter;
                this.authRateLimiter = authRateLimiter;
        }


        @Bean
        public RouteLocator routesLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                .route("GestionPublicMap", r -> r
                                                .method("GET")
                                                .and()
                                                .path("/admin/Vehiculo/v1/listartodo",
                                                                "/admin/Conductor/v1/listar",
                                                                "/admin/Asignacion/v1/listar",
                                                                "/admin/Ruta/v1/listar",
                                                                "/admin/Ruta/v1/listar/mapa",
                                                                "/admin/Parada/v1/listar",
                                                                "/admin/Parada/v1/listar/mapa")
                                                .filters(f -> f
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(gestionUri))

                                .route("GestionConductor", r -> r.path("/conductor/**")
                                                .filters(f -> f
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(gestionUri))

                                .route("Gestion", r -> r.path("/admin/**")
                                                .filters(f -> f
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(gestionUri))

                                .route("servidorDeRegistroDeServicios", r -> r.path("/eureka/web")
                                                .filters(f -> f
                                                                .setPath("/")
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(dicoveriUri))

                                .route("servidorDeRegistroDeServicios-static", r -> r.path("/eureka/**")
                                                .filters(f -> f
                                                                .saveSession()
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(dicoveriUri))

                                .route("registrarAdmin", r -> r.path("/auth/registrar-admin")
                                                .and()
                                                .header("X-API-KEY", adminSecret)
                                                .filters(f -> f
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(authRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(controlDeIdentidadUri))

                                .route("registrarUser", r -> r.path("/auth/registrar-user")
                                                .and()
                                                .header("X-API-KEY", userSecret)
                                                .filters(f -> f
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(authRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(controlDeIdentidadUri))

                                .route("controlDeIdentidadPublic", r -> r
                                                .path("/auth/login", "/auth/refrescar", "/auth/registrar-driver")
                                                .filters(f -> f
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(authRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(controlDeIdentidadUri))

                                .route("controlDeIdentidadProtected", r -> r
                                                .path("/usuarios/**")
                                                .filters(f -> f
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(authRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(controlDeIdentidadUri))

                                .route("distribucionTelemetriaUri", r -> r
                                                .path("/telemetria/v1/estado/cambiar")
                                                .filters(f -> f
                                                                .tokenRelay()
                                                                .requestRateLimiter(config -> config
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver)))
                                                .uri(distribucionTelemetriaUri))
                                .build();
        }
}
