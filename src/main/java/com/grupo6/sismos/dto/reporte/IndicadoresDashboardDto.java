package com.grupo6.sismos.dto.reporte;

public class IndicadoresDashboardDto {
    private final long sismosRegistrados;
    private final Double magnitudMaxima;
    private final long totalAfectados;
    private final long reportesPendientes;

    public IndicadoresDashboardDto(long sismosRegistrados, Double magnitudMaxima,
                                   long totalAfectados, long reportesPendientes) {
        this.sismosRegistrados = sismosRegistrados;
        this.magnitudMaxima = magnitudMaxima;
        this.totalAfectados = totalAfectados;
        this.reportesPendientes = reportesPendientes;
    }

    public long getSismosRegistrados() { return sismosRegistrados; }
    public Double getMagnitudMaxima() { return magnitudMaxima; }
    public long getTotalAfectados() { return totalAfectados; }
    public long getReportesPendientes() { return reportesPendientes; }
}
