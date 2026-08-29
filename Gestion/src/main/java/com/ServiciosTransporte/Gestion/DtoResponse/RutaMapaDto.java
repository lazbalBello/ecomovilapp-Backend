package com.ServiciosTransporte.Gestion.DtoResponse;

import com.ServiciosTransporte.Gestion.Dto.RecorridoRutaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RutaMapaDto {

    private Long id;
    private String nombre;
    private String descripcion;
    private List<RecorridoRutaDto> recorrido;
    private List<ParadaLiteDto> paradas;
    private List<String> vehiculosAsignados;
}
