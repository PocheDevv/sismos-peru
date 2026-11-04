package com.grupo6.sismos.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class ReporteDanoForm {

    @NotNull(message = "Seleccione el tipo de daño.")
    private Long tipoDanoId;

    @NotNull(message = "La fecha y hora del reporte son obligatorias.")
    @PastOrPresent(message = "La fecha del reporte no puede ser futura.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaReporte;

    @NotNull(message = "La cantidad afectada es obligatoria.")
    @Positive(message = "La cantidad afectada debe ser mayor que cero.")
    @Max(value = 10000000, message = "La cantidad afectada no puede superar 10 000 000.")
    private Integer cantidadAfectada;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    private String descripcion;

    @NotBlank(message = "Seleccione el nivel de afectación.")
    private String nivel = "MEDIO";
    
    @NotBlank(message = "Seleccione el estado.")
    private String estado = "PENDIENTE";

    public Long getTipoDanoId() {
        return tipoDanoId;
    }

    public void setTipoDanoId(Long tipoDanoId) {
        this.tipoDanoId = tipoDanoId;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public Integer getCantidadAfectada() {
        return cantidadAfectada;
    }

    public void setCantidadAfectada(Integer cantidadAfectada) {
        this.cantidadAfectada = cantidadAfectada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
}
