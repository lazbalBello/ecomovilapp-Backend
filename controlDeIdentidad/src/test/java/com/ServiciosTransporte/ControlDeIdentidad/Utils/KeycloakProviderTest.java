package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakProviderTest {

    private final KeycloakProvider provider = new KeycloakProvider();
    private final Keycloak keycloak = mock(Keycloak.class);
    private final RealmResource realm = mock(RealmResource.class);
    private final RolesResource rolesDelRealm = mock(RolesResource.class);
    private final RoleResource roleResource = mock(RoleResource.class);

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(provider, "adminClient", keycloak);
        ReflectionTestUtils.setField(provider, "realmName", "test-realm");
        when(keycloak.realm("test-realm")).thenReturn(realm);
        when(realm.roles()).thenReturn(rolesDelRealm);
    }

    @Test
    @DisplayName("obtenerRol lee el rol de Keycloak una sola vez y cachea el resto")
    void obtenerRolCacheaLaSegundaLectura() {
        when(rolesDelRealm.get("driver")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());

        RoleRepresentation primera = provider.obtenerRol("driver");
        RoleRepresentation segunda = provider.obtenerRol("driver");

        verify(rolesDelRealm, times(1)).get("driver");
        assertSame(primera, segunda);
    }

    @Test
    @DisplayName("roles distintos se cachean de forma independiente")
    void rolesDistintosSeCacheanIndependientemente() {
        RoleResource roleResourceOtro = mock(RoleResource.class);
        when(rolesDelRealm.get("driver")).thenReturn(roleResource);
        when(rolesDelRealm.get("admin")).thenReturn(roleResourceOtro);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
        when(roleResourceOtro.toRepresentation()).thenReturn(new RoleRepresentation());

        provider.obtenerRol("driver");
        provider.obtenerRol("driver");
        provider.obtenerRol("admin");

        verify(rolesDelRealm, times(1)).get("driver");
        verify(rolesDelRealm, times(1)).get("admin");
    }
}
