package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

public record UsuarioRepresentacionDto(
        String id,

        String nombreUsuario,

        String email,

        boolean habilitado
) {
}
