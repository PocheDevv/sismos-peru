package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.TipoDano;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoDanoRepository extends JpaRepository<TipoDano, Long> {

    List<TipoDano> findAllByOrderByCodigoAsc();

    List<TipoDano> findByEstadoOrderByNombreAsc(String estado);

    boolean existsByCodigoIgnoreCase(String codigo);
}
