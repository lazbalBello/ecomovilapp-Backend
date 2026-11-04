package com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas;

import com.ServiciosTransporte.Gestion.Modelos.RecorridoRuta;

import java.util.List;

public record RutaMapaDto(
        Long id,
        String nombre,
        String descripcion,
        List<RecorridoRuta> recorrido
) {
}
