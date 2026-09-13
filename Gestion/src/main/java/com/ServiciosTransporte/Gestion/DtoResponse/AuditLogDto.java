package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private String id;
    private LocalDateTime timestamp;
    private String accion;
    private String categoria;
    private String detalles;
    private String usuario;
}
