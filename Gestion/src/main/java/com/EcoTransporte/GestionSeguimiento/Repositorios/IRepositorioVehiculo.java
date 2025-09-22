package com.EcoTransporte.GestionSeguimiento.Repositorios;

import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRepositorioVehiculo extends JpaRepository<Vehiculo , Long> {

    Optional<Vehiculo> findByMatricula(String matricula);

    List<Vehiculo> findByMatriculaContainingIgnoreCase(String matricula);

    @Modifying
    @Query("UPDATE Vehiculo v SET v.ruta = null WHERE v.ruta.id = :rutaId")
    int desasociarRuta(@Param("rutaId") Long rutaId);
}
