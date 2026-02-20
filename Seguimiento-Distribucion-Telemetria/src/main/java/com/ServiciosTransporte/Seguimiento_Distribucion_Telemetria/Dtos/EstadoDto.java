package com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstadoDto(

        @NotBlank(message = "se requiere el id del vehículo")
        String vehiculoId,
        @NotNull(message = "Se requiere el nuevo estado")
        @Max(3)
        @Min(0)
        int nuevoEsatado
) {
}
