package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.ReporteDano;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Reportes de daño (sprint 3). */
public interface ReporteDanoRepository extends JpaRepository<ReporteDano, Long> {

    List<ReporteDano> findBySismoIdOrderByFechaReporteDesc(Long sismoId);

    @Query("SELECT SUM(r.cantidadAfectada) FROM ReporteDano r WHERE r.sismo.id = :sismoId")
    Long sumarCantidadAfectadaPorSismo(@Param("sismoId") Long sismoId);

    long countBySismoId(Long sismoId);

    long countByTipoDanoId(Long tipoDanoId);
}
