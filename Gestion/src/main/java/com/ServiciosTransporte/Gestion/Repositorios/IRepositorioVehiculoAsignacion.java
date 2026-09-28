package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRepositorioVehiculoAsignacion extends JpaRepository<VehiculoAsignacion, Long> {

    Optional<VehiculoAsignacion> findByVehiculo_IdAndConductor_Id(Long vehiculoId, Long conductorId);

    @Modifying
    @Query("UPDATE VehiculoAsignacion a SET a.fechaEliminacion = :now WHERE a.vehiculo.id = :vehiculoId AND a.fechaEliminacion IS NULL")
    int softDeleteFromVehiculo(@Param("vehiculoId") Long vehiculoId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VehiculoAsignacion a SET a.fechaEliminacion = :now WHERE a.conductor.id = :conductorId AND a.fechaEliminacion IS NULL")
    int softDeleteFromConductor(@Param("conductorId") Long conductorId, @Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM vehiculo_asignacion WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<VehiculoAsignacion> findAllEliminadas();

    long countByFechaCreacionAfter(LocalDateTime fecha);

    List<VehiculoAsignacion> findByVehiculo_Id(Long vehiculoId);

    List<VehiculoAsignacion> findByConductor_Id(Long conductorId);

        @Query("""
                SELECT DISTINCT a.vehiculo FROM VehiculoAsignacion a
                WHERE a.conductor.usuarioId = :usuarioId
                    AND a.fechaEliminacion IS NULL
                    AND a.fechaInicio <= CURRENT_DATE
                    AND (a.indefinido = true OR a.fechaFinal IS NULL OR a.fechaFinal >= CURRENT_DATE)
                    AND a.vehiculo.fechaEliminacion IS NULL
        """)
        List<Vehiculo> findVehiculosActivosByConductorUsuarioId(@Param("usuarioId") String usuarioId);

    @Query("""
        SELECT COUNT(a) FROM VehiculoAsignacion a
        WHERE (a.indefinido = true
               OR (a.indefinido = false AND (a.fechaFinal IS NULL OR a.fechaFinal >= CURRENT_DATE)))
          AND a.fechaEliminacion IS NULL
    """)
    long countAsignacionesActivas();

    @Query("""
        SELECT COUNT(a) FROM VehiculoAsignacion a
        WHERE a.indefinido = false
          AND a.fechaFinal IS NOT NULL
          AND a.fechaFinal < CURRENT_DATE
          AND a.fechaEliminacion IS NULL
    """)
    long countAsignacionesInactivas();

    @Query("""
        SELECT COUNT(DISTINCT a.vehiculo.id) FROM VehiculoAsignacion a
        WHERE (a.indefinido = true
               OR (a.indefinido = false AND (a.fechaFinal IS NULL OR a.fechaFinal >= CURRENT_DATE)))
          AND a.fechaEliminacion IS NULL
          AND a.vehiculo.fechaEliminacion IS NULL
    """)
    long countVehiculosConAsignacionActiva();

    @Query("""
        SELECT COUNT(DISTINCT a.vehiculo.ruta.id) FROM VehiculoAsignacion a
        WHERE (a.indefinido = true
               OR (a.indefinido = false AND (a.fechaFinal IS NULL OR a.fechaFinal >= CURRENT_DATE)))
          AND a.fechaEliminacion IS NULL
          AND a.vehiculo.fechaEliminacion IS NULL
          AND a.vehiculo.ruta IS NOT NULL
          AND a.vehiculo.ruta.fechaEliminacion IS NULL
    """)
    long countRutasConVehiculosEnAsignacionActiva();
}
