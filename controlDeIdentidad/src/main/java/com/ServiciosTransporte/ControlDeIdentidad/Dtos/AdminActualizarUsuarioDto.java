package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminActualizarUsuarioDto(
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        @NotBlank(message = "El apellido no puede estar vacío")
        String apellido,

        @Email(message = "El formato del email es inválido")
        @NotBlank(message = "El email no puede estar vacío")
        String email,

        @NotNull(message = "El estado habilitado es obligatorio")
        Boolean habilitado
) {}
