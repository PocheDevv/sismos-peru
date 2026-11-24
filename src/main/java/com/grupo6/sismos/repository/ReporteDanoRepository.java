package com.grupo6.sismos.repository;

import com.grupo6.sismos.dto.reporte.AfectadosPorTipoDto;
import com.grupo6.sismos.entity.ReporteDano;
import java.time.LocalDateTime;
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

    @Query("""
            SELECT COALESCE(SUM(r.cantidadAfectada), 0) FROM ReporteDano r
            WHERE r.sismo.fechaHora >= :desde AND r.sismo.fechaHora < :hasta
              AND (:departamento IS NULL OR r.sismo.ubigeo.codigo LIKE CONCAT(:departamento, '%'))
            """)
    long sumarAfectadosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                 @Param("departamento") String departamento);

    @Query("""
            SELECT COUNT(r) FROM ReporteDano r
            WHERE r.estado = :estado
              AND r.sismo.fechaHora >= :desde AND r.sismo.fechaHora < :hasta
              AND (:departamento IS NULL OR r.sismo.ubigeo.codigo LIKE CONCAT(:departamento, '%'))
            """)
    long contarPorEstadoEnPeriodo(@Param("estado") String estado, @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta, @Param("departamento") String departamento);

    @Query("""
            SELECT new com.grupo6.sismos.dto.reporte.AfectadosPorTipoDto(r.tipoDano.nombre, SUM(r.cantidadAfectada))
            FROM ReporteDano r
            WHERE r.sismo.fechaHora >= :desde AND r.sismo.fechaHora < :hasta
              AND (:departamento IS NULL OR r.sismo.ubigeo.codigo LIKE CONCAT(:departamento, '%'))
            GROUP BY r.tipoDano.nombre
            ORDER BY SUM(r.cantidadAfectada) DESC
            """)
    List<AfectadosPorTipoDto> sumarAfectadosPorTipo(@Param("desde") LocalDateTime desde,
                                                    @Param("hasta") LocalDateTime hasta,
                                                    @Param("departamento") String departamento);

    long countBySismoId(Long sismoId);

    long countByTipoDanoId(Long tipoDanoId);
}
