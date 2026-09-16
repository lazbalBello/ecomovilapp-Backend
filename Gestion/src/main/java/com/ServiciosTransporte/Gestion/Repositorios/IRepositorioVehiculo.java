package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRepositorioVehiculo extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByMatricula(String matricula);

    Optional<Vehiculo> findByGpsId(String gpsId);

    // @SQLRestriction en la entidad ya garantiza que fecha_eliminacion IS NULL,
    // por lo que solo es necesario filtrar por gpsId no nulo.
    List<Vehiculo> findByGpsIdIsNotNull();

    List<Vehiculo> findByMatriculaContainingIgnoreCase(String matricula);

    @Modifying
    @Query("UPDATE Vehiculo v SET v.ruta = null WHERE v.ruta.id = :rutaId")
    int desasociarRuta(@Param("rutaId") Long rutaId);

    /**
     * Devuelve todos los vehículos que han sido eliminados (soft delete).
     * Usa SQL nativo para eludir el filtro {@code @SQLRestriction} de la entidad.
     */
    @Query(value = "SELECT * FROM vehiculo WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<Vehiculo> findAllEliminados();
}
