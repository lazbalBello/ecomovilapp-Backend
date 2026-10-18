package com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaLiteDto;

import java.util.List;

public record RutaLiteDto(
        Long id,
        String nombre,
        String descripcion,
        List<ParadaLiteDto> paradas,
        List<String> vehiculosAsignados
) {
}
