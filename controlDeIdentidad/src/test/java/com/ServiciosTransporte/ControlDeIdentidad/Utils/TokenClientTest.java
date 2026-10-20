package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenClientTest {

    private static HttpServer servidor;
    private static TokenClient tokenClient;
    private static int statusToken = 200;

    @BeforeAll
    static void levantarServidorFalso() throws IOException {
        AccessTokenResponse tokenFalso = new AccessTokenResponse();
        tokenFalso.setToken("access-abc");
        tokenFalso.setRefreshToken("refresh-xyz");
        tokenFalso.setExpiresIn(300L);
        tokenFalso.setRefreshExpiresIn(1800L);
        byte[] cuerpoOk = new ObjectMapper().writeValueAsBytes(tokenFalso);
        byte[] cuerpo401 = "{\"error\":\"invalid_grant\"}".getBytes();

        servidor = HttpServer.create(new InetSocketAddress(0), 0);
        servidor.createContext("/realms/test/protocol/openid-connect/token", exchange -> {
            byte[] cuerpo = statusToken == 200 ? cuerpoOk : cuerpo401;
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusToken, cuerpo.length);
            exchange.getResponseBody().write(cuerpo);
            exchange.close();
        });
        servidor.start();
        tokenClient = new TokenClient(
                "http://localhost:" + servidor.getAddress().getPort(), "test", WebClient.builder());
    }

    @AfterAll
    static void pararServidor() {
        servidor.stop(0);
    }

    @Test
    @DisplayName("login por grant_type=password retorna el TokenResponse completo")
    void loginExitoso() {
        statusToken = 200;

        TokenResponse respuesta = tokenClient.login("a.perez@x.com", "pass123");

        assertEquals("access-abc", respuesta.jwtToken());
        assertEquals("refresh-xyz", respuesta.refreshToken());
        assertEquals(300L, respuesta.expireIn());
        assertEquals(1800L, respuesta.refreshExpiresIn());
    }

    @Test
    @DisplayName("login con 401 lanza el mensaje original de credenciales inválidas")
    void loginInvalido() {
        statusToken = 401;

        AuthenticationFailedException excepcion = assertThrows(AuthenticationFailedException.class,
                () -> tokenClient.login("a.perez@x.com", "pass-incorrecta"));

        assertEquals("Credenciales inválidas: usuario o contraseña incorrectos", excepcion.getMessage());
    }

    @Test
    @DisplayName("login contra un servidor caído informa fallo de conexión")
    void loginConServidorCaido() throws IOException {
        int puertoCerrado;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoCerrado = socket.getLocalPort();
        }
        TokenClient clienteSinServidor = new TokenClient(
                "http://localhost:" + puertoCerrado, "test", WebClient.builder());

        AuthenticationFailedException excepcion = assertThrows(AuthenticationFailedException.class,
                () -> clienteSinServidor.login("a.perez@x.com", "pass123"));

        assertEquals("Imposible conectar con el servidor de identidad", excepcion.getMessage());
    }

    @Test
    @DisplayName("refresh y guest-token siguen funcionando tras el refactor")
    void refreshYGuestTokenExitosos() {
        statusToken = 200;

        TokenResponse refresh = tokenClient.refresh("token-viejo");
        assertEquals("access-abc", refresh.jwtToken());
        assertEquals("refresh-xyz", refresh.refreshToken());

        TokenResponse guest = tokenClient.getGuestToken();
        assertEquals(300L, guest.expireIn());
    }
}
