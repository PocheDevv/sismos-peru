package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.TipoAlerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoAlertaRepository extends JpaRepository<TipoAlerta, Long> {

    List<TipoAlerta> findAllByOrderByPrioridadAscCodigoAsc();

    List<TipoAlerta> findByEstadoOrderByPrioridadAsc(String estado);

    boolean existsByCodigoIgnoreCase(String codigo);
}
