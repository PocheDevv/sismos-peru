package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.ReporteDano;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Reportes de daño (sprint 3). */
public interface ReporteDanoRepository extends JpaRepository<ReporteDano, Long> {

    List<ReporteDano> findBySismoIdOrderByFechaReporteDesc(Long sismoId);

    long countBySismoId(Long sismoId);

    long countByTipoDanoId(Long tipoDanoId);
}
