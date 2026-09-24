package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasAsignacionesDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasConductoresDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasResumenDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasRutasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasVehiculosDto;
import com.ServiciosTransporte.Gestion.Servicios.EstadisticasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/Estadisticas/v1")
public class ControladorEstadisticas {

    private final EstadisticasService estadisticasService;

    public ControladorEstadisticas(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<EstadisticasResumenDto> obtenerResumen() {
        return ResponseEntity.ok(estadisticasService.obtenerResumen());
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<EstadisticasVehiculosDto> obtenerEstadisticasVehiculos() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasVehiculos());
    }

    @GetMapping("/rutas")
    public ResponseEntity<EstadisticasRutasDto> obtenerEstadisticasRutas() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasRutas());
    }

    @GetMapping("/conductores")
    public ResponseEntity<EstadisticasConductoresDto> obtenerEstadisticasConductores() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasConductores());
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<EstadisticasAsignacionesDto> obtenerEstadisticasAsignaciones() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasAsignaciones());
    }
}