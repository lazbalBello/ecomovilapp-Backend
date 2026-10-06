package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.DtoResponse.PublicoEstadisticasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoPublicoDto;
import com.ServiciosTransporte.Gestion.Servicios.PublicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/v1")
public class ControladorPublico {

    private final PublicoService publicoService;

    public ControladorPublico(PublicoService publicoService) {
        this.publicoService = publicoService;
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<List<VehiculoPublicoDto>> obtenerVehiculosPublicos(){
        return  ResponseEntity.ok(publicoService.listarVehiculosPublico());
    }

    @GetMapping("/estadisticas/vehiculos-activos")
    public ResponseEntity<Long> obtenerVehiculosActivos() {
        return ResponseEntity.ok(publicoService.obtenerEstadisticasPublicas().getVehiculosActivos());
    }

    @GetMapping("/estadisticas/rutas-activas")
    public ResponseEntity<Long> obtenerRutasActivas() {
        return ResponseEntity.ok(publicoService.obtenerEstadisticasPublicas().getRutasActivas());
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<PublicoEstadisticasDto> obtenerEstadisticasPublicas() {
        return ResponseEntity.ok(publicoService.obtenerEstadisticasPublicas());
    }

    @GetMapping("/rutas/mapa")
    public ResponseEntity<List<RutaMapaDto>> listarRutasParaMapa() {
        return ResponseEntity.ok(publicoService.listarRutasParaMapa());
    }

    @GetMapping("/paradas/mapa")
    public ResponseEntity<List<ParadaMapaDto>> listarParadasParaMapa() {
        return ResponseEntity.ok(publicoService.listarParadasParaMapa());
    }
}