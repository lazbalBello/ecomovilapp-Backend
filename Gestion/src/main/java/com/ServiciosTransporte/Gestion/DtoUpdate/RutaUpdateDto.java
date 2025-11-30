package com.ServiciosTransporte.Gestion.DtoUpdate;

import com.ServiciosTransporte.Gestion.Dto.RecorridoRutaDto;
import com.ServiciosTransporte.Gestion.Validacion.NotEmptyIfNotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RutaUpdateDto implements Serializable {

    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
            message = "El nombre no debe tener caracteres especiales")
    private String nombre;

    @NotEmptyIfNotNull
    private List<RecorridoRutaDto> recorrido;

    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
            message = "La descripción no debe tener caracteres especiales")
    private String descripcion;
}
