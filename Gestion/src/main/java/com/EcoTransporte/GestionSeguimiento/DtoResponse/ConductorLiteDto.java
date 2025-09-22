package com.EcoTransporte.GestionSeguimiento.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConductorLiteDto implements Serializable {

    private Long id;
    private String dni;
    private String nombre;
    private String apellidos;
    private List<AsignacionLiteDto> historialAsignaciones;
    private List<String> categoriasLicencia;
}
