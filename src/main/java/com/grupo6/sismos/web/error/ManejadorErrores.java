package com.grupo6.sismos.web.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException error, Model model) {
        model.addAttribute("mensaje", error.getMessage());
        return "error/404";
    }

    /** Un parámetro de la URL tiene un valor que no corresponde (por ejemplo, letras en el número de página). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parametroInvalido(MethodArgumentTypeMismatchException error, Model model) {
        model.addAttribute("mensaje",
                "El valor de «" + error.getName() + "» en la dirección no es válido. Vuelva a la lista e inténtelo de nuevo.");
        return "error/general";
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
