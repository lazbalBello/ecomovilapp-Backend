package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IRepositorioRuta extends JpaRepository<Ruta , Long> {

    List<Ruta> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT COUNT(r) FROM Ruta r WHERE r.fechaCreacion >= :desde AND r.fechaEliminacion IS NULL")
    long countCreadasDesde(@Param("desde") LocalDateTime desde);
}
