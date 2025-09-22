package com.EcoTransporte.GestionSeguimiento.Repositorios;

import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRepositorioConductor extends JpaRepository<Conductor , Long> {

    Optional<Conductor> findByDni(String dni);

    List<Conductor> findByDniContainingIgnoreCase(String dni);

    List<Conductor> findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(String NombreQuery, String ApellidosQuery);
}
