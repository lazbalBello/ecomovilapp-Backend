package com.ServiciosTransporte.Gestion.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogRegistroDto {

    private String accion;
    private String categoria;
    private String detalles;
    private String usuario;
}
