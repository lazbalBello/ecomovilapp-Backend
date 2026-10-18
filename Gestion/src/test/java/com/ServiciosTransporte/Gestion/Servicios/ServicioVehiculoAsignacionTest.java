package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoAsignacionMapper;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioVehiculoAsignacionTest {

    @Mock
    private IRepositorioVehiculo repositorioVehiculo;
    @Mock
    private IRepositorioConductor repositorioConductor;
    @Mock
    private VehiculoAsignacionMapper vehiculoAsignacionMapper;
    @Mock
    private IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    @Mock
    private VehiculoMapper vehiculoMapper;

    private ServicioVehiculoAsignacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioVehiculoAsignacion(
                repositorioVehiculo,
                repositorioConductor,
                vehiculoAsignacionMapper,
                repositorioVehiculoAsignacion,
                vehiculoMapper
        );
    }

    @Test
    void listaVehiculosActivosDelConductorPorUsuarioIdDelToken() {
        String usuarioId = "keycloak-user-id";
        Vehiculo vehiculo = new Vehiculo();
        VehiculoLiteDto vehiculoDto = new VehiculoLiteDto(null, null, null, 0,
                null, null, null, null, null, null);

        when(repositorioVehiculoAsignacion.findVehiculosActivosByConductorUsuarioId(usuarioId))
                .thenReturn(List.of(vehiculo));
        when(vehiculoMapper.toVehiculoLiteDto(vehiculo)).thenReturn(vehiculoDto);

        assertEquals(List.of(vehiculoDto), servicio.listarVehiculosActivosDelConductor(usuarioId));
        verify(repositorioVehiculoAsignacion).findVehiculosActivosByConductorUsuarioId(usuarioId);
    }
}
