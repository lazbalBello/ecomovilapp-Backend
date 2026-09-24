package com.ServiciosTransporte.Gestion.Repositorios;

import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IRepositorioRuta extends JpaRepository<Ruta, Long> {

    List<Ruta> findByNombreContainingIgnoreCase(String nombre);

    @Query(value = "SELECT * FROM ruta WHERE fecha_eliminacion IS NOT NULL ORDER BY fecha_eliminacion DESC",
           nativeQuery = true)
    List<Ruta> findAllEliminadas();

    long countByFechaCreacionAfter(LocalDateTime fecha);

    @Query("""
        SELECT COUNT(DISTINCT r) FROM Ruta r
        JOIN r.vehiculosAsignados v
        WHERE v.estado = com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo.ACTIVO
          AND r.fechaEliminacion IS NULL
          AND v.fechaEliminacion IS NULL
    """)
    long countRutasActivasConVehiculosActivos();

    @Query("SELECT COUNT(DISTINCT r) FROM Ruta r JOIN r.vehiculosAsignados v WHERE r.fechaEliminacion IS NULL AND v.fechaEliminacion IS NULL")
    long countRutasConVehiculos();

    @Query("SELECT COUNT(r) FROM Ruta r WHERE r.vehiculosAsignados IS EMPTY AND r.fechaEliminacion IS NULL")
    long countRutasSinVehiculos();

    @Query("SELECT COUNT(r) FROM Ruta r WHERE r.paradas IS NOT EMPTY AND r.fechaEliminacion IS NULL")
    long countRutasConParadas();

    @Query("SELECT COUNT(r) FROM Ruta r WHERE r.paradas IS EMPTY AND r.fechaEliminacion IS NULL")
    long countRutasSinParadas();

    @Query("SELECT AVG(CAST(SIZE(r.paradas) AS DOUBLE)) FROM Ruta r WHERE r.fechaEliminacion IS NULL")
    Double promedioParadasPorRuta();
}
