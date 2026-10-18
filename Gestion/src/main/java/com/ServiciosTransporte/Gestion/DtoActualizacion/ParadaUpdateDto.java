package com.ServiciosTransporte.Gestion.DtoActualizacion;

import jakarta.validation.constraints.Pattern;

public record ParadaUpdateDto(
        Long rutaID,

        @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
                message = "El nombre no debe tener caracteres especiales")
        String nombre,

        Double latitud,

        Double longitud
) {
}
