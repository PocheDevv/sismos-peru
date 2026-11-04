package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.ReporteDano;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Reportes de daño (sprint 3). */
public interface ReporteDanoRepository extends JpaRepository<ReporteDano, Long> {

    List<ReporteDano> findBySismoIdOrderByFechaReporteDesc(Long sismoId);

    @Query("""
            SELECT r FROM ReporteDano r
            WHERE (:tipoDanoId IS NULL OR r.tipoDano.id = :tipoDanoId)
              AND (:nivel IS NULL OR r.nivel = :nivel)
              AND (:estado IS NULL OR r.estado = :estado)
            """)
    Page<ReporteDano> buscarPorFiltros(@Param("tipoDanoId") Long tipoDanoId,
                                      @Param("nivel") String nivel,
                                      @Param("estado") String estado,
                                      Pageable pageable);

    @Query("SELECT SUM(r.cantidadAfectada) FROM ReporteDano r WHERE r.sismo.id = :sismoId")
    Long sumarCantidadAfectadaPorSismo(@Param("sismoId") Long sismoId);

    long countBySismoId(Long sismoId);

    long countByTipoDanoId(Long tipoDanoId);
}
