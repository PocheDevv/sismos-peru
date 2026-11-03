package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.web.form.SismoFiltro;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** HU-15, HU-30 y HU-31: condiciones de la búsqueda de sismos según los filtros aplicados. */
public final class SismoEspecificaciones {

    private SismoEspecificaciones() {
    }

    /**
     * @param filtro            filtros ya revisados por el servicio (los inválidos llegan en null)
     * @param codigoDepartamento código del departamento elegido, o null
     */
    public static Specification<Sismo> segun(SismoFiltro filtro, String codigoDepartamento) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            Join<Sismo, Ubigeo> ubigeo = root.join("ubigeo");

            String texto = filtro.getTextoLimpio();
            if (!texto.isEmpty()) {
                Join<Ubigeo, Ubigeo> padre = ubigeo.join("padre", JoinType.LEFT);
                String patron = "%" + texto.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
                condiciones.add(cb.or(
                        cb.equal(cb.lower(root.get("codigo")), texto.toLowerCase(Locale.ROOT)),
                        cb.like(cb.lower(root.get("referencia")), patron, '!'),
                        cb.like(cb.lower(ubigeo.get("nombre")), patron, '!'),
                        cb.like(cb.lower(padre.get("nombre")), patron, '!')));
            }
            if (codigoDepartamento != null) {
                condiciones.add(cb.like(ubigeo.get("codigo"), codigoDepartamento + "%"));
            }
            if (filtro.getMagnitudMin() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("magnitud"), filtro.getMagnitudMin()));
            }
            if (filtro.getMagnitudMax() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("magnitud"), filtro.getMagnitudMax()));
            }
            if (filtro.getDesde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), filtro.getDesde().atStartOfDay()));
            }
            if (filtro.getHasta() != null) {
                condiciones.add(cb.lessThan(root.get("fechaHora"), filtro.getHasta().plusDays(1).atStartOfDay()));
            }
            if (filtro.getEstado() != null && !filtro.getEstado().isBlank()) {
                condiciones.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }
            if (filtro.getTipoAlertaId() != null) {
                condiciones.add(cb.equal(root.get("tipoAlerta").get("id"), filtro.getTipoAlertaId()));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
