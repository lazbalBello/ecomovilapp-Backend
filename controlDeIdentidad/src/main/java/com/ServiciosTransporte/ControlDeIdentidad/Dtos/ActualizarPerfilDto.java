package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.NotBlank;

public record ActualizarPerfilDto(
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        @NotBlank(message = "El apellido no puede estar vacío")
        String apellido
) {}
