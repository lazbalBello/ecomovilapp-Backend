package com.ServiciosTransporte.Api_Gateway.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String mensajeError = "Error interno del servidor en el Gateway";

        if (ex instanceof ResponseStatusException rse) {
            HttpStatus resolved = HttpStatus.resolve(rse.getStatusCode().value());
            if (resolved != null) {
                status = resolved;
            }
            if (status == HttpStatus.NOT_FOUND) {
                mensajeError = "Ruta no encontrada en el API Gateway: verifique la URL solicitada";
            } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                mensajeError = "Demasiadas peticiones: ha superado el límite de tasa permitido";
            } else if (rse.getReason() != null && !rse.getReason().isBlank()) {
                mensajeError = rse.getReason();
            }
        } else if (ex.getClass().getSimpleName().contains("NotFound")) {
            status = HttpStatus.NOT_FOUND;
            mensajeError = "Ruta no encontrada en el API Gateway: verifique la URL solicitada";
        } else if (ex.getClass().getSimpleName().contains("Timeout") || ex.getClass().getSimpleName().contains("Connect")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            mensajeError = "Servicio no disponible temporalmente";
        }

        log.warn("Error capturado en Gateway [{} {}]: {} - {}",
                exchange.getRequest().getMethod(), exchange.getRequest().getPath(), status, mensajeError);

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("estado", status.value());
        body.put("error", mensajeError);
        body.put("path", exchange.getRequest().getPath().value());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"estado\":" + status.value() + ",\"error\":\"" + mensajeError + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
