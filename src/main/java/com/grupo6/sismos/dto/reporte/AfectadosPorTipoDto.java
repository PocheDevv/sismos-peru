package com.grupo6.sismos.dto.reporte;

public class AfectadosPorTipoDto {
    private final String tipoDano;
    private final Long total;

    public AfectadosPorTipoDto(String tipoDano, Long total) {
        this.tipoDano = tipoDano;
        this.total = total;
    }

    public String getTipoDano() { return tipoDano; }
    public Long getTotal() { return total; }
}
