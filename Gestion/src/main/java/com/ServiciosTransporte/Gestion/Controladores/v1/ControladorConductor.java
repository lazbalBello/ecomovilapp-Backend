package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores.ConductorLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores.ConductorSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.ConductorUpdateDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioConductor;
import com.ServiciosTransporte.Gestion.Servicios.ServicioVehiculoAsignacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ControladorConductor {

    private final ServicioConductor servicioConductor;
    private final ServicioVehiculoAsignacion servicioVehiculoAsignacion;

    @GetMapping("/conductores/mis-vehiculos")
    @PreAuthorize("hasRole('driver')")
    public ResponseEntity<List<VehiculoLiteDto>> listarMisVehiculos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                servicioVehiculoAsignacion.listarVehiculosActivosDelConductor(jwt.getSubject()));
    }

    @PostMapping("/admin/conductores")
    public ResponseEntity<ConductorDto> registrarConductor(@Valid @RequestBody ConductorDto conductorDto){
        ConductorDto conductorRegistrado = servicioConductor.registrarConductor(conductorDto);
        return new ResponseEntity<>(conductorRegistrado, HttpStatus.CREATED);
    }

    @GetMapping("/admin/conductores/por-dni")
    public ResponseEntity<ConductorLiteDto> buscarPorDni(@RequestParam("dni") String dni) {
        ConductorLiteDto conductor = servicioConductor.buscarPorDni(dni);
        return ResponseEntity.ok(conductor);
    }

    @GetMapping("/admin/conductores/filtrar")
    public ResponseEntity<List<ConductorLiteDto>> filtrarPorDni(@RequestParam("dni") String dni){
        List<ConductorLiteDto> conductores = servicioConductor.filtrarPorDni(dni);
        return ResponseEntity.ok(conductores);
    }

    @GetMapping("/admin/conductores/sugerencias-dni")
    public ResponseEntity<List<String>> sugerirDni(@RequestParam("dni") String dni){
        List<String> sugerencias = servicioConductor.sugerirDni(dni);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("/admin/conductores/sugerencias")
    public ResponseEntity<List<ConductorSugerenciaDto>> sugerirNombresYApellidos(@RequestParam("query") String query){
        List<ConductorSugerenciaDto> sugerencias = servicioConductor.sugerirNombreYApellidos(query);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("/admin/conductores/{id}")
    public ResponseEntity<ConductorLiteDto> buscarPorId(@PathVariable Long id){
        ConductorLiteDto conductor = servicioConductor.buscarPorId(id);
        return ResponseEntity.ok(conductor);
    }

    @GetMapping("/admin/conductores")
    public ResponseEntity<List<ConductorLiteDto>> listarTodo(){
        List<ConductorLiteDto> conductores = servicioConductor.listarTodo();
        return ResponseEntity.ok(conductores);
    }

    @PatchMapping("/admin/conductores/{id}")
    public ResponseEntity<ConductorLiteDto> actualizarConductor(
            @PathVariable Long id, @Valid @RequestBody ConductorUpdateDto updateDto){
        ConductorLiteDto actualizado = servicioConductor.actualizarConductor(id, updateDto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/admin/conductores/{id}")
    public ResponseEntity<Void> eliminarConductor(@PathVariable Long id){
        servicioConductor.softDeleteConductor(id);
        return ResponseEntity.noContent().build();
    }

    /** Retorna todos los conductores eliminados (soft delete) — solo administradores. */
    @GetMapping("/admin/conductores/eliminados")
    public ResponseEntity<List<ConductorLiteDto>> listarEliminados(){
        return ResponseEntity.ok(servicioConductor.listarEliminados());
    }
}
