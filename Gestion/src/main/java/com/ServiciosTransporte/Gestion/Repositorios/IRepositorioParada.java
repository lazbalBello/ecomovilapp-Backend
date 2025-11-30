package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IRepositorioParada extends JpaRepository<Parada, Long> {

    List<Parada> findByNombreContainingIgnoreCase(String nombre);

    @Modifying
    @Query("UPDATE Parada p SET p.fechaEliminacion = :now WHERE p.ruta.id = :rutaId AND p.fechaEliminacion IS NULL")
    int softDeleteFromRuta(@Param("rutaId")Long rutaId, @Param("now")LocalDateTime now);
}
