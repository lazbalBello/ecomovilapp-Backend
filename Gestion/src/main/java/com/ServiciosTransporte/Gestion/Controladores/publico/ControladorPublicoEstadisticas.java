package com.ServiciosTransporte.Gestion.Controladores.publico;

import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasSemanalesDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioEstadisticas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publico")
public class ControladorPublicoEstadisticas {

    @Autowired
    private ServicioEstadisticas servicioEstadisticas;

    /**
     * Cuenta los vehículos, conductores y rutas creados en los últimos 7 días.
     * Solo se muestran al administrador en el dashboard (los públicos no ven el delta semanal).
     */
    @GetMapping("/estadisticas/semanales")
    public ResponseEntity<EstadisticasSemanalesDto> obtenerEstadisticasSemanales() {
        return ResponseEntity.ok(servicioEstadisticas.obtenerEstadisticasSemanales());
    }
}
