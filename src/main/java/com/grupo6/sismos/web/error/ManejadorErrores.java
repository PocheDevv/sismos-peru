package com.grupo6.sismos.web.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException error, Model model) {
        model.addAttribute("mensaje", error.getMessage());
        return "error/404";
    }

    /** Respaldo: la base de datos rechazó la operación por una restricción (FK, UNIQUE, CHECK). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String integridad(DataIntegrityViolationException error, Model model) {
        model.addAttribute("mensaje",
                "La base de datos rechazó la operación porque viola una restricción (dato repetido o en uso).");
        return "error/general";
    }
}
