package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasAsignacionesDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasConductoresDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasResumenDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasRutasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasVehiculosDto;
import com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final IRepositorioRuta repositorioRuta;
    private final IRepositorioConductor repositorioConductor;
    private final IRepositorioVehiculoAsignacion repositorioAsignacion;

    public EstadisticasResumenDto obtenerResumen() {
        LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);

        Map<String, Long> vehiculosPorEstado = new HashMap<>();
        for (EstadoVehiculo estado : EstadoVehiculo.values()) {
            vehiculosPorEstado.put(estado.name(), repositorioVehiculo.countByEstado(estado));
        }

        Map<String, Long> vehiculosPorMarca = new HashMap<>();
        List<String> marcas = repositorioVehiculo.findAll().stream()
                .map(v -> v.getMarca())
                .distinct()
                .toList();
        for (String marca : marcas) {
            vehiculosPorMarca.put(marca, repositorioVehiculo.countByMarca(marca));
        }

        Map<String, Long> vehiculosPorTipoBateria = new HashMap<>();
        List<String> tiposBateria = repositorioVehiculo.findAll().stream()
                .map(v -> v.getTipoBateria())
                .distinct()
                .toList();
        for (String tipo : tiposBateria) {
            vehiculosPorTipoBateria.put(tipo, repositorioVehiculo.countByTipoBateria(tipo));
        }

        return new EstadisticasResumenDto(
                repositorioVehiculo.count(),
                repositorioVehiculo.countVehiculosActivos(),
                repositorioVehiculo.countByEstado(EstadoVehiculo.INACTIVO),
                repositorioVehiculo.countByEstado(EstadoVehiculo.MANTENIMIENTO),
                repositorioVehiculo.countByEstado(EstadoVehiculo.CARGANDO),
                repositorioVehiculo.countByEstado(EstadoVehiculo.FUERA_DE_SERVICIO),
                repositorioVehiculo.countByFechaCreacionAfter(hace7Dias),
                vehiculosPorEstado,
                vehiculosPorMarca,
                vehiculosPorTipoBateria,

                repositorioRuta.count(),
                repositorioRuta.countRutasConVehiculos(),
                repositorioRuta.countRutasSinVehiculos(),
                repositorioRuta.countRutasConParadas(),
                repositorioRuta.countRutasSinParadas(),
                repositorioRuta.countByFechaCreacionAfter(hace7Dias),
                repositorioRuta.promedioParadasPorRuta(),

                repositorioConductor.count(),
                repositorioConductor.countByDisponibilidadTrue(),
                repositorioConductor.countByDisponibilidadFalse(),
                repositorioConductor.countByFechaCreacionAfter(hace7Dias),

                repositorioAsignacion.countAsignacionesActivas(),
                repositorioAsignacion.countAsignacionesInactivas(),
                repositorioAsignacion.countVehiculosConAsignacionActiva(),
                repositorioAsignacion.countRutasConVehiculosEnAsignacionActiva()
        );
    }

    public EstadisticasVehiculosDto obtenerEstadisticasVehiculos() {
        LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);

        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoVehiculo estado : EstadoVehiculo.values()) {
            porEstado.put(estado.name(), repositorioVehiculo.countByEstado(estado));
        }

        Map<String, Long> porMarca = new HashMap<>();
        List<String> marcas = repositorioVehiculo.findAll().stream()
                .map(v -> v.getMarca())
                .distinct()
                .toList();
        for (String marca : marcas) {
            porMarca.put(marca, repositorioVehiculo.countByMarca(marca));
        }

        Map<String, Long> porTipoBateria = new HashMap<>();
        List<String> tipos = repositorioVehiculo.findAll().stream()
                .map(v -> v.getTipoBateria())
                .distinct()
                .toList();
        for (String tipo : tipos) {
            porTipoBateria.put(tipo, repositorioVehiculo.countByTipoBateria(tipo));
        }

        List<EstadisticasVehiculosDto.TopMarcaDto> topMarcas = porMarca.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new EstadisticasVehiculosDto.TopMarcaDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<EstadisticasVehiculosDto.TopModeloDto> topModelos = repositorioVehiculo.findAll().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getMarca() + " " + v.getModelo(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new EstadisticasVehiculosDto.TopModeloDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new EstadisticasVehiculosDto(
                repositorioVehiculo.count(),
                repositorioVehiculo.countVehiculosActivos(),
                repositorioVehiculo.countByEstado(EstadoVehiculo.INACTIVO),
                repositorioVehiculo.countByEstado(EstadoVehiculo.MANTENIMIENTO),
                repositorioVehiculo.countByEstado(EstadoVehiculo.CARGANDO),
                repositorioVehiculo.countByEstado(EstadoVehiculo.FUERA_DE_SERVICIO),
                repositorioVehiculo.countByFechaCreacionAfter(hace7Dias),
                porEstado,
                porMarca,
                porTipoBateria,
                repositorioVehiculo.countByRutaIsNotNull(),
                repositorioVehiculo.countByGpsIdIsNotNull(),
                topMarcas,
                topModelos
        );
    }

    public EstadisticasRutasDto obtenerEstadisticasRutas() {
        LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);

        List<com.ServiciosTransporte.Gestion.Modelos.Ruta> rutas = repositorioRuta.findAll();

        Map<String, Long> vehiculosPorRuta = new HashMap<>();
        Map<String, Long> paradasPorRuta = new HashMap<>();

        for (com.ServiciosTransporte.Gestion.Modelos.Ruta ruta : rutas) {
            vehiculosPorRuta.put(ruta.getNombre(),
                    (long) ruta.getVehiculosAsignados().size());
            paradasPorRuta.put(ruta.getNombre(),
                    (long) ruta.getParadas().size());
        }

        return new EstadisticasRutasDto(
                repositorioRuta.count(),
                repositorioRuta.countRutasConVehiculos(),
                repositorioRuta.countRutasSinVehiculos(),
                repositorioRuta.countRutasConParadas(),
                repositorioRuta.countRutasSinParadas(),
                repositorioRuta.countByFechaCreacionAfter(hace7Dias),
                repositorioRuta.promedioParadasPorRuta(),
                vehiculosPorRuta,
                paradasPorRuta
        );
    }

    public EstadisticasConductoresDto obtenerEstadisticasConductores() {
        LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);

        Map<String, Long> porCategoria = new HashMap<>();
        List<String> categorias = repositorioConductor.findAll().stream()
                .flatMap(c -> c.getCategoriasLicencia().stream())
                .distinct()
                .toList();
        for (String cat : categorias) {
            final String categoria = cat;
            long count = repositorioConductor.findAll().stream()
                    .filter(c -> c.getCategoriasLicencia().contains(categoria))
                    .count();
            porCategoria.put(categoria, count);
        }

        return new EstadisticasConductoresDto(
                repositorioConductor.count(),
                repositorioConductor.countByDisponibilidadTrue(),
                repositorioConductor.countByDisponibilidadFalse(),
                repositorioConductor.countByFechaCreacionAfter(hace7Dias),
                porCategoria
        );
    }

    public EstadisticasAsignacionesDto obtenerEstadisticasAsignaciones() {
        List<VehiculoAsignacion> asignacionesActivas = repositorioAsignacion.findAll().stream()
                .filter(a -> a.isIndefinido() || (a.getFechaFinal() == null || !a.getFechaFinal().isBefore(java.time.LocalDate.now())))
                .toList();

        Map<String, Long> asignacionesPorConductor = new HashMap<>();
        Map<String, Long> asignacionesPorVehiculo = new HashMap<>();

        for (VehiculoAsignacion a : asignacionesActivas) {
            String conductorKey = a.getConductor().getNombre() + " " + a.getConductor().getApellidos();
            asignacionesPorConductor.merge(conductorKey, 1L, Long::sum);

            String vehiculoKey = a.getVehiculo().getMatricula();
            asignacionesPorVehiculo.merge(vehiculoKey, 1L, Long::sum);
        }

        List<EstadisticasAsignacionesDto.ConductorAsignacionesDto> topConductores = asignacionesPorConductor.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    // Buscar el ID del conductor para el DTO
                    Long conductorId = asignacionesActivas.stream()
                            .filter(a -> (a.getConductor().getNombre() + " " + a.getConductor().getApellidos()).equals(e.getKey()))
                            .map(a -> a.getConductor().getId())
                            .findFirst()
                            .orElse(null);
                    return new EstadisticasAsignacionesDto.ConductorAsignacionesDto(conductorId, e.getKey(), e.getValue());
                })
                .collect(Collectors.toList());

        return new EstadisticasAsignacionesDto(
                repositorioAsignacion.countAsignacionesActivas(),
                repositorioAsignacion.countAsignacionesInactivas(),
                repositorioAsignacion.countVehiculosConAsignacionActiva(),
                repositorioAsignacion.countRutasConVehiculosEnAsignacionActiva(),
                asignacionesPorConductor,
                asignacionesPorVehiculo,
                topConductores
        );
    }
}