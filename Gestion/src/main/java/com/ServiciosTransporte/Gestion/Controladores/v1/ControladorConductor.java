package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ConductorLiteDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ConductorSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.ConductorUpdateDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioConductor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/Conductor/v1")
public class ControladorConductor {

    @Autowired
    private ServicioConductor servicioConductor;

    @PostMapping("/registrar")
    public ResponseEntity<ConductorDto> registrarConductor(@Valid @RequestBody ConductorDto conductorDto){
        ConductorDto conductorRegistrado = servicioConductor.registrarConductor(conductorDto);
        return new ResponseEntity<>(conductorRegistrado, HttpStatus.CREATED);
    }

    @GetMapping("/conductordni")
    public ResponseEntity<ConductorLiteDto> buscarPorDni(
            @RequestParam("dni") String dni) {
        ConductorLiteDto conductor = servicioConductor.buscarPorDni(dni);
        return ResponseEntity.ok(conductor);
    }

    @GetMapping("/dni")
    public ResponseEntity<List<ConductorLiteDto>> filtrarPorDni(
            @RequestParam("dni") String dni){
        List<ConductorLiteDto> conductores = servicioConductor.filtrarPorDni(dni);
        return ResponseEntity.ok(conductores);
    }

    @GetMapping("/sugerencia")
    public ResponseEntity<List<String>> sugerirDni(
            @RequestParam("dni") String dni){
        List<String> sugerencias = servicioConductor.sugerirDni(dni);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("/sugerencia/nombre")
    public ResponseEntity<List<ConductorSugerenciaDto>> sigerirNomrbesYapellidos(
            @RequestParam("query") String query){
        List<ConductorSugerenciaDto> sugerencias = servicioConductor.sugerirNombreYApellidos(query);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorLiteDto> buscarPorId(@PathVariable Long id){
        ConductorLiteDto conductor = servicioConductor.buscarPorId(id);
        return ResponseEntity.ok(conductor);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ConductorLiteDto>> listarTodo(){
        List<ConductorLiteDto> conductores = servicioConductor.listarTodo();
        return ResponseEntity.ok(conductores);
    }

    @PatchMapping("/actualizar/{id}")
    public ResponseEntity<ConductorLiteDto> actualizarConductor
            (@PathVariable Long id, @Valid @RequestBody ConductorUpdateDto updateDto){
        ConductorLiteDto actualizado = servicioConductor.actualizarConductor(id,updateDto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarConductor(@PathVariable Long id){
        servicioConductor.softDeleteConductor(id);
        return ResponseEntity.noContent().build();
    }
}
