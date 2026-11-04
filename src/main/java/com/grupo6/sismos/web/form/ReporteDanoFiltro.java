package com.grupo6.sismos.web.form;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.util.UriComponentsBuilder;

/** Filtros de la lista general de reportes de afectación. */
public class ReporteDanoFiltro {

    private String tipoDanoId;
    private String nivel;
    private String estado;
    private String pagina;
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

    public int getPaginaValida() {
        return pagina != null && pagina.matches("\\d{1,6}") && Integer.parseInt(pagina) > 0
                ? Integer.parseInt(pagina) : 1;
    }

    public boolean isTieneFiltros() {
        return noVacio(tipoDanoId) || noVacio(nivel) || noVacio(estado);
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public String urlPagina(int numero) {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath("/reportes");
        agregarSiTieneValor(url, "tipoDanoId", tipoDanoId);
        agregarSiTieneValor(url, "nivel", nivel);
        agregarSiTieneValor(url, "estado", estado);
        url.queryParam("pagina", numero);
        return url.encode().build().toUriString();
    }

    private void agregarSiTieneValor(UriComponentsBuilder url, String parametro, String valor) {
        if (noVacio(valor)) {
            url.queryParam(parametro, valor);
        }
    }

    private boolean noVacio(String valor) {
        return valor != null && !valor.isBlank();
    }
}
