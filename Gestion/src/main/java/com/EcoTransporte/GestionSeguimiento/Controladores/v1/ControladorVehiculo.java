package com.EcoTransporte.GestionSeguimiento.Controladores.v1;

import com.EcoTransporte.GestionSeguimiento.Dto.VehiculoDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.VehiculoLiteDto;
import com.EcoTransporte.GestionSeguimiento.DtoUpdate.VehiculoUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Servicios.SrevicioVehiculo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/Vehiculo/v1")
public class ControladorVehiculo {

    @Autowired
    private SrevicioVehiculo servicioVehiculo;

    @GetMapping("/listartodo")
    public ResponseEntity<List<VehiculoLiteDto>> listarVehiculos(){
        List<VehiculoLiteDto> vehiculos = servicioVehiculo.listarVehiculos();
        return ResponseEntity.ok(vehiculos);
    }

    @PostMapping(value = "/registrar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiculoDto> registrarVehiculo( @Valid  @RequestBody VehiculoDto vehiculoDto){
        VehiculoDto vehiculoRegistrado = servicioVehiculo.registrarVehiculo(vehiculoDto);
        return new ResponseEntity<>(vehiculoRegistrado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoLiteDto> buscarPorId(@PathVariable Long id){
        VehiculoLiteDto vehiculoDto = servicioVehiculo.buscarPorId(id);
        return ResponseEntity.ok(vehiculoDto);
    }

    @GetMapping("/vehiculo/matricula")
    public ResponseEntity<VehiculoLiteDto> buscarPorMatricula
            (@RequestParam("matricula") String matricula ){
        VehiculoLiteDto vehiculo = servicioVehiculo.buscarPorMatricula(matricula);
        return ResponseEntity.ok(vehiculo);
    }

    @GetMapping("/matricula")
    public ResponseEntity<List<VehiculoLiteDto>> filtarPorMatricula
            (@RequestParam("matricula") String matricula){
        List<VehiculoLiteDto> veiculos = servicioVehiculo.filtrarPorMatricula(matricula);
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/sugerencia")
    public ResponseEntity<List<String>> sugerirMatricula
            (@RequestParam("matricula") String matricula){
        List<String> matriculas = servicioVehiculo.sugerirMaricula(matricula);
        return ResponseEntity.ok(matriculas);
    }

    @PatchMapping("/actualizar/{id}")
    public ResponseEntity<VehiculoLiteDto> actualizarVehiculo
            (@PathVariable Long id, @RequestBody @Valid VehiculoUpdateDto updateDto){
        VehiculoLiteDto actualizado = servicioVehiculo.actualizarVehiculo(id,updateDto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Long id){
        servicioVehiculo.softDeleteVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}
