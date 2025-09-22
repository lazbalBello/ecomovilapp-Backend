package com.EcoTransporte.GestionSeguimiento.Controladores.v1;

import com.EcoTransporte.GestionSeguimiento.Dto.VehiculoAsignacionDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.AsignacionLiteDto;
import com.EcoTransporte.GestionSeguimiento.DtoUpdate.AsignacionUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Servicios.ServicioVehiculoAsignacion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/Asignacion/v1")
public class ControladorVehiculoAsignacion {

    @Autowired
    private ServicioVehiculoAsignacion servicioVehiculoAsignacion;

    @PostMapping
    public ResponseEntity<AsignacionLiteDto> asignarConductorAVehiculo(@Valid @RequestBody VehiculoAsignacionDto vadto) {
        AsignacionLiteDto asignacion = servicioVehiculoAsignacion.asignarConductorAVehiculo(vadto);
        return new ResponseEntity<>(asignacion, HttpStatus.CREATED);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<AsignacionLiteDto>> listarTodo(){
        List<AsignacionLiteDto> asignaciones = servicioVehiculoAsignacion.listarTodo();
        return ResponseEntity.ok(asignaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignacionLiteDto> buscarPorId(@PathVariable Long id){
        AsignacionLiteDto asignacion = servicioVehiculoAsignacion.buscarPorId(id);
        return ResponseEntity.ok(asignacion);
    }

    @PatchMapping("/actualizar/{id}")
    public ResponseEntity<AsignacionLiteDto> actualizarAsignacion
            (@PathVariable Long id, @RequestBody @Valid AsignacionUpdateDto updateDto){
        AsignacionLiteDto actualizada = servicioVehiculoAsignacion.actualizarAsignacion(id,updateDto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarAsignacion(@PathVariable Long id){
        servicioVehiculoAsignacion.softDeleteAsignacion(id);
        return ResponseEntity.noContent().build();
    }
}
