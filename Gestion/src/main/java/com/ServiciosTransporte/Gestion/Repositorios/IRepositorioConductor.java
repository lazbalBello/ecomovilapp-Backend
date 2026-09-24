package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRepositorioConductor extends JpaRepository<Conductor, Long> {

    Optional<Conductor> findByDni(String dni);

    List<Conductor> findByDniContainingIgnoreCase(String dni);

    List<Conductor> findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(String NombreQuery, String ApellidosQuery);

    @Query(value = "SELECT * FROM conductor WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<Conductor> findAllEliminados();

    long countByFechaCreacionAfter(LocalDateTime fecha);

    long countByDisponibilidadTrue();

    long countByDisponibilidadFalse();
}
