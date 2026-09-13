package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class GestionVehicleLookupService {

    private final WebClient webClient;

    public GestionVehicleLookupService(@Value("${gestion.base-url:http://localhost:8080}") String gestionBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(gestionBaseUrl)
                .build();
    }

    public Mono<String> resolveVehicleId(String imei) {
        if (imei == null || imei.isBlank()) {
            return Mono.empty();
        }

        String normalized = imei.trim();

        return webClient.get()
                .uri("/admin/Vehiculo/v1/listartodo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Map.class)
                .filter(item -> item != null)
                .filter(item -> item.containsKey("imeiDispositivoGps") || item.containsKey("imei_dispositivo_gps"))
                .map(item -> {
                    Object value = item.get("imeiDispositivoGps");
                    if (value == null) {
                        value = item.get("imei_dispositivo_gps");
                    }
                    return value == null ? null : value.toString();
                })
                .filter(value -> normalized.equalsIgnoreCase(value))
                .next()
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("IMEI {} no encontrado en Gestion durante la validaición previa de telemetría.", normalized);
                    return Mono.empty();
                }))
                .map(value -> normalized)
                .onErrorResume(e -> {
                    log.warn("No se pudo validar el IMEI {} contra Gestion: {}", normalized, e.getMessage());
                    return Mono.empty();
                });
    }
}
