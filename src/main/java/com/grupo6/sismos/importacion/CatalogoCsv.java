package com.grupo6.sismos.importacion;

import java.util.List;

/** Catálogos que se pueden importar desde CSV, con las columnas mínimas que exige cada archivo. */
public enum CatalogoCsv {

    UBIGEO("Ubigeo", "ubigeo.csv", List.of("codigo", "nombre", "tipo", "estado")),
    TIPO_ALERTA("Tipos de alerta", "tipo_alerta.csv",
            List.of("codigo", "nombre", "color", "prioridad", "descripcion", "estado")),
    TIPO_DANO("Tipos de daño", "tipo_dano.csv", List.of("codigo", "nombre", "sector", "descripcion", "estado"));

    private final String nombre;
    private final String archivoSugerido;
    private final List<String> columnas;

    CatalogoCsv(String nombre, String archivoSugerido, List<String> columnas) {
        this.nombre = nombre;
        this.archivoSugerido = archivoSugerido;
        this.columnas = columnas;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArchivoSugerido() {
        return archivoSugerido;
    }

    public List<String> getColumnas() {
        return columnas;
    }
}
