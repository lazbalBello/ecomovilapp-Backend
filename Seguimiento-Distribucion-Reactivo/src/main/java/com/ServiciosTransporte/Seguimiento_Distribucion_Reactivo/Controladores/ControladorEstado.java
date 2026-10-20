package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Controladores;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.EstadoDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Servicios.ServicioDistribucionTelemetria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/telemetria/v1/estado")
@RequiredArgsConstructor
public class ControladorEstado {

    private final ServicioDistribucionTelemetria servicioDistribucionTelemetria;

    @PostMapping("/cambiar")
    @PreAuthorize("hasRole('driver')")
    public Mono<ResponseEntity<Void>> cambiarEstado(@RequestBody @Valid EstadoDto cambioDto) {
        return servicioDistribucionTelemetria.actualizarEstadoManual(cambioDto)
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}
