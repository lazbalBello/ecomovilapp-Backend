package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRepositorioRuta extends JpaRepository<Ruta , Long> {

    List<Ruta> findByNombreContainingIgnoreCase(String nombre);
}
