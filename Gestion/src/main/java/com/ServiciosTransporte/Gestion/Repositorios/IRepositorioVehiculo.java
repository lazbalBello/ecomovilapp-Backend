package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo;
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
public interface IRepositorioVehiculo extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByMatricula(String matricula);

    Optional<Vehiculo> findByGpsId(String gpsId);

    List<Vehiculo> findByGpsIdIsNotNull();

    List<Vehiculo> findByMatriculaContainingIgnoreCase(String matricula);

    @Modifying
    @Query("UPDATE Vehiculo v SET v.ruta = null WHERE v.ruta.id = :rutaId")
    int desasociarRuta(@Param("rutaId") Long rutaId);

    @Query(value = "SELECT * FROM vehiculo WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<Vehiculo> findAllEliminados();

    long countByEstado(EstadoVehiculo estado);

    long countByFechaCreacionAfter(LocalDateTime fecha);

    long countByMarca(String marca);

    long countByTipoBateria(String tipoBateria);

    long countByRutaIsNotNull();

    long countByGpsIdIsNotNull();

    @Query("SELECT COUNT(v) FROM Vehiculo v WHERE v.estado = com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo.ACTIVO AND v.fechaEliminacion IS NULL")
    long countVehiculosActivos();

    @Query("SELECT COUNT(v) FROM Vehiculo v WHERE v.estado = com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo.ACTIVO AND v.ruta IS NOT NULL AND v.fechaEliminacion IS NULL")
    long countVehiculosActivosConRuta();
}
