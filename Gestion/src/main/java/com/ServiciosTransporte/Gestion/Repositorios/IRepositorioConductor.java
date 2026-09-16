package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRepositorioConductor extends JpaRepository<Conductor, Long> {

    Optional<Conductor> findByDni(String dni);

    List<Conductor> findByDniContainingIgnoreCase(String dni);

    List<Conductor> findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(String NombreQuery, String ApellidosQuery);

    /**
     * Devuelve todos los conductores que han sido eliminados (soft delete).
     * Usa SQL nativo para eludir el filtro {@code @SQLRestriction} de la entidad.
     */
    @Query(value = "SELECT * FROM conductor WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<Conductor> findAllEliminados();
}
