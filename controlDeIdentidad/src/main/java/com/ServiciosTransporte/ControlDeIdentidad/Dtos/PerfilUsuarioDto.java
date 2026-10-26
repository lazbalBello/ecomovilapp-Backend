package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import java.util.List;

public record PerfilUsuarioDto(
        String id,
        String username,
        String email,
        String nombre,
        String apellido,
        boolean habilitado,
        List<String> roles
) {}
