package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.EstacionMonitoreo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstacionMonitoreoRepository extends JpaRepository<EstacionMonitoreo, Long> {

    List<EstacionMonitoreo> findAllByOrderByCodigoAsc();

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    long countByUbigeoId(Long ubigeoId);
}
