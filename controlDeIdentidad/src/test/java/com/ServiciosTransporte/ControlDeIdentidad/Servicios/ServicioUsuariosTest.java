package com.ServiciosTransporte.ControlDeIdentidad.Servicios;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ConductorUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.LoginDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.UserDto;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserAlreadyExistsException;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.UserCreationException;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.KeycloakProvider;
import com.ServiciosTransporte.ControlDeIdentidad.Utils.TokenClient;
import com.servicioTransporte.flota.eventos.conductor.registro.RegistroConductorIniciado;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioUsuariosTest {

    private static final String KEYCLOAK_ID = "user-abc-123";

    @Mock
    private KeycloakProvider keycloakProvider;
    @Mock
    private TokenClient tokenClient;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private RealmResource realmResource;
    @Mock
    private org.keycloak.admin.client.resource.UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private RoleMappingResource rolesResourceUsuario;
    @Mock
    private RoleScopeResource roleScopeResource;

    @InjectMocks
    private ServicioUsuarios servicioUsuarios;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private Response respuesta(int status) {
        Response respuesta = mock(Response.class);
        when(respuesta.getStatus()).thenReturn(status);
        if (status == 201) {
            when(respuesta.getLocation()).thenReturn(URI.create(
                    "http://localhost:9090/admin/realms/flota/users/" + KEYCLOAK_ID));
        }
        return respuesta;
    }

    private void mockearRegistroExitoso(String rol) {
        RoleRepresentation rolMock = new RoleRepresentation();
        rolMock.setName(rol);

        Response respuesta = respuesta(201);
        when(keycloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(respuesta);
        when(keycloakProvider.obtenerRol(rol)).thenReturn(rolMock);
        when(keycloakProvider.getRealmResource()).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(KEYCLOAK_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(rolesResourceUsuario);
        when(rolesResourceUsuario.realmLevel()).thenReturn(roleScopeResource);
    }

    @Test
    @DisplayName("createAdmin crea el usuario con credenciales embebidas y asigna el rol admin")
    void createAdminCreaUsuarioYAsignaRol() {
        mockearRegistroExitoso("admin");

        servicioUsuarios.createAdmin(new UserDto("a.gomez", "Alejandro", "Gómez", "a.gomez@x.com", "pass123"));

        ArgumentCaptor<UserRepresentation> usuario = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(usersResource).create(usuario.capture());
        assertEquals("a.gomez", usuario.getValue().getUsername());
        assertEquals("Alejandro", usuario.getValue().getFirstName());
        assertEquals("Gómez", usuario.getValue().getLastName());
        assertEquals("a.gomez@x.com", usuario.getValue().getEmail());
        assertTrue(usuario.getValue().isEnabled());
        assertTrue(usuario.getValue().isEmailVerified());
        assertEquals(1, usuario.getValue().getCredentials().size());
        assertFalse(usuario.getValue().getCredentials().get(0).isTemporary());
        assertEquals(OAuth2Constants.PASSWORD, usuario.getValue().getCredentials().get(0).getType());
        assertEquals("pass123", usuario.getValue().getCredentials().get(0).getValue());

        verify(keycloakProvider).obtenerRol("admin");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleRepresentation>> roles = (ArgumentCaptor<List<RoleRepresentation>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(roleScopeResource).add(roles.capture());
        assertEquals("admin", roles.getValue().get(0).getName());
    }

    @Test
    @DisplayName("createUser asigna el rol user")
    void createUserAsignaRolUser() {
        mockearRegistroExitoso("user");

        servicioUsuarios.createUser(new UserDto("u.nuevo", "Única", "Nuñez", "u.nuevo@x.com", "pass123"));

        verify(keycloakProvider).obtenerRol("user");
        verify(roleScopeResource).add(any());
    }

    @Test
    @DisplayName("createConductor registra en Keycloak y publica el evento de saga con los datos del DTO")
    void createConductorPublicaEventoDeSaga() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-master", null, List.of()));
        mockearRegistroExitoso("driver");

        servicioUsuarios.createConductor(new ConductorUsuarioDto(
                "a.perez", "pass123", "a.perez@x.com", "12345678901", "Andrés", "Pérez",
                List.of("A"), true));

        verify(keycloakProvider).obtenerRol("driver");
        ArgumentCaptor<RegistroConductorIniciado> evento = ArgumentCaptor.forClass(RegistroConductorIniciado.class);
        verify(kafkaTemplate).send(eq("inicio-registro-conductor"), eq(KEYCLOAK_ID), evento.capture());

        assertEquals(KEYCLOAK_ID, evento.getValue().getKeycloakId().toString());
        assertEquals("Andrés", evento.getValue().getNombre().toString());
        assertEquals("Pérez", evento.getValue().getApellido().toString());
        assertEquals("12345678901", evento.getValue().getDni().toString());
        assertEquals(List.of("A"), evento.getValue().getCategoriasLicencia().stream().map(String::valueOf).toList());
        assertEquals("admin-master", evento.getValue().getAdminId().toString());
    }

    @Test
    @DisplayName("createConductor sin autenticación publica el evento con adminId null")
    void createConductorSinAuthPublicaNullAdminId() {
        mockearRegistroExitoso("driver");

        servicioUsuarios.createConductor(new ConductorUsuarioDto(
                "a.perez", "pass123", "a.perez@x.com", "12345678901", "Andrés", "Pérez",
                List.of("A"), null));

        ArgumentCaptor<RegistroConductorIniciado> evento = ArgumentCaptor.forClass(RegistroConductorIniciado.class);
        verify(kafkaTemplate).send(eq("inicio-registro-conductor"), eq(KEYCLOAK_ID), evento.capture());
        assertNull(evento.getValue().getAdminId());
    }

    @Test
    @DisplayName("duplicado (409) lanza UserAlreadyExistsException con el mensaje original y no publica Kafka")
    void registroDuplicadoLanzaExcepcion() {
        Response respuesta = respuesta(409);
        when(keycloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(respuesta);

        UserAlreadyExistsException excepcion = assertThrows(UserAlreadyExistsException.class,
                () -> servicioUsuarios.createAdmin(new UserDto("a.gomez", "A", "G", "a@x.com", "pass123")));

        assertEquals("Ya está registrado este administrador", excepcion.getMessage());
        verify(kafkaTemplate, never()).send(anyString(), any(), any());
    }

    @Test
    @DisplayName("status inesperado (500) lanza UserCreationException con el mensaje original")
    void errorDesconocidoLanzaExcepcionDeCreacion() {
        Response respuesta = respuesta(500);
        when(keycloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(respuesta);

        UserCreationException excepcion = assertThrows(UserCreationException.class,
                () -> servicioUsuarios.createUser(new UserDto("u.nuevo", "U", "N", "u@x.com", "pass123")));

        assertEquals("Error al crear el usuario", excepcion.getMessage());
    }

    @Test
    @DisplayName("login delega en TokenClient y devuelve el TokenResponse sin envolverlo")
    void loginDelegaEnTokenClient() {
        TokenResponse esperado = new TokenResponse("jwt-abc", "refresh-abc", 300L, 1800L);
        when(tokenClient.login("a@x.com", "pass123")).thenReturn(esperado);

        TokenResponse resultado = servicioUsuarios.login(new LoginDto("a@x.com", "pass123"));

        assertSame(esperado, resultado);
    }

    @Test
    @DisplayName("login re-propaga la AuthenticationFailedException de TokenClient sin re-envolverla")
    void loginRepropagaExcepcionDeAutenticacion() {
        AuthenticationFailedException fallo = new AuthenticationFailedException(
                "Credenciales inválidas: usuario o contraseña incorrectos");
        when(tokenClient.login("a@x.com", "bad")).thenThrow(fallo);

        AuthenticationFailedException excepcion = assertThrows(AuthenticationFailedException.class,
                () -> servicioUsuarios.login(new LoginDto("a@x.com", "bad")));

        assertSame(fallo, excepcion);
    }

    @Test
    @DisplayName("ConductorUsuarioDto: si disponibilidad viene ausente el default es false; si viene explícita se conserva")
    void defaultsDeDisponibilidadEnRecord() {
        ConductorUsuarioDto ausente = new ConductorUsuarioDto(
                "u", "p", "e@x.com", "12345678901", "N", "A", List.of("A"), null);
        assertFalse(ausente.disponibilidad());

        ConductorUsuarioDto explicitaTrue = new ConductorUsuarioDto(
                "u", "p", "e@x.com", "12345678901", "N", "A", List.of("A"), true);
        assertTrue(explicitaTrue.disponibilidad());

        ConductorUsuarioDto explicitaFalse = new ConductorUsuarioDto(
                "u", "p", "e@x.com", "12345678901", "N", "A", List.of("A"), false);
        assertFalse(explicitaFalse.disponibilidad());
    }
}
