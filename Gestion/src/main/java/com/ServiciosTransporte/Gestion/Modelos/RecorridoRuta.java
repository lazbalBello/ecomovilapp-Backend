package com.ServiciosTransporte.Gestion.Modelos;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Generated;

@Embeddable
@Table(name = "RecorridoRuta")
@Data
public class RecorridoRuta {
    private double latitud;

    private double longitud;

    private Integer orden;
}
