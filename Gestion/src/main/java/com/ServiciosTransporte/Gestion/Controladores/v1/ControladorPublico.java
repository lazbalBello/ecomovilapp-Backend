package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.PublicoEstadisticasDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoPublicoDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioEstadisticasPublicas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publico")
@RequiredArgsConstructor
public class ControladorPublico {

    private final ServicioEstadisticasPublicas servicioEstadisticasPublicas;

    @GetMapping("/vehiculos")
    public ResponseEntity<List<VehiculoPublicoDto>> obtenerVehiculosPublicos(){
        return ResponseEntity.ok(servicioEstadisticasPublicas.listarVehiculosPublico());
    }

    @GetMapping("/estadisticas/vehiculos-activos")
    public ResponseEntity<Long> obtenerVehiculosActivos() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.obtenerEstadisticasPublicas().vehiculosActivos());
    }

    @GetMapping("/estadisticas/rutas-activas")
    public ResponseEntity<Long> obtenerRutasActivas() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.obtenerEstadisticasPublicas().rutasActivas());
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<PublicoEstadisticasDto> obtenerEstadisticasPublicas() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.obtenerEstadisticasPublicas());
    }

    @GetMapping("/rutas/mapa")
    public ResponseEntity<List<RutaMapaDto>> listarRutasParaMapa() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.listarRutasParaMapa());
    }

    @GetMapping("/paradas/mapa")
    public ResponseEntity<List<ParadaMapaDto>> listarParadasParaMapa() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.listarParadasParaMapa());
    }
}