package com.grupo6.sismos.repository;

import com.grupo6.sismos.dto.reporte.SismosPorMesDto;
import com.grupo6.sismos.entity.Sismo;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SismoRepository extends JpaRepository<Sismo, Long>, JpaSpecificationExecutor<Sismo> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    long countByUbigeoId(Long ubigeoId);

    long countByTipoAlertaId(Long tipoAlertaId);

    /** HU-11: cantidad de sismos que registró cada estación (id de estación, total). */
    @Query("SELECT s.estacion.id, COUNT(s) FROM Sismo s WHERE s.estacion IS NOT NULL GROUP BY s.estacion.id")
    List<Object[]> contarPorEstacion();

    @Query("""
            SELECT COUNT(s) FROM Sismo s
            WHERE s.fechaHora >= :desde AND s.fechaHora < :hasta
              AND (:departamento IS NULL OR s.ubigeo.codigo LIKE CONCAT(:departamento, '%'))
            """)
    long contarEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                         @Param("departamento") String departamento);

    @Query("""
            SELECT MAX(s.magnitud) FROM Sismo s
            WHERE s.fechaHora >= :desde AND s.fechaHora < :hasta
              AND (:departamento IS NULL OR s.ubigeo.codigo LIKE CONCAT(:departamento, '%'))
            """)
    Double magnitudMaximaEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                   @Param("departamento") String departamento);

    @Query("""
            SELECT new com.grupo6.sismos.dto.reporte.SismosPorMesDto(
                YEAR(s.fechaHora), MONTH(s.fechaHora), COUNT(s))
            FROM Sismo s
            WHERE s.fechaHora >= :desde AND s.fechaHora < :hasta
              AND (:departamento IS NULL OR s.ubigeo.codigo LIKE CONCAT(:departamento, '%'))
            GROUP BY YEAR(s.fechaHora), MONTH(s.fechaHora)
            ORDER BY YEAR(s.fechaHora), MONTH(s.fechaHora)
            """)
    List<SismosPorMesDto> contarPorMes(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                       @Param("departamento") String departamento);
}
