package com.ServiciosTransporte.Gestion;

import com.ServiciosTransporte.Gestion.Dto.VehiculoDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.VehiculoUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Servicios.SrevicioVehiculo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SrevicioVehiculoIntegrationTest {

    @Autowired
    private SrevicioVehiculo servicioVehiculo;

    @Autowired
    private IRepositorioVehiculo repositorioVehiculo;

    @AfterEach
    void limpiarBaseDeDatos() {
        repositorioVehiculo.deleteAll();
    }

    @Test
    void crearVehiculoSinImeiDebePermitirlo() {
        VehiculoDto dto = crearVehiculoDto("B123456", null);

        VehiculoDto guardado = servicioVehiculo.registrarVehiculo(dto);

        assertNotNull(guardado.getId());
        assertNull(guardado.getImeiDispositivoGps());
    }

    @Test
    void crearVehiculoConImeiDebePersistirlo() {
        String imei = "123456789012345";
        VehiculoDto dto = crearVehiculoDto("B123457", imei);

        VehiculoDto guardado = servicioVehiculo.registrarVehiculo(dto);

        assertEquals(imei, guardado.getImeiDispositivoGps());
        assertEquals(imei, repositorioVehiculo.findById(guardado.getId()).orElseThrow().getImeiDispositivoGps());
    }

    @Test
    void recuperarVehiculoConImeiDebeDevolverlo() {
        String imei = "123456789012346";
        VehiculoDto insertado = servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123458", imei));

        Vehiculo vehiculo = repositorioVehiculo.findByImeiDispositivoGps(imei).orElseThrow();

        assertEquals(insertado.getId(), vehiculo.getId());
        assertEquals(imei, vehiculo.getImeiDispositivoGps());
    }

    @Test
    void actualizarVehiculoConImeiDebeModificarlo() {
        VehiculoDto insertado = servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123459", "123456789012347"));
        VehiculoUpdateDto updateDto = new VehiculoUpdateDto();
        updateDto.setImeiDispositivoGps("987654321012345");

        servicioVehiculo.actualizarVehiculo(insertado.getId(), updateDto);

        Vehiculo actualizado = repositorioVehiculo.findById(insertado.getId()).orElseThrow();
        assertEquals("987654321012345", actualizado.getImeiDispositivoGps());
    }

    @Test
    void rechazarImeiDuplicadoConConflict() {
        String imei = "123456789012348";
        servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123460", imei));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123461", imei)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("IMEI"));
    }

    @Test
    void softDeleteDeVehiculoDebeRechazarReutilizacionDelImei() {
        String imei = "123456789012350";
        VehiculoDto vehiculoA = servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123463", imei));

        servicioVehiculo.softDeleteVehiculo(vehiculoA.getId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123464", imei)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("IMEI"));
    }

    @Test
    void softDeleteDebePreservarVehiculoYHistorial() {
        String imei = "123456789012349";
        VehiculoDto insertado = servicioVehiculo.registrarVehiculo(crearVehiculoDto("B123462", imei));

        servicioVehiculo.softDeleteVehiculo(insertado.getId());

        Vehiculo eliminado = repositorioVehiculo.findById(insertado.getId()).orElseThrow();
        assertNotNull(eliminado.getFechaEliminacion());
        assertEquals(imei, eliminado.getImeiDispositivoGps());
    }

    private VehiculoDto crearVehiculoDto(String matricula, String imei) {
        VehiculoDto dto = new VehiculoDto();
        dto.setMatricula(matricula);
        dto.setCapacidadPersonas(4);
        dto.setModelo("E2");
        dto.setMarca("BYD");
        dto.setTipoBateria("Ion-Litio (Li-ion)");
        dto.setImeiDispositivoGps(imei);
        return dto;
    }
}
