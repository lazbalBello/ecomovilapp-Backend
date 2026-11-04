package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioAsignarRuta;
import com.ServiciosTransporte.Gestion.Servicios.ServicioRuta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rutas")
@RequiredArgsConstructor
public class ControladorRuta {

    private final ServicioRuta servicioRuta;
    private final ServicioAsignarRuta servicioAsignarRuta;

    @PostMapping
    public ResponseEntity<RutaDto> registrarRuta(@Valid @RequestBody RutaDto rutaDto){
        RutaDto ruta = servicioRuta.registrarRuta(rutaDto);
        return new ResponseEntity<>(ruta, HttpStatus.CREATED);
    }

    @PutMapping("/{rutaId}/vehiculos/{vehiculoId}")
    public ResponseEntity<VehiculoLiteDto> asignarRutaAVehiculo(@PathVariable Long rutaId, @PathVariable Long vehiculoId){
        VehiculoLiteDto actualizado = servicioAsignarRuta.asignarRutaAVehiculo(rutaId, vehiculoId);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaLiteDto> buscarPorId(@PathVariable Long id){
        RutaLiteDto ruta = servicioRuta.buscarPorId(id);
        return ResponseEntity.ok(ruta);
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<RutaSugerenciaDto>> sugerirRuta(@RequestParam("nombre") String nombre){
        List<RutaSugerenciaDto> sugerencias = servicioRuta.sugerirRuta(nombre);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping
    public ResponseEntity<List<RutaLiteDto>> listarTodo(){
        List<RutaLiteDto> rutas = servicioRuta.listarTodo();
        return ResponseEntity.ok(rutas);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RutaLiteDto> actualizarRuta(
            @PathVariable Long id, @Valid @RequestBody RutaUpdateDto updateDto){
        RutaLiteDto actualizada = servicioRuta.actualizarRuta(id, updateDto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRuta(@PathVariable Long id){
        servicioRuta.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /** Retorna todas las rutas eliminadas (soft delete) — solo administradores. */
    @GetMapping("/eliminadas")
    public ResponseEntity<List<RutaLiteDto>> listarEliminadas(){
        return ResponseEntity.ok(servicioRuta.listarEliminadas());
    }
}
