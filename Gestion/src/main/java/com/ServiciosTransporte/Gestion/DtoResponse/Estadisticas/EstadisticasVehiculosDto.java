package com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasVehiculosDto {

    private Long total;
    private Long activos;
    private Long inactivos;
    private Long enMantenimiento;;
    private Long fueraDeServicio;
    private Long ultimos7Dias;
    private Map<String, Long> porEstado;
    private Map<String, Long> porMarca;
    private Map<String, Long> porTipoBateria;
    private Long conRutaAsignada;
    private Long conGpsAsignado;
    private List<TopMarcaDto> topMarcas;
    private List<TopModeloDto> topModelos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopMarcaDto {
        private String marca;
        private Long cantidad;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopModeloDto {
        private String modelo;
        private Long cantidad;
    }
}