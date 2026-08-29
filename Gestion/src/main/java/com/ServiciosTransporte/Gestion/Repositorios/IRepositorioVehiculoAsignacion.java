package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IRepositorioVehiculoAsignacion extends JpaRepository<VehiculoAsignacion, Long> {


    Optional<VehiculoAsignacion> findByVehiculo_IdAndConductor_Id(Long vehiculoId, Long conductorId);

    @Query("SELECT a FROM VehiculoAsignacion a WHERE a.conductor.id = :conductorId AND a.fechaEliminacion IS NULL AND (a.indefinido = true OR a.fechaFinal IS NULL OR a.fechaFinal >= CURRENT_DATE) ORDER BY a.fechaInicio DESC")
    java.util.List<VehiculoAsignacion> findActiveByConductorId(@Param("conductorId") Long conductorId);

    @Modifying
    @Query("UPDATE VehiculoAsignacion a SET a.fechaEliminacion = :now WHERE a.vehiculo.id = :vehiculoId AND a.fechaEliminacion IS NULL")
    int softDeleteFromVehiculo(@Param("vehiculoId")Long vehiculoId, @Param("now")LocalDateTime now);

    @Modifying
    @Query("UPDATE VehiculoAsignacion a SET a.fechaEliminacion = :now WHERE a.conductor.id = :conductorId AND a.fechaEliminacion IS NULL")
    int softDeleteFromConductor(@Param("conductorId")Long conductorId, @Param("now")LocalDateTime now);
}
