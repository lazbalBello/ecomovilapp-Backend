package com.ServiciosTransporte.Gestion.DtoResponse;

import com.ServiciosTransporte.Gestion.Modelos.RecorridoRuta;
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
    private List<RecorridoRuta> recorrido;
    private List<ParadaLiteDto> paradas;
    private List<String> vehiculosAsignados;
}
