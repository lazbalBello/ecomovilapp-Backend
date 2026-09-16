package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.VehiculoLiteDtoMapper;
import com.ServiciosTransporte.Gestion.MappersUpdate.VehiculoUpdateMapper;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import com.servicioTransporte.flota.eventos.vehiculo.configuracion.DispositivoGpsAutorizado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SrevicioVehiculoGpsTest {

    @Mock
    private IRepositorioVehiculo repositorioVehiculo;
    @Mock
    private VehiculoMapper vehiculoMapper;
    @Mock
    private VehiculoLiteDtoMapper vehiculoLiteDtoMapper;
    @Mock
    private VehiculoUpdateMapper vehiculoUpdateMapper;
    @Mock
    private IRepositorioRuta repositorioRuta;
    @Mock
    private IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private SrevicioVehiculo servicioVehiculo;

    @BeforeEach
    void setUp() {
        servicioVehiculo = new SrevicioVehiculo(
                repositorioVehiculo,
                vehiculoMapper,
                vehiculoLiteDtoMapper,
                vehiculoUpdateMapper,
                repositorioRuta,
                repositorioVehiculoAsignacion,
                kafkaTemplate
        );
    }

    @Test
    @DisplayName("softDeleteVehiculo debe poner gpsId en null y publicar revocación en Kafka")
    void testSoftDeleteVehiculoSetsGpsNullAndRevokes() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setMatricula("B123456");
        vehiculo.setGpsId("1558885");

        when(repositorioVehiculo.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(repositorioVehiculo.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(new CompletableFuture<>());

        servicioVehiculo.softDeleteVehiculo(1L);

        // 1. Debe haber puesto el gpsId en null
        assertNull(vehiculo.getGpsId(), "El campo gpsId debe quedar en null para poder reasignarse a otro vehículo");
        assertNotNull(vehiculo.getFechaEliminacion());

        // 2. Debe haber publicado el evento de revocación en Kafka
        ArgumentCaptor<DispositivoGpsAutorizado> captor = ArgumentCaptor.forClass(DispositivoGpsAutorizado.class);
        verify(kafkaTemplate).send(eq("flota-dispositivos-autorizados"), eq("1558885"), captor.capture());

        DispositivoGpsAutorizado evento = captor.getValue();
        assertEquals("1558885", evento.getGpsId().toString());
        assertFalse(evento.getActivo(), "El evento debe indicar activo=false");
    }

    @Test
    @DisplayName("desasociarGps debe poner gpsId en null sin eliminar el vehículo y revocar en Kafka")
    void testDesasociarGps() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(2L);
        vehiculo.setMatricula("B654321");
        vehiculo.setGpsId("9876543");

        when(repositorioVehiculo.findById(2L)).thenReturn(Optional.of(vehiculo));
        when(repositorioVehiculo.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(new CompletableFuture<>());
        when(vehiculoLiteDtoMapper.toVehiculoLiteDto(any())).thenReturn(new VehiculoLiteDto());

        VehiculoLiteDto resultado = servicioVehiculo.desasociarGps(2L);
        assertNotNull(resultado);

        // El vehículo no debe estar eliminado pero sí desasociado del GPS
        assertNull(vehiculo.getGpsId());
        assertNull(vehiculo.getFechaEliminacion());

        // Debe haber publicado la revocación en Kafka
        ArgumentCaptor<DispositivoGpsAutorizado> captor = ArgumentCaptor.forClass(DispositivoGpsAutorizado.class);
        verify(kafkaTemplate).send(eq("flota-dispositivos-autorizados"), eq("9876543"), captor.capture());
        assertFalse(captor.getValue().getActivo());
    }

    @Test
    @DisplayName("buscarPorGpsId debe retornar el vehículo correspondiente")
    void testBuscarPorGpsId() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(3L);
        vehiculo.setMatricula("B999999");
        vehiculo.setGpsId("1558885");

        VehiculoLiteDto dto = new VehiculoLiteDto();
        dto.setId(3L);
        dto.setMatricula("B999999");
        dto.setGpsId("1558885");

        when(repositorioVehiculo.findByGpsId("1558885")).thenReturn(Optional.of(vehiculo));
        when(vehiculoLiteDtoMapper.toVehiculoLiteDto(vehiculo)).thenReturn(dto);

        VehiculoLiteDto resultado = servicioVehiculo.buscarPorGpsId("1558885");

        assertNotNull(resultado);
        assertEquals("B999999", resultado.getMatricula());
        assertEquals("1558885", resultado.getGpsId());
    }
}
