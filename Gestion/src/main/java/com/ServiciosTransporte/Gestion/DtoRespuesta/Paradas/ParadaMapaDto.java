package com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas;

public record ParadaMapaDto(
        Long id,
        String nombre,
        double latitud,
        double longitud
) {
}
