package com.ServiciosTransporte.Gestion.DtoActualizacion;

import com.ServiciosTransporte.Gestion.Dto.RecorridoRutaDto;
import com.ServiciosTransporte.Gestion.Validacion.NotEmptyIfNotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record RutaUpdateDto(
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
                message = "El nombre no debe tener caracteres especiales")
        String nombre,

        @NotEmptyIfNotNull
        List<RecorridoRutaDto> recorrido,

        @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
                message = "La descripción no debe tener caracteres especiales")
        String descripcion
) {
}
