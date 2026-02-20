package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.WebSockets.Config;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Servicios.ServicioDistribucion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketsConfig {

    @Bean
    public SimpleUrlHandlerMapping webSocketMapping(ServicioDistribucion servicioDistribucion) {
        Map<String, WebSocketHandler> map = new HashMap<>();

        // Endpoint Público: http://gateway:8080/ws-flota-publico
        map.put("/ws-flota-publico", session -> {
            Flux<WebSocketMessage> output = servicioDistribucion.getFlujoPublico()
                    .map(session::textMessage)
                    .doOnError(e -> System.err.println("Error en flujo público: " + e.getMessage()))
                    .onErrorResume(e -> Flux.empty());
            return session.send(output);
        });

        // Endpoint Admin: http://gateway:8080/ws-flota-admin
        map.put("/ws-flota-admin", session -> {
            Flux<WebSocketMessage> output = servicioDistribucion.getFlujoAdmin()
                    .map(session::textMessage);
            return session.send(output);
        });

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
