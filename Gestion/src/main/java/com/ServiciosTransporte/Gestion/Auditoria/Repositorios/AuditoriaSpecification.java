package com.ServiciosTransporte.Gestion.Auditoria.Repositorios;

import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaFilterDto;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.AuditoriaLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AuditoriaSpecification {

    private AuditoriaSpecification() {
        // Utility class
    }

    public static Specification<AuditoriaLog> conFiltros(AuditoriaFilterDto filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.nombreTabla() != null && !filtro.nombreTabla().trim().isEmpty()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("nombreTabla")),
                        filtro.nombreTabla().trim().toLowerCase()
                ));
            }

            if (filtro.tipoOperacion() != null) {
                predicates.add(cb.equal(root.get("tipoOperacion"), filtro.tipoOperacion()));
            }

            if (filtro.entidadId() != null) {
                predicates.add(cb.equal(root.get("entidadId"), filtro.entidadId()));
            }

            if (filtro.usuarioId() != null && !filtro.usuarioId().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("usuarioId"), filtro.usuarioId().trim()));
            }

            if (filtro.fechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaOperacion"), filtro.fechaInicio()));
            }

            if (filtro.fechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaOperacion"), filtro.fechaFin()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
