package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.NotNull;

public record RecorridoRutaDto(
        @NotNull(message = "Seleccione la latitud")
        Double latitud,

        @NotNull(message = "Selecciona la longitud")
        Double longitud
) {
}
