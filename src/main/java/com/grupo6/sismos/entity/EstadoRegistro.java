package com.grupo6.sismos.entity;

import java.util.List;

/** Estados de los catálogos y de las estaciones: un registro INACTIVO no se puede usar en registros nuevos. */
public final class EstadoRegistro {

    public static final String ACTIVO = "ACTIVO";
    public static final String INACTIVO = "INACTIVO";
    public static final List<String> VALORES = List.of(ACTIVO, INACTIVO);

    private EstadoRegistro() {
    }

    public static boolean esValido(String estado) {
        return estado != null && VALORES.contains(estado);
    }
}
