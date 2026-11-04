package com.grupo6.sismos.controller;

import com.grupo6.sismos.web.error.ReglaNegocioException;
import org.springframework.validation.BindingResult;

/** Lleva el mensaje de una regla de negocio al formulario, junto al campo que corresponde. */
final class Errores {

    private Errores() {
    }

    static void registrar(BindingResult errores, ReglaNegocioException error) {
        if (error.getCampo() != null) {
            errores.rejectValue(error.getCampo(), "regla", error.getMessage());
        } else {
            errores.reject("regla", error.getMessage());
        }
    }
}
