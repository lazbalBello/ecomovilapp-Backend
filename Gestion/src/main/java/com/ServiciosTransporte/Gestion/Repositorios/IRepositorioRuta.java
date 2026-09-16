package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRepositorioRuta extends JpaRepository<Ruta, Long> {

    List<Ruta> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Devuelve todas las rutas que han sido eliminadas (soft delete).
     * Usa SQL nativo para eludir el filtro {@code @SQLRestriction} de la entidad.
     */
    @Query(value = "SELECT * FROM ruta WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<Ruta> findAllEliminadas();
}
