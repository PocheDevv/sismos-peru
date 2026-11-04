package com.grupo6.sismos.web.form;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Filtros, orden y página de la lista de sismos (HU-15, HU-30, HU-31).
 * Se llena con los parámetros de la URL (GET /sismos?q=...&departamentoId=...).
 */
public class SismoFiltro {

    public static final int TAMANIO_PAGINA = 10;
    public static final List<String> ORDENES = List.of(
            "codigo", "fecha", "magnitud", "profundidad", "ubicacion", "alerta", "estado");

    private String q;
    private Long departamentoId;
    private Double magnitudMin;
    private Double magnitudMax;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
    private String estado;
    private Long tipoAlertaId;
    private String orden;
    private String dir;
    private Integer pagina;

    /** Mensajes de filtros que no se aplicaron (rango invertido, valor inválido). */
    private final List<String> avisos = new ArrayList<>();

    public String getTextoLimpio() {
        return q == null ? "" : q.trim();
    }

    public String getOrdenValido() {
        return orden != null && ORDENES.contains(orden) ? orden : "fecha";
    }

    /** Por defecto la fecha va descendente (lo más reciente primero) y lo demás ascendente. */
    public boolean isDescendente() {
        if (dir == null || dir.isBlank()) {
            return "fecha".equals(getOrdenValido());
        }
        return "desc".equalsIgnoreCase(dir);
    }

    public int getPaginaValida() {
        return pagina == null || pagina < 1 ? 1 : pagina;
    }

    public boolean isTieneFiltros() {
        return !getTextoLimpio().isEmpty() || departamentoId != null || magnitudMin != null
                || magnitudMax != null || desde != null || hasta != null
                || (estado != null && !estado.isBlank()) || tipoAlertaId != null;
    }

    private Map<String, Object> parametrosFiltro() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("q", getTextoLimpio().isEmpty() ? null : getTextoLimpio());
        p.put("departamentoId", departamentoId);
        p.put("magnitudMin", magnitudMin);
        p.put("magnitudMax", magnitudMax);
        p.put("desde", desde);
        p.put("hasta", hasta);
        p.put("estado", estado == null || estado.isBlank() ? null : estado);
        p.put("tipoAlertaId", tipoAlertaId);
        return p;
    }

    private String url(Map<String, Object> parametros) {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath("/sismos");
        parametros.forEach((nombre, valor) -> {
            if (valor != null) {
                url.queryParam(nombre, valor);
            }
        });
        return url.encode().build().toUriString();
    }

    /** URL de otra página conservando filtros y orden. */
    public String urlPagina(int numero) {
        Map<String, Object> p = parametrosFiltro();
        p.put("orden", getOrdenValido());
        p.put("dir", isDescendente() ? "desc" : "asc");
        p.put("pagina", numero);
        return url(p);
    }

    /** URL para ordenar por una columna: si ya está ordenada por ella, invierte la dirección. */
    public String urlOrden(String columna) {
        boolean descendente = columna.equals(getOrdenValido()) ? !isDescendente() : "fecha".equals(columna);
        Map<String, Object> p = parametrosFiltro();
        p.put("orden", columna);
        p.put("dir", descendente ? "desc" : "asc");
        return url(p);
    }

    /** Flecha que se muestra junto al encabezado de la columna ordenada. */
    public String indicadorOrden(String columna) {
        if (!columna.equals(getOrdenValido())) {
            return "";
        }
        return isDescendente() ? " ▼" : " ▲";
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Long getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Long departamentoId) {
        this.departamentoId = departamentoId;
    }

    public Double getMagnitudMin() {
        return magnitudMin;
    }

    public void setMagnitudMin(Double magnitudMin) {
        this.magnitudMin = magnitudMin;
    }

    public Double getMagnitudMax() {
        return magnitudMax;
    }

    public void setMagnitudMax(Double magnitudMax) {
        this.magnitudMax = magnitudMax;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getTipoAlertaId() {
        return tipoAlertaId;
    }

    public void setTipoAlertaId(Long tipoAlertaId) {
        this.tipoAlertaId = tipoAlertaId;
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

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }
}
