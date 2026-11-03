package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.Ubigeo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbigeoRepository extends JpaRepository<Ubigeo, Long> {

    Optional<Ubigeo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Ubigeo> findByPadreIsNullOrderByCodigoAsc();

    List<Ubigeo> findByPadreIdOrderByCodigoAsc(Long padreId);

    List<Ubigeo> findByEstadoOrderByCodigoAsc(String estado);

    long countByPadreId(Long padreId);

    boolean existsByPadreIsNullAndNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByPadreIdAndNombreIgnoreCaseAndIdNot(Long padreId, String nombre, Long id);
}
