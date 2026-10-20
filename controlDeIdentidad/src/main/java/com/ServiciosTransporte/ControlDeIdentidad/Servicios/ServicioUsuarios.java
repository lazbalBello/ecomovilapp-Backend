package com.ServiciosTransporte.ControlDeIdentidad.Servicios;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ConductorUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.LoginDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.UserDto;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserAlreadyExistsException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserCreationException;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.KeycloakProvider;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.TokenClient;
import com.servicioTransporte.flota.eventos.conductor.registro.RegistroConductorIniciado;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
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

    public void createAdmin(@NonNull UserDto userDto) {
        registrarUsuario(userDto.nombreUsuario(), userDto.nombre(), userDto.apellido(), userDto.email(), userDto.password(),
                ROL_ADMIN, "Ya está registrado este administrador", "Error al registrar el administrador");
    }

    public void createUser(@NonNull UserDto userDto) {
        registrarUsuario(userDto.nombreUsuario(), userDto.nombre(), userDto.apellido(), userDto.email(), userDto.password(),
                ROL_USER, "Ya está registrado este usuario", "Error al crear el usuario");
    }

    public void createConductor(ConductorUsuarioDto userDto) {
        String keycloakId = registrarUsuario(userDto.nombreUsuario(), userDto.nombre(), userDto.apellidos(), userDto.email(), userDto.password(),
                ROL_DRIVER, "Ya está registrado este usuario", "Error al crear el usuario");
        publicarRegistroConductor(keycloakId, userDto);
    }

    public void deleteUser(String userId) {
        keycloakProvider.getUserResource()
                .get(userId)
                .remove();
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
