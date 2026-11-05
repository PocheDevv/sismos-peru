package com.grupo6.sismos.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/** HU-14: datos de una entrada de la bitácora de seguimiento. */
public class SeguimientoForm {

    @NotNull(message = "La fecha y hora de la actualización son obligatorias.")
    @PastOrPresent(message = "La fecha de la actualización no puede ser futura.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaActualizacion;

    @NotBlank(message = "El responsable es obligatorio.")
    @Size(max = 100, message = "El responsable no puede superar los 100 caracteres.")
    private String responsable;

    @NotBlank(message = "Las observaciones son obligatorias.")
    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    private String observaciones;

    @NotBlank(message = "Las acciones realizadas son obligatorias.")
    @Size(max = 500, message = "Las acciones realizadas no pueden superar los 500 caracteres.")
    private String accionesRealizadas;

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getAccionesRealizadas() {
        return accionesRealizadas;
    }

    public void setAccionesRealizadas(String accionesRealizadas) {
        this.accionesRealizadas = accionesRealizadas;
    }
}
