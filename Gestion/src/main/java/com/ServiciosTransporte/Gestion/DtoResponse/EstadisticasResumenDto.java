package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasResumenDto {

    private Long totalVehiculos;
    private Long vehiculosActivos;
    private Long vehiculosInactivos;
    private Long vehiculosMantenimiento;
    private Long vehiculosCargando;
    private Long vehiculosFueraDeServicio;
    private Long vehiculosUltimos7Dias;
    private Map<String, Long> vehiculosPorEstado;
    private Map<String, Long> vehiculosPorMarca;
    private Map<String, Long> vehiculosPorTipoBateria;

    private Long totalRutas;
    private Long rutasConVehiculos;
    private Long rutasSinVehiculos;
    private Long rutasConParadas;
    private Long rutasSinParadas;
    private Long rutasUltimos7Dias;
    private Double promedioParadasPorRuta;

    private Long totalConductores;
    private Long conductoresDisponibles;
    private Long conductoresNoDisponibles;
    private Long conductoresUltimos7Dias;

    private Long totalAsignacionesActivas;
    private Long totalAsignacionesInactivas;
    private Long vehiculosConAsignacionActiva;
    private Long rutasConVehiculosEnAsignacionActiva;
}