package com.grupo6.sismos.importacion;

import java.util.ArrayList;
import java.util.List;

/** Resumen de una importación: filas leídas, importadas y rechazadas con su motivo. */
public class ResultadoImportacion {

    private final CatalogoCsv catalogo;
    private final String archivo;
    private int filasLeidas;
    private int importadas;
    private final List<FilaRechazada> rechazadas = new ArrayList<>();

    public ResultadoImportacion(CatalogoCsv catalogo, String archivo) {
        this.catalogo = catalogo;
        this.archivo = archivo;
    }

    public void sumarLeida() {
        filasLeidas++;
    }

    public void sumarImportada() {
        importadas++;
    }

    public void rechazar(int linea, String codigo, String motivo) {
        rechazadas.add(new FilaRechazada(linea, codigo, motivo));
    }

    public CatalogoCsv getCatalogo() {
        return catalogo;
    }

    public String getArchivo() {
        return archivo;
    }

    public int getFilasLeidas() {
        return filasLeidas;
    }

    public int getImportadas() {
        return importadas;
    }

    public List<FilaRechazada> getRechazadas() {
        return rechazadas;
    }
}
