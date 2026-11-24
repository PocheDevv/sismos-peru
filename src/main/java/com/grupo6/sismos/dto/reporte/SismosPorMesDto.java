package com.grupo6.sismos.dto.reporte;

public class SismosPorMesDto {
    private final Integer anio;
    private final Integer mes;
    private final Long cantidad;

    public SismosPorMesDto(Integer anio, Integer mes, Long cantidad) {
        this.anio = anio;
        this.mes = mes;
        this.cantidad = cantidad;
    }

    public Integer getAnio() { return anio; }
    public Integer getMes() { return mes; }
    public Long getCantidad() { return cantidad; }
}
