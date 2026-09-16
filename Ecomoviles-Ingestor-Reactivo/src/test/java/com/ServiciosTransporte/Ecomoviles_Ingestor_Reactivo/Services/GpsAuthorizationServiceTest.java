package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GpsAuthorizationServiceTest {

    private GpsAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new GpsAuthorizationService();
        authorizationService.clear();
    }

    @Test
    @DisplayName("Debe autorizar y validar un GPS correctamente")
    void testAuthorizeAndIsAuthorized() {
        assertFalse(authorizationService.isAuthorized("1558885"));

        authorizationService.authorize("1558885");
        assertTrue(authorizationService.isAuthorized("1558885"));
        assertEquals(1, authorizationService.getAuthorizedCount());
    }

    @Test
    @DisplayName("Debe manejar espacios en blanco y normalizar IDs")
    void testTrimmingAndBlanks() {
        authorizationService.authorize("  1558885  ");
        assertTrue(authorizationService.isAuthorized("1558885"));
        assertTrue(authorizationService.isAuthorized(" 1558885 "));

        assertFalse(authorizationService.isAuthorized(null));
        assertFalse(authorizationService.isAuthorized(""));
        assertFalse(authorizationService.isAuthorized("   "));
    }

    @Test
    @DisplayName("Debe revocar un GPS previamente autorizado")
    void testRevoke() {
        authorizationService.authorize("1558885");
        assertTrue(authorizationService.isAuthorized("1558885"));

        authorizationService.revoke("1558885");
        assertFalse(authorizationService.isAuthorized("1558885"));
        assertEquals(0, authorizationService.getAuthorizedCount());
    }

    @Test
    @DisplayName("Debe rechazar IDs no registrados (fail-fast)")
    void testUnauthorizedGpsRejected() {
        authorizationService.authorize("1558885");

        assertFalse(authorizationService.isAuthorized("9999999"));
        assertFalse(authorizationService.isAuthorized("HACKER_GPS"));
    }
}
