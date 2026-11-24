package com.grupo6.sismos.importacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Resumen de una importación: filas leídas, importadas y rechazadas con su motivo. */
public class ResultadoImportacion {

    private final CatalogoCsv catalogo;
    private final String archivo;
    private int filasLeidas;
    private int importadas;
    private int yaRegistradas;
    private int omitidas;
    private final List<FilaRechazada> rechazadas = new ArrayList<>();
    /** Ubigeo oficial: cuántos registros nuevos se crearon por nivel (departamento, provincia, distrito). */
    private final Map<String, Integer> creadosPorNivel = new LinkedHashMap<>();

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

    /** La fila ya estaba en la base con el mismo código: no se duplica ni cuenta como error. */
    public void sumarYaRegistrada() {
        yaRegistradas++;
    }

    /** Línea que no es un dato, como las notas al pie del archivo oficial. */
    public void sumarOmitida() {
        omitidas++;
    }

    public void sumarCreado(String nivel) {
        creadosPorNivel.merge(nivel, 1, Integer::sum);
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

    public int getYaRegistradas() {
        return yaRegistradas;
    }

    public int getOmitidas() {
        return omitidas;
    }

    public Map<String, Integer> getCreadosPorNivel() {
        return creadosPorNivel;
    }

    public int getTotalCreados() {
        return creadosPorNivel.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<FilaRechazada> getRechazadas() {
        return rechazadas;
    }
}
