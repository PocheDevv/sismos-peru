package com.grupo6.sismos.web.error;

/**
 * Una regla de negocio no se cumple (código repetido, ubigeo inactivo, ...).
 * {@code campo} es el campo del formulario donde se muestra el mensaje, o null si es general.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String campo;

    public ReglaNegocioException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
