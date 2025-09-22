package com.EcoTransporte.GestionSeguimiento.Dto;

import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RutaDto implements Serializable {

    @NotBlank(message = "El nombre de la ruta no debe estar en blanco")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
            message = "El nombre no debe tener caracteres especiales")
    private String nombre;

    @NotNull(message ="Se debe seleccionar un recorrido")
    @NotEmpty(message = "Se debe seleccionar un recorrido")
    private List<RecorridoRutaDto> recorrido;

    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
            message = "La descripción no debe tener caracteres especiales")
    private String descripcion;

}
