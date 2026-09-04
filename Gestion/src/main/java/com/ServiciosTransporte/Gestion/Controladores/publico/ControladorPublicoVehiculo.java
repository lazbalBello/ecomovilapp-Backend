package com.ServiciosTransporte.Gestion.Controladores.publico;

import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Servicios.SrevicioVehiculo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/publico/Vehiculo/v1")
public class ControladorPublicoVehiculo {

    @Autowired
    private SrevicioVehiculo servicioVehiculo;

    @GetMapping("/listartodo")
    public ResponseEntity<List<VehiculoLiteDto>> listarVehiculos() {
        return ResponseEntity.ok(servicioVehiculo.listarVehiculos());
    }
}
