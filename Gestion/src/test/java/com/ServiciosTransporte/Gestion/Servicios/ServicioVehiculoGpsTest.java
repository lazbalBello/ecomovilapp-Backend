package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
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
class ServicioVehiculoGpsTest {

    @Mock
    private IRepositorioVehiculo repositorioVehiculo;
    @Mock
    private VehiculoMapper vehiculoMapper;
    @Mock
    private IRepositorioRuta repositorioRuta;
    @Mock
    private IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ServicioVehiculo servicioVehiculo;

    @BeforeEach
    void setUp() {
        servicioVehiculo = new ServicioVehiculo(
                repositorioVehiculo,
                vehiculoMapper,
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
        when(vehiculoMapper.toVehiculoLiteDto(any())).thenReturn(
                new VehiculoLiteDto(null, null, null, 0, null, null, null, null, null, null));

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

        VehiculoLiteDto dto = new VehiculoLiteDto(3L, "B999999", "1558885",
                0, null, null, null, null, null, null);

        when(repositorioVehiculo.findByGpsId("1558885")).thenReturn(Optional.of(vehiculo));
        when(vehiculoMapper.toVehiculoLiteDto(vehiculo)).thenReturn(dto);

        VehiculoLiteDto resultado = servicioVehiculo.buscarPorGpsId("1558885");

        assertNotNull(resultado);
        assertEquals("B999999", resultado.matricula());
        assertEquals("1558885", resultado.gpsId());
    }
}
