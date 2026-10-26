package com.ServiciosTransporte.ControlDeIdentidad.Controladores;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ActualizarPerfilDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.AdminActualizarUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.CambiarPasswordDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ConductorUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.PerfilUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.UsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.UsuarioRepresentacionDto;
import com.ServiciosTransporte.ControlDeIdentidad.Servicios.ServicioUsuarios;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ControladorUsuarios {

    private final ServicioUsuarios servicioUsuarios;

    /** Registra una cuenta de usuario estándar (público, primer acceso). */
    @PostMapping("/usuarios")
    public ResponseEntity<Void> registrarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        servicioUsuarios.createUser(usuarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Registra una cuenta de administrador (protegida además por API key en el gateway). */
    @PostMapping("/usuarios/admin")
    public ResponseEntity<Void> registrarAdministrador(@Valid @RequestBody UsuarioDto usuarioDto) {
        servicioUsuarios.createAdmin(usuarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Inicia la saga de registro de conductor: usuario en Keycloak + evento Kafka. */
    @PostMapping("/usuarios/conductores")
    public ResponseEntity<Void> registrarConductor(@Valid @RequestBody ConductorUsuarioDto userDto) {
        servicioUsuarios.createConductor(userDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /** Lista todos los usuarios del realm (Keycloak) — solo administradores. */
    @GetMapping("/admin/usuarios")
    public ResponseEntity<List<UsuarioRepresentacionDto>> listarUsuarios() {
        return ResponseEntity.ok(
                servicioUsuarios.findAllUsers().stream()
                        .map(u -> new UsuarioRepresentacionDto(u.getId(), u.getUsername(), u.getEmail(),
                                u.isEnabled() != null && u.isEnabled()))
                        .toList());
    }

    /** Actualiza información de un usuario (nombre, apellido, email, estado) — solo administradores. */
    @PutMapping("/admin/usuarios/{id}")
    public ResponseEntity<Void> actualizarUsuarioAdmin(@PathVariable String id,
                                                       @Valid @RequestBody AdminActualizarUsuarioDto dto) {
        servicioUsuarios.actualizarUsuarioPorAdmin(id, dto);
        return ResponseEntity.noContent().build();
    }

    /** Elimina el usuario de Keycloak — solo administradores. */
    @DeleteMapping("/admin/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
        servicioUsuarios.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Obtiene los datos del perfil del usuario autenticado a través de su token JWT. */
    @GetMapping("/usuarios/me")
    public ResponseEntity<PerfilUsuarioDto> obtenerPerfilPropio(@AuthenticationPrincipal Jwt jwt) {
        PerfilUsuarioDto perfil = servicioUsuarios.obtenerPerfil(jwt.getSubject());
        return ResponseEntity.ok(perfil);
    }

    /** Actualiza datos personales básicos del usuario autenticado. */
    @PatchMapping("/usuarios/me")
    public ResponseEntity<Void> actualizarPerfilPropio(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody ActualizarPerfilDto dto) {
        servicioUsuarios.actualizarPerfil(jwt.getSubject(), dto);
        return ResponseEntity.noContent().build();
    }

    /** Permite al usuario autenticado cambiar su contraseña validando la contraseña actual. */
    @PutMapping("/usuarios/me/password")
    public ResponseEntity<Void> cambiarPasswordPropio(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody CambiarPasswordDto dto) {
        servicioUsuarios.cambiarPassword(jwt.getSubject(), dto);
        return ResponseEntity.noContent().build();
    }

    /** Permite al usuario autenticado eliminar su propia cuenta (restringido a usuarios comunes). */
    @DeleteMapping("/usuarios/me")
    public ResponseEntity<Void> eliminarCuentaPropia(@AuthenticationPrincipal Jwt jwt) {
        servicioUsuarios.eliminarCuentaPropia(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
