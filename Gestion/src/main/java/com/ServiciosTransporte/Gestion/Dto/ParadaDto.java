package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ParadaDto(
        @NotNull(message = "Se requere una ruta")
        Long rutaID,

        @NotBlank(message = "El nombre de la parada mo debe estar en blanco")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
                message = "El nombre no debe tener caracteres especiales")
        String nombre,

        @NotNull(message = "Seleccione la ubicacion(latitud)")
        Double latitud,

        @NotNull(message = "Seleccione la ubicacion(longitud)")
        Double longitud
) {
}
