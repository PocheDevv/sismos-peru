package com.grupo6.sismos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HU-19, HU-37 y HU-38: autenticación con login propio y autorización por rol.
 * <ul>
 *   <li>Sin sesión: solo /login y los recursos estáticos; el resto redirige al login.</li>
 *   <li>CONSULTA: puede ver listas, detalles y el dashboard.</li>
 *   <li>ADMIN: además registra, edita, elimina, cambia estados e importa CSV.</li>
 * </ul>
 * Toda petición POST cambia datos, así que exige ADMIN.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Pantallas GET que solo abre el administrador (formularios y confirmaciones). */
    private static final String[] PANTALLAS_ADMIN = {
            "/importacion/**",
            "/sismos/nuevo", "/sismos/*/editar", "/sismos/*/eliminar",
            "/sismos/*/reportes/nuevo", "/sismos/*/reportes/*/editar",
            "/sismos/*/seguimientos/nuevo",
            "/ubigeos/nuevo", "/ubigeos/*/editar", "/ubigeos/*/eliminar",
            "/estaciones/nuevo", "/estaciones/*/editar",
            "/tipos-alerta/nuevo", "/tipos-dano/nuevo"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        // El aviso de acceso denegado (HU-37) lo ve cualquier usuario con sesión,
                        // también cuando llega reenviado desde un POST que se le negó.
                        .requestMatchers("/acceso-denegado").authenticated()
                        .requestMatchers(HttpMethod.POST, "/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
                        .requestMatchers(PANTALLAS_ADMIN).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?salir")
                        .permitAll())
                .exceptionHandling(errores -> errores.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarioDetailsService);
        proveedor.setPasswordEncoder(passwordEncoder);
        return proveedor;
    }
}
