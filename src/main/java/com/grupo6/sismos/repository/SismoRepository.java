package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.Sismo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SismoRepository extends JpaRepository<Sismo, Long>, JpaSpecificationExecutor<Sismo> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    long countByUbigeoId(Long ubigeoId);

    long countByTipoAlertaId(Long tipoAlertaId);

    /** HU-11: cantidad de sismos que registró cada estación (id de estación, total). */
    @Query("SELECT s.estacion.id, COUNT(s) FROM Sismo s WHERE s.estacion IS NOT NULL GROUP BY s.estacion.id")
    List<Object[]> contarPorEstacion();
}
