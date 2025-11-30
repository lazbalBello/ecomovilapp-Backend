package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaLiteDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioAsignarRuta;
import com.ServiciosTransporte.Gestion.Servicios.ServicioRuta;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/Ruta/v1")
public class ControladorRuta {

    @Autowired
    private ServicioRuta servicioRuta;

    @Autowired
    private ServicioAsignarRuta servicioAsignarRuta;

    @PostMapping("/registrar")
    public ResponseEntity<RutaDto> registrarRuta(@Valid @RequestBody RutaDto rutaDto){
        RutaDto ruta = servicioRuta.registrarRuta(rutaDto);
        return new ResponseEntity<>(ruta, HttpStatus.CREATED);
    }

    @PutMapping("/asignar/ruta/{rutaId}/vehiculo/{vehiculoId}")
    public ResponseEntity<VehiculoLiteDto> asignarRutaAVehiculo(@PathVariable Long rutaId, @PathVariable Long vehiculoId){
        VehiculoLiteDto actualizado = servicioAsignarRuta.asignarRutaAVehiculo(rutaId,vehiculoId);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaLiteDto> buscarPorId(@PathVariable Long id){
        RutaLiteDto ruta = servicioRuta.buscarPorId(id);
        return ResponseEntity.ok(ruta);
    }

    @GetMapping("/admin/sugerencia")
    public  ResponseEntity<List<RutaSugerenciaDto>> sugerirRuta(@RequestParam("nombre") String nombre){
        List<RutaSugerenciaDto> sugerencias = servicioRuta.sugerirRuta(nombre);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<RutaLiteDto>> listarTodo(){
        List<RutaLiteDto> rutas = servicioRuta.listarTodo();
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/listar/mapa")
    public ResponseEntity<List<RutaMapaDto>> listarParaMapa(){
        List<RutaMapaDto> rutasMapa = servicioRuta.rutasParaMapa();
        return ResponseEntity.ok(rutasMapa);
    }

    @PatchMapping("/actualizar/{id}")
    public ResponseEntity<RutaLiteDto> actualizarRuta
            (@PathVariable Long id, @Valid @RequestBody RutaUpdateDto updateDto){
        RutaLiteDto actualizada = servicioRuta.actualizarRuta(id,updateDto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarRuta
            (@PathVariable Long id, @RequestParam("deleteParadas") boolean deleteParadas){
        servicioRuta.softDelete(id,deleteParadas);
        return ResponseEntity.noContent().build();
    }
}
