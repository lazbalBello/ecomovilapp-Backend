package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.EstadoOperativoDto;
import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Servicios.SrevicioVehiculo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conductor/Vehiculo/v1")
@RequiredArgsConstructor
public class ControladorEstadoVehiculoConductor {

    private final SrevicioVehiculo servicioVehiculo;

    @GetMapping("/estado")
    public ResponseEntity<VehiculoLiteDto> consultarEstado(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(servicioVehiculo.obtenerVehiculoAsignado(emailDe(jwt), sujetoDe(jwt)));
    }

    @PatchMapping("/estado")
    public ResponseEntity<VehiculoLiteDto> cambiarEstado(
            @Valid @RequestBody EstadoOperativoDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                servicioVehiculo.actualizarEstadoOperativo(emailDe(jwt), sujetoDe(jwt), dto.getEstado())
        );
    }

    private String emailDe(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        return jwt.getClaimAsString("preferred_username");
    }

    private String sujetoDe(Jwt jwt) {
        return jwt == null ? null : jwt.getSubject();
    }
}
