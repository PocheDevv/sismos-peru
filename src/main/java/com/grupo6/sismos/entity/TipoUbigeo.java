package com.grupo6.sismos.entity;

/**
 * Niveles del ubigeo INEI. El código tiene 2 dígitos para departamento (o provincia
 * constitucional), 4 para provincia y 6 para distrito; cada nivel empieza con el código de su padre.
 */
public enum TipoUbigeo {

    DEPARTAMENTO("Departamento", 2),
    PROVINCIA_CONSTITUCIONAL("Provincia constitucional", 2),
    PROVINCIA("Provincia", 4),
    DISTRITO("Distrito", 6);

    private final String nombre;
    private final int longitudCodigo;

    TipoUbigeo(String nombre, int longitudCodigo) {
        this.nombre = nombre;
        this.longitudCodigo = longitudCodigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getLongitudCodigo() {
        return longitudCodigo;
    }

    /** Departamentos y provincias constitucionales no tienen padre. */
    public boolean isRaiz() {
        return longitudCodigo == 2;
    }

    /** Tipo que corresponde a los hijos de este nivel, o null si no admite hijos. */
    public TipoUbigeo getTipoHijo() {
        return switch (this) {
            case DEPARTAMENTO, PROVINCIA_CONSTITUCIONAL -> PROVINCIA;
            case PROVINCIA -> DISTRITO;
            case DISTRITO -> null;
        };
    }

    public static TipoUbigeo desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        for (TipoUbigeo tipo : values()) {
            if (tipo.name().equalsIgnoreCase(texto.trim())) {
                return tipo;
            }
        }
        return null;
    }
}
