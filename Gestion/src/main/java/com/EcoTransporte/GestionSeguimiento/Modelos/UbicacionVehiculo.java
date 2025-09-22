package com.EcoTransporte.GestionSeguimiento.Modelos;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table
@Data
public class UbicacionVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double latitud;
    private double longitud;
    private LocalDateTime timestamp;

    @OneToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;
}
