package com.grupo6.sismos.web.form;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/** Datos del formulario de sismo (registrar y editar). HU-03 y HU-04 con Bean Validation. */
public class SismoForm {

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 20, message = "El código no puede superar los 20 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "El código solo admite letras, números y guiones.")
    private String codigo;

    @NotNull(message = "La fecha y hora son obligatorias.")
    @PastOrPresent(message = "La fecha de ocurrencia no puede ser futura.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHora;

    @NotNull(message = "La magnitud es obligatoria.")
    @DecimalMin(value = "0", inclusive = false, message = "La magnitud debe ser mayor que cero.")
    @DecimalMax(value = "10", message = "La magnitud no puede ser mayor que 10.")
    private Double magnitud;

    @NotNull(message = "La profundidad es obligatoria.")
    @PositiveOrZero(message = "La profundidad no puede ser negativa.")
    private Double profundidad;

    @NotNull(message = "La latitud es obligatoria.")
    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90.")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90.")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria.")
    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180.")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180.")
    private Double longitud;

    @NotNull(message = "Seleccione el ubigeo donde ocurrió el sismo.")
    private Long ubigeoId;

    @NotNull(message = "Seleccione el tipo de alerta.")
    private Long tipoAlertaId;

    private Long estacionId;

    private String intensidad;

    @Size(max = 150, message = "La referencia no puede superar los 150 caracteres.")
    private String referencia;

    /** Solo se usa al editar (HU-08); al registrar el estado es Registrado. */
    private String estado;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Double getMagnitud() {
        return magnitud;
    }

    public void setMagnitud(Double magnitud) {
        this.magnitud = magnitud;
    }

    public Double getProfundidad() {
        return profundidad;
    }

    public void setProfundidad(Double profundidad) {
        this.profundidad = profundidad;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Long getUbigeoId() {
        return ubigeoId;
    }

    public void setUbigeoId(Long ubigeoId) {
        this.ubigeoId = ubigeoId;
    }

    public Long getTipoAlertaId() {
        return tipoAlertaId;
    }

    public void setTipoAlertaId(Long tipoAlertaId) {
        this.tipoAlertaId = tipoAlertaId;
    }

    public Long getEstacionId() {
        return estacionId;
    }

    public void setEstacionId(Long estacionId) {
        this.estacionId = estacionId;
    }

    public String getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(String intensidad) {
        this.intensidad = intensidad;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
