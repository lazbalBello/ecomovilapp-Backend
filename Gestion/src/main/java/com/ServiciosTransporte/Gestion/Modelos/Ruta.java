package com.ServiciosTransporte.Gestion.Modelos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
@Entity
@Table(indexes = {@Index(name = "idx_ruta_nombre", columnList = "nombre"),
                  @Index(name = "idx_ruta_fecha_eliminacion", columnList = "fecha_eliminacion")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ElementCollection
    @CollectionTable(name = "coordenada_ruta" , joinColumns = @JoinColumn(name = "ruta_id"))
    @OrderBy("orden ASC")
    @JsonIgnore
    private List<RecorridoRuta> recorrido;

    private String descripcion;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    @Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
    @ToString.Exclude
    private List<Parada> paradas;

    @OneToMany(mappedBy = "ruta")
    @Filter(name = "notDeletedFilter", condition = "fecha_eliminacion IS NULL")
    @ToString.Exclude
    private List<Vehiculo> vehiculosAsignados;

    @PrePersist
    @PreUpdate
    private void ordenarCoordenadas(){
        if (recorrido != null){
            for (int i = 0; i < recorrido.size(); i++)
                recorrido.get(i).setOrden(i + 1);
        }

    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Ruta ruta = (Ruta) o;
        return getId() != null && Objects.equals(getId(), ruta.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
