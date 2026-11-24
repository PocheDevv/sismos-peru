package com.grupo6.sismos.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** HU-19: pantalla de login. HU-37: aviso de acceso denegado cuando el rol no alcanza. */
@Controller
public class SeguridadController {

    @GetMapping("/login")
    public String login() {
        return "seguridad/login";
    }

    @RequestMapping("/acceso-denegado")
    public String accesoDenegado(HttpServletResponse respuesta) {
        respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "error/403";
    }
}
