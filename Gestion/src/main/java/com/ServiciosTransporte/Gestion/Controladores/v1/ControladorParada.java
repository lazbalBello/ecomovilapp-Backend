package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.ParadaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ParadaLiteDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ParadaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.ParadaUpdateDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioParada;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/Parada/v1")
public class ControladorParada {

    @Autowired
    ServicioParada servicioParada;

    @PostMapping("/registrar")
    public ResponseEntity<ParadaLiteDto> registrarParada(@Valid @RequestBody ParadaDto paradaDto){
        ParadaLiteDto paradaRegistrada = servicioParada.registrarParada(paradaDto);
        return new ResponseEntity<>(paradaRegistrada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParadaLiteDto> buscarPorId(@PathVariable Long id){
        ParadaLiteDto parada = servicioParada.buscarPorId(id);
        return ResponseEntity.ok(parada);
    }

    @GetMapping("/sugerencia")
    public ResponseEntity<List<ParadaSugerenciaDto>> sugerirParada(@RequestParam("nombre") String nombre){
        List<ParadaSugerenciaDto> sugerencias = servicioParada.sugerirParada(nombre);
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ParadaLiteDto>> listarTodo(){
        List<ParadaLiteDto> paradas = servicioParada.listarTodo();
        return ResponseEntity.ok(paradas);
    }

    @GetMapping("/listar/mapa")
    public ResponseEntity<List<ParadaMapaDto>> listarParaMapa(){
        List<ParadaMapaDto> paradas = servicioParada.listarParaMapa();
        return ResponseEntity.ok(paradas);
    }

    @PatchMapping("/actualizar/{id}")
    public ResponseEntity<ParadaLiteDto> actualizarParada
            (@PathVariable Long id, @Valid @RequestBody ParadaUpdateDto updateDto){
        ParadaLiteDto actualizada = servicioParada.actualizarParada(id,updateDto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarParada(@PathVariable Long id){
        servicioParada.softDeleteParada(id);
        return ResponseEntity.noContent().build();
    }
}
