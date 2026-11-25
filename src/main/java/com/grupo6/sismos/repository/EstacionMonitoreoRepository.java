package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.EstacionMonitoreo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstacionMonitoreoRepository extends JpaRepository<EstacionMonitoreo, Long> {

    List<EstacionMonitoreo> findAllByOrderByCodigoAsc();

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    long countByUbigeoId(Long ubigeoId);

    @Query("""
            SELECT e FROM EstacionMonitoreo e
            LEFT JOIN e.ubigeo u
            LEFT JOIN u.padre p
            LEFT JOIN p.padre d
            WHERE (:departamentoId IS NULL
                   OR u.id = :departamentoId
                   OR p.id = :departamentoId
                   OR d.id = :departamentoId)
              AND (:estado IS NULL OR :estado = '' OR e.estado = :estado)
            ORDER BY e.codigo ASC
            """)
    Page<EstacionMonitoreo> buscarFiltrado(@Param("departamentoId") Long departamentoId,
                                           @Param("estado") String estado,
                                           Pageable pageable);
}