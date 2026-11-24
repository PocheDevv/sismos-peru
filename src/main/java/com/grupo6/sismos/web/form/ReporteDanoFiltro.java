package com.grupo6.sismos.web.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

/** Filtros de la lista general de reportes de afectación. */
public class ReporteDanoFiltro {

    public static final List<Integer> TAMANIOS_PAGINA = List.of(10, 25, 50);
    public static final List<String> ORDENES = List.of(
            "fecha", "sismo", "ubicacion", "tipo", "cantidad", "nivel", "estado");

    private String tipoDanoId;
    private String nivel;
    private String estado;
    private String pagina;
    private String tamanio;
    private String orden;
    private String dir;
    private final List<String> avisos = new ArrayList<>();

    public String getTipoDanoId() {
        return tipoDanoId;
    }

    public void setTipoDanoId(String tipoDanoId) {
        this.tipoDanoId = tipoDanoId;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPagina() {
        return pagina;
    }

    public void setPagina(String pagina) {
        this.pagina = pagina;
    }

    public String getTamanio() {
        return tamanio;
    }

    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }

    public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getPaginaValida() {
        return pagina != null && pagina.matches("\\d{1,6}") && Integer.parseInt(pagina) > 0
                ? Integer.parseInt(pagina) : 1;
    }

    public int getTamanioValido() {
        if (tamanio == null || tamanio.isBlank()) {
            return 10;
        }
        try {
            int valor = Integer.parseInt(tamanio);
            return TAMANIOS_PAGINA.contains(valor) ? valor : 10;
        } catch (NumberFormatException error) {
            return 10;
        }
    }

    public String getOrdenValido() {
        return orden != null && ORDENES.contains(orden) ? orden : "fecha";
    }

    public boolean isDescendente() {
        if (dir == null || dir.isBlank()) {
            return "fecha".equals(getOrdenValido());
        }
        return "desc".equalsIgnoreCase(dir);
    }

    public boolean isTieneFiltros() {
        return noVacio(tipoDanoId) || noVacio(nivel) || noVacio(estado);
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public String urlPagina(int numero) {
        Map<String, Object> parametros = parametrosBase();
        parametros.put("pagina", numero);
        return url(parametros);
    }

    public String urlOrden(String columna) {
        boolean descendente = columna.equals(getOrdenValido()) ? !isDescendente() : "fecha".equals(columna);
        Map<String, Object> parametros = parametrosBase();
        parametros.put("orden", columna);
        parametros.put("dir", descendente ? "desc" : "asc");
        parametros.put("pagina", 1);
        return url(parametros);
    }

    public String indicadorOrden(String columna) {
        if (!columna.equals(getOrdenValido())) {
            return "";
        }
        return isDescendente() ? " ▼" : " ▲";
    }

    private Map<String, Object> parametrosBase() {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("tipoDanoId", noVacio(tipoDanoId) ? tipoDanoId : null);
        parametros.put("nivel", noVacio(nivel) ? nivel : null);
        parametros.put("estado", noVacio(estado) ? estado : null);
        parametros.put("tamanio", getTamanioValido());
        parametros.put("orden", getOrdenValido());
        parametros.put("dir", isDescendente() ? "desc" : "asc");
        return parametros;
    }

    private String url(Map<String, Object> parametros) {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath("/reportes");
        parametros.forEach((nombre, valor) -> {
            if (valor != null) {
                url.queryParam(nombre, valor);
            }
        });
        return url.encode().build().toUriString();
    }

    private boolean noVacio(String valor) {
        return valor != null && !valor.isBlank();
    }
}
