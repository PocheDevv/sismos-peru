package com.grupo6.sismos.repository;

import com.grupo6.sismos.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameAndEstado(String username, String estado);
}
