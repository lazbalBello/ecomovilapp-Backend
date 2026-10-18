package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.EstadisticasAsignacionesDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.EstadisticasConductoresDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.EstadisticasResumenDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.EstadisticasRutasDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.EstadisticasVehiculosDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioEstadisticas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/estadisticas")
@RequiredArgsConstructor
public class ControladorEstadisticas {

    private final ServicioEstadisticas servicioEstadisticas;

    @GetMapping("/resumen")
    public ResponseEntity<EstadisticasResumenDto> obtenerResumen() {
        return ResponseEntity.ok(servicioEstadisticas.obtenerResumen());
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<EstadisticasVehiculosDto> obtenerEstadisticasVehiculos() {
        return ResponseEntity.ok(servicioEstadisticas.obtenerEstadisticasVehiculos());
    }

    @GetMapping("/rutas")
    public ResponseEntity<EstadisticasRutasDto> obtenerEstadisticasRutas() {
        return ResponseEntity.ok(servicioEstadisticas.obtenerEstadisticasRutas());
    }

    @GetMapping("/conductores")
    public ResponseEntity<EstadisticasConductoresDto> obtenerEstadisticasConductores() {
        return ResponseEntity.ok(servicioEstadisticas.obtenerEstadisticasConductores());
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<EstadisticasAsignacionesDto> obtenerEstadisticasAsignaciones() {
        return ResponseEntity.ok(servicioEstadisticas.obtenerEstadisticasAsignaciones());
    }
}