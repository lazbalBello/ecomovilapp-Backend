package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas.EstadisticasAsignacionesDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas.EstadisticasConductoresDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas.EstadisticasResumenDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas.EstadisticasRutasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas.EstadisticasVehiculosDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioEstadisticas;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/Estadisticas/v1")
public class ControladorEstadisticas {

    private final ServicioEstadisticas servicioEstadisticas;

    public ControladorEstadisticas(ServicioEstadisticas servicioEstadisticas) {
        this.servicioEstadisticas = servicioEstadisticas;
    }

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