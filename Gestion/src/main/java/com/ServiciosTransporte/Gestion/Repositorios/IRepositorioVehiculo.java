package com.ServiciosTransporte.Gestion.Repositorios;

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
public interface IRepositorioVehiculo extends JpaRepository<Vehiculo , Long> {

    // Solo vehículos activos (no eliminados)
    List<Vehiculo> findByFechaEliminacionIsNull();

    Optional<Vehiculo> findByMatriculaAndFechaEliminacionIsNull(String matricula);

    Optional<Vehiculo> findByImeiDispositivoGpsAndFechaEliminacionIsNull(String imeiDispositivoGps);

    // Mantener estos para compatibilidad interna
    Optional<Vehiculo> findByMatricula(String matricula);

    Optional<Vehiculo> findByImeiDispositivoGps(String imeiDispositivoGps);

    List<Vehiculo> findByMatriculaContainingIgnoreCaseAndFechaEliminacionIsNull(String matricula);

    List<Vehiculo> findByMatriculaContainingIgnoreCase(String matricula);

    @Modifying
    @Query("UPDATE Vehiculo v SET v.ruta = null WHERE v.ruta.id = :rutaId")
    int desasociarRuta(@Param("rutaId") Long rutaId);

    @Query("SELECT COUNT(v) FROM Vehiculo v WHERE v.fechaCreacion >= :desde AND v.fechaEliminacion IS NULL")
    long countCreadosDesde(@Param("desde") LocalDateTime desde);
}
