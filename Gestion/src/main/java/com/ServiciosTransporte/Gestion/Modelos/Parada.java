package com.ServiciosTransporte.Gestion.Modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDateTime;
import java.util.Objects;

@Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
@Entity
@Table(indexes = {@Index(name = "idx_parada_nombre", columnList = "nombre"),
                  @Index(name = "idx_parada_fecha_eliminacion", columnList = "fecha_eliminacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Parada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private double latitud;
    private double longitud;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Parada parada = (Parada) o;
        return getId() != null && Objects.equals(getId(), parada.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
