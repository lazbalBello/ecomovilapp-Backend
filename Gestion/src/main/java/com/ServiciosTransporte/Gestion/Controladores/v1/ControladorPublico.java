package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas.PublicoEstadisticasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Paradas.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Vehiculos.VehiculoPublicoDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioEstadisticasPublicas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/v1")
public class ControladorPublico {

   @Autowired
   ServicioEstadisticasPublicas servicioEstadisticasPublicas;

    @GetMapping("/vehiculo/listartodo")
    public ResponseEntity<List<VehiculoPublicoDto>> obtenerVehiculosPublicos(){
        return  ResponseEntity.ok(servicioEstadisticasPublicas.listarVehiculosPublico());
    }

    @GetMapping("/estadisticas/vehiculos-activos")
    public ResponseEntity<Long> obtenerVehiculosActivos() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.obtenerEstadisticasPublicas().getVehiculosActivos());
    }

    @GetMapping("/estadisticas/rutas-activas")
    public ResponseEntity<Long> obtenerRutasActivas() {
        return ResponseEntity.ok(servicioEstadisticasPublicas.obtenerEstadisticasPublicas().getRutasActivas());
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