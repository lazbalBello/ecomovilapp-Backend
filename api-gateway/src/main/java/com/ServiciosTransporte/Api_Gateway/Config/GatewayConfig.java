package com.ServiciosTransporte.Api_Gateway.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
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

    @Value("${microservicios.admin-secret}")
    private String adminSecret;

    @Value("${microservicios.user-secret}")
    private String userSecret;

    @Bean
    public RouteLocator routesLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("Gestion"
                ,r -> r.path("/admin/**")
                                .filters(GatewayFilterSpec::tokenRelay)
                                .uri(gestionUri))

                .route("servidorDeRegistroDeServicios"
                ,r -> r.path("/eureka/web")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec.setPath("/"))
                                .uri(dicoveriUri))

                .route("servidorDeRegistroDeServicios-static"
                        ,r -> r.path("/eureka/**")
                                .filters(GatewayFilterSpec -> GatewayFilterSpec
                                        .saveSession()
                                        .tokenRelay())
                                .uri(dicoveriUri))

                .route("registrarAdmin"
                        ,r ->  r.path("/auth/registrar-admin")
                                .and()
                                .header("X-API-KEY", adminSecret)
                                .filters(GatewayFilterSpec::tokenRelay)
                                .uri(controlDeIdentidadUri))

                .route("registrarDriver"
                        ,r ->  r.path("/auth/registrar-driver")
                                .and()
                                .header("X-API-KEY", adminSecret)
                                .filters(GatewayFilterSpec::tokenRelay)
                                .uri(controlDeIdentidadUri))

                .route("registrarUser"
                        ,r ->  r.path("/auth/registrar-user")
                                .and()
                                .header("X-API-KEY", userSecret)
                                .filters(GatewayFilterSpec::tokenRelay)
                                .uri(controlDeIdentidadUri))

                .route("controlDeIdentidad", r -> r
                        .path("/auth/login", "/usuarios/**", "/auth/refrescar")
                        .filters(GatewayFilterSpec::tokenRelay)
                        .uri(controlDeIdentidadUri)
                )
                .build();
    }
}
