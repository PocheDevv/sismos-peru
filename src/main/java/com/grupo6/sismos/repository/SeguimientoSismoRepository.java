package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.SeguimientoSismo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Bitácora de seguimiento (sprint 3, HU-14). */
public interface SeguimientoSismoRepository extends JpaRepository<SeguimientoSismo, Long> {

    List<SeguimientoSismo> findBySismoIdOrderByFechaActualizacionDesc(Long sismoId);
}
