package com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Asignaciones.AsignacionLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaLiteDto;

import java.util.List;

public record VehiculoLiteDto(
        Long id,
        String matricula,
        String gpsId,
        int capacidadPersonas,
        String modelo,
        String marca,
        String tipoBateria,
        String estado,
        List<AsignacionLiteDto> asignaciones,
        RutaLiteDto ruta
) {
}
