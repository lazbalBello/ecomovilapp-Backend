package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoAsignacionMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.AsignacionLiteDtoMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.VehiculoLiteDtoMapper;
import com.ServiciosTransporte.Gestion.MappersUpdate.AsignacionUpdateDtoMapper;
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
    private AsignacionLiteDtoMapper asignacionLiteDtoMapper;
    @Mock
    private AsignacionUpdateDtoMapper asignacionUpdateDtoMapper;
    @Mock
    private VehiculoLiteDtoMapper vehiculoLiteDtoMapper;

    private ServicioVehiculoAsignacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioVehiculoAsignacion(
                repositorioVehiculo,
                repositorioConductor,
                vehiculoAsignacionMapper,
                repositorioVehiculoAsignacion,
                asignacionLiteDtoMapper,
                asignacionUpdateDtoMapper,
                vehiculoLiteDtoMapper
        );
    }

    @Test
    void listaVehiculosActivosDelConductorPorUsuarioIdDelToken() {
        String usuarioId = "keycloak-user-id";
        Vehiculo vehiculo = new Vehiculo();
        VehiculoLiteDto vehiculoDto = new VehiculoLiteDto();

        when(repositorioVehiculoAsignacion.findVehiculosActivosByConductorUsuarioId(usuarioId))
                .thenReturn(List.of(vehiculo));
        when(vehiculoLiteDtoMapper.toVehiculoLiteDto(vehiculo)).thenReturn(vehiculoDto);

        assertEquals(List.of(vehiculoDto), servicio.listarVehiculosActivosDelConductor(usuarioId));
        verify(repositorioVehiculoAsignacion).findVehiculosActivosByConductorUsuarioId(usuarioId);
    }
}
