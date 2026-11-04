package com.ServiciosTransporte.ControlDeIdentidad.Controladores;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.LoginDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.MensajeRespuestaDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.RecuperarPasswordDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.RefreshToken;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Servicios.ServicioUsuarios;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.TokenClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ControladorAutenticacion {

    private final ServicioUsuarios servicioUsuarios;
    private final TokenClient tokenClient;

    /** Autentica al usuario con sus credenciales y devuelvo los tokens de acceso. */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDto loginDto) {
        TokenResponse response = servicioUsuarios.login(loginDto);
        return ResponseEntity.ok(response);
    }

    /** Renueva los tokens entregando un refresh token válido. */
    @PostMapping("/refrescar")
    public ResponseEntity<TokenResponse> refrescar(@Valid @RequestBody RefreshToken token) {
        return ResponseEntity.ok(tokenClient.refresh(token.refreshToken()));
    }

    /** Entrega un token de lectura para clientes públicos (visualización del mapa). */
    @GetMapping("/guest-token")
    public ResponseEntity<TokenResponse> guestToken() {
        return ResponseEntity.ok(tokenClient.getGuestToken());
    }

    /** Solicita el envío de un correo para restablecer la contraseña a través de Keycloak. */
    @PostMapping("/recuperar-password")
    public ResponseEntity<MensajeRespuestaDto> recuperarPassword(@Valid @RequestBody RecuperarPasswordDto dto) {
        MensajeRespuestaDto respuesta = servicioUsuarios.solicitarRecuperacionPassword(dto);
        return ResponseEntity.ok(respuesta);
    }
}
