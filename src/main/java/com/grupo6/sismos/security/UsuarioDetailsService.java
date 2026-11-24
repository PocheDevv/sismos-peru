package com.grupo6.sismos.security;

import com.grupo6.sismos.entity.EstadoRegistro;
import com.grupo6.sismos.entity.Usuario;
import com.grupo6.sismos.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** HU-19: Spring Security carga desde la tabla usuario solo a los usuarios ACTIVOS. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameAndEstado(username, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos."));
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol())
                .build();
    }
}
