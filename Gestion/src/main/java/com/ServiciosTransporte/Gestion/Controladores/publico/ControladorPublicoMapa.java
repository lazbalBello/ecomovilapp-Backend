package com.ServiciosTransporte.Gestion.Controladores.publico;

import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioParada;
import com.ServiciosTransporte.Gestion.Servicios.ServicioRuta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints públicos (sin autenticación) para la vista de mapa del dashboard.
 * Accesibles bajo /publico/** que en SecurityConfig tiene permitAll().
 * El API Gateway los reenvía a este microservicio.
 */
@RestController
@RequestMapping("/publico")
public class ControladorPublicoMapa {

    @Autowired
    private ServicioRuta servicioRuta;

    @Autowired
    private ServicioParada servicioParada;

    /**
     * Lista todas las rutas con sus puntos de recorrido y paradas para renderizar en mapa.
     * No requiere autenticación — visible para cualquier usuario.
     */
    @GetMapping("/Ruta/v1/listar/mapa")
    public ResponseEntity<List<RutaMapaDto>> listarRutasParaMapa() {
        return ResponseEntity.ok(servicioRuta.rutasParaMapa());
    }

    /**
     * Lista todas las paradas con sus coordenadas para renderizar en mapa.
     * No requiere autenticación — visible para cualquier usuario.
     */
    @GetMapping("/Parada/v1/listar/mapa")
    public ResponseEntity<List<ParadaMapaDto>> listarParadasParaMapa() {
        return ResponseEntity.ok(servicioParada.listarParaMapa());
    }
}
