package com.ServiciosTransporte.ControlDeIdentidad.Servicios;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ActualizarPerfilDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.AdminActualizarUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.CambiarPasswordDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ConductorUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.LoginDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.MensajeRespuestaDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.PerfilUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.RecuperarPasswordDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.UsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.OperationNotAllowedException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserAlreadyExistsException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserCreationException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserNotFoundException;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.KeycloakProvider;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.TokenClient;
import com.servicioTransporte.flota.eventos.conductor.registro.RegistroConductorIniciado;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioUsuarios {

    private static final String TOPIC_INIT = "inicio-registro-conductor";
    private static final String ROL_ADMIN = "admin";
    private static final String ROL_USER = "user";
    private static final String ROL_DRIVER = "driver";

    private final KeycloakProvider keycloakProvider;
    private final TokenClient tokenClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RecuperacionPasswordWorker recuperacionPasswordWorker;

    public List<UserRepresentation> findAllUsers() {
        return keycloakProvider.getRealmResource()
                .users()
                .list();
    }

    public List<UserRepresentation> searchUserByUsername(String username) {
        return keycloakProvider.getRealmResource()
                .users()
                .searchByUsername(username, true);
    }

    public void createAdmin(@NonNull UsuarioDto usuarioDto) {
        registrarUsuario(usuarioDto.nombreUsuario(), usuarioDto.nombre(), usuarioDto.apellido(), usuarioDto.email(), usuarioDto.password(),
                ROL_ADMIN, "Ya está registrado este administrador", "Error al registrar el administrador");
    }

    public void createUser(@NonNull UsuarioDto usuarioDto) {
        registrarUsuario(usuarioDto.nombreUsuario(), usuarioDto.nombre(), usuarioDto.apellido(), usuarioDto.email(), usuarioDto.password(),
                ROL_USER, "Ya está registrado este usuario", "Error al crear el usuario");
    }

    public void createConductor(ConductorUsuarioDto userDto) {
        String keycloakId = registrarUsuario(userDto.nombreUsuario(), userDto.nombre(), userDto.apellidos(), userDto.email(), userDto.password(),
                ROL_DRIVER, "Ya está registrado este usuario", "Error al crear el usuario");
        publicarRegistroConductor(keycloakId, userDto);
    }

    public void deleteUser(String userId) {
        try {
            keycloakProvider.getUserResource()
                    .get(userId)
                    .remove();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }
    }

    public PerfilUsuarioDto obtenerPerfil(String userId) {
        UserResource userResource = keycloakProvider.getUserResource().get(userId);
        UserRepresentation user;
        try {
            user = userResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }
        List<String> roles = userResource.roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName)
                .toList();

        return new PerfilUsuarioDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled() != null && user.isEnabled(),
                roles
        );
    }

    public void actualizarPerfil(String userId, ActualizarPerfilDto dto) {
        UserResource userResource = keycloakProvider.getUserResource().get(userId);
        UserRepresentation user;
        try {
            user = userResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }
        user.setFirstName(dto.nombre());
        user.setLastName(dto.apellido());
        userResource.update(user);
        log.info("Perfil actualizado para el usuario {}", userId);
    }

    public void actualizarUsuarioPorAdmin(String userId, AdminActualizarUsuarioDto dto) {
        UserResource userResource = keycloakProvider.getUserResource().get(userId);
        UserRepresentation user;
        try {
            user = userResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }
        user.setFirstName(dto.nombre());
        user.setLastName(dto.apellido());
        user.setEmail(dto.email());
        user.setEnabled(dto.habilitado());
        userResource.update(user);
        log.info("Usuario {} actualizado por administrador", userId);
    }

    public void cambiarPassword(String userId, CambiarPasswordDto dto) {
        UserResource userResource = keycloakProvider.getUserResource().get(userId);
        UserRepresentation user;
        try {
            user = userResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }

        try {
            tokenClient.login(user.getUsername(), dto.passwordActual());
        } catch (AuthenticationFailedException e) {
            throw new AuthenticationFailedException("La contraseña actual es incorrecta");
        }

        CredentialRepresentation credencial = new CredentialRepresentation();
        credencial.setTemporary(false);
        credencial.setType(OAuth2Constants.PASSWORD);
        credencial.setValue(dto.passwordNuevo());
        userResource.resetPassword(credencial);
        log.info("Contraseña actualizada exitosamente para el usuario {}", userId);
    }

    public void eliminarCuentaPropia(String userId) {
        UserResource userResource = keycloakProvider.getUserResource().get(userId);
        UserRepresentation user;
        try {
            user = userResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }

        List<String> roles = userResource.roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName)
                .toList();

        if (roles.contains(ROL_ADMIN) || roles.contains(ROL_DRIVER)) {
            throw new OperationNotAllowedException("Los administradores y conductores no pueden auto-eliminarse del sistema");
        }

        userResource.remove();
        log.info("Cuenta eliminada exitosamente por el propio usuario {}", userId);
    }

    /**
     * Inicia el flujo de recuperación de contraseña de forma <b>asíncrona</b>.
     *
     * <p>El método retorna inmediatamente con un mensaje genérico, independientemente
     * de si el email está o no registrado. Esto elimina el <i>timing attack</i>
     * que permitía enumerar emails midiendo el tiempo de respuesta.</p>
     *
     * <p>La búsqueda en Keycloak y el envío del correo se delegan al
     * {@link RecuperacionPasswordWorker}, que se ejecuta en un pool acotado
     * y captura todas las excepciones internamente.</p>
     *
     * @param dto DTO con el email del solicitante
     * @return mensaje genérico anti-enumeración
     */
    public MensajeRespuestaDto solicitarRecuperacionPassword(RecuperarPasswordDto dto) {
        // Disparar el worker: no bloqueante, no se espera resultado
        recuperacionPasswordWorker.enviarCorreoRecuperacion(dto.email());
        // Respuesta inmediata — igual para emails registrados y no registrados
        return new MensajeRespuestaDto(
                "Si el correo está registrado, se enviaron las instrucciones para restablecer la contraseña."
        );
    }



    public TokenResponse login(LoginDto loginDto) {
        return tokenClient.login(loginDto.email(), loginDto.password());
    }

    /**
     * Flujo común de registro en Keycloak: crea el usuario (con credenciales embebidas, sin llamada
     * adicional de resetPassword), asigna el rol y devuelve el id de Keycloak generado.
     * Lanza {@link UserAlreadyExistsException} si el 201 no se completa o {@link UserCreationException}
     * ante cualquier otro estado distinto de 201/409 (mensajes idénticos a los callers originales).
     */
    private String registrarUsuario(String nombreUsuario, String nombre, String apellido, String email,
                                    String password, String rol, String mensajeDuplicado, String mensajeError) {
        UserRepresentation usuario = construirUserRepresentation(nombreUsuario, nombre, apellido, email, password);

        try (Response response = keycloakProvider.getUserResource().create(usuario)) {
            if (response.getStatus() == 201) {
                String keycloakId = extraerUserId(response);
                asignarRolRealm(keycloakId, rol);
                return keycloakId;
            }
            if (response.getStatus() == 409) {
                log.error(mensajeDuplicado);
                throw new UserAlreadyExistsException(mensajeDuplicado);
            }
            log.error(mensajeError);
            throw new UserCreationException(mensajeError);
        }
    }

    private UserRepresentation construirUserRepresentation(String nombreUsuario, String nombre, String apellido,
                                                           String email, String password) {
        CredentialRepresentation credencial = new CredentialRepresentation();
        credencial.setTemporary(false);
        credencial.setType(OAuth2Constants.PASSWORD);
        credencial.setValue(password);

        UserRepresentation usuario = new UserRepresentation();
        usuario.setFirstName(nombre);
        usuario.setLastName(apellido);
        usuario.setUsername(nombreUsuario);
        usuario.setEmail(email);
        usuario.setEmailVerified(true);
        usuario.setEnabled(true);
        usuario.setCredentials(List.of(credencial));
        return usuario;
    }

    private String extraerUserId(Response response) {
        String path = response.getLocation().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void asignarRolRealm(String keycloakId, String rol) {
        RoleRepresentation representacionRol = keycloakProvider.obtenerRol(rol);
        keycloakProvider.getRealmResource()
                .users()
                .get(keycloakId)
                .roles()
                .realmLevel()
                .add(List.of(representacionRol));
    }

    private void publicarRegistroConductor(String keycloakId, ConductorUsuarioDto userDto) {
        RegistroConductorIniciado evento = RegistroConductorIniciado.newBuilder()
                .setKeycloakId(keycloakId)
                .setNombre(userDto.nombre())
                .setApellido(userDto.apellidos())
                .setDni(userDto.dni())
                .setCategoriasLicencia(new ArrayList<>(userDto.categoriasLicencia()))
                .setAdminId(obtenerAdminId())
                .build();
        kafkaTemplate.send(TOPIC_INIT, keycloakId, evento);
    }

    private String obtenerAdminId() {
        String adminId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                adminId = auth.getName();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el adminId de la autenticación: {}", e.getMessage());
        }
        return adminId;
    }
}
