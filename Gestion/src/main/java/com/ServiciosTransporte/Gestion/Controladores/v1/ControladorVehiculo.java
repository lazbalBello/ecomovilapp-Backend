package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.VehiculoDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.VehiculoUpdateDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioVehiculo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/vehiculos")
@RequiredArgsConstructor
public class ControladorVehiculo {

    private final ServicioVehiculo servicioVehiculo;

    @GetMapping
    public ResponseEntity<List<VehiculoLiteDto>> listarVehiculos(){
        List<VehiculoLiteDto> vehiculos = servicioVehiculo.listarVehiculos();
        return ResponseEntity.ok(vehiculos);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiculoDto> registrarVehiculo(@Valid @RequestBody VehiculoDto vehiculoDto){
        VehiculoDto vehiculoRegistrado = servicioVehiculo.registrarVehiculo(vehiculoDto);
        return new ResponseEntity<>(vehiculoRegistrado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoLiteDto> buscarPorId(@PathVariable Long id){
        VehiculoLiteDto vehiculoDto = servicioVehiculo.buscarPorId(id);
        return ResponseEntity.ok(vehiculoDto);
    }

    @GetMapping("/por-matricula")
    public ResponseEntity<VehiculoLiteDto> buscarPorMatricula(@RequestParam("matricula") String matricula){
        VehiculoLiteDto vehiculo = servicioVehiculo.buscarPorMatricula(matricula);
        return ResponseEntity.ok(vehiculo);
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<VehiculoLiteDto>> filtrarPorMatricula(@RequestParam("matricula") String matricula){
        List<VehiculoLiteDto> vehiculos = servicioVehiculo.filtrarPorMatricula(matricula);
        return ResponseEntity.ok(vehiculos);
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<String>> sugerirMatricula(@RequestParam("matricula") String matricula){
        List<String> matriculas = servicioVehiculo.sugerirMaricula(matricula);
        return ResponseEntity.ok(matriculas);
    }

    @GetMapping("/gps/{gpsId}")
    public ResponseEntity<VehiculoLiteDto> buscarPorGpsId(@PathVariable String gpsId){
        VehiculoLiteDto vehiculo = servicioVehiculo.buscarPorGpsId(gpsId);
        return ResponseEntity.ok(vehiculo);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VehiculoLiteDto> actualizarVehiculo(
            @PathVariable Long id, @RequestBody @Valid VehiculoUpdateDto updateDto){
        VehiculoLiteDto actualizado = servicioVehiculo.actualizarVehiculo(id, updateDto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}/gps")
    public ResponseEntity<VehiculoLiteDto> desasociarGps(@PathVariable Long id){
        VehiculoLiteDto vehiculo = servicioVehiculo.desasociarGps(id);
        return ResponseEntity.ok(vehiculo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Long id){
        servicioVehiculo.softDeleteVehiculo(id);
        return ResponseEntity.noContent().build();
    }

    /** Retorna todos los vehículos eliminados (soft delete) — solo administradores. */
    @GetMapping("/eliminados")
    public ResponseEntity<List<VehiculoLiteDto>> listarEliminados(){
        return ResponseEntity.ok(servicioVehiculo.listarEliminados());
    }
}
