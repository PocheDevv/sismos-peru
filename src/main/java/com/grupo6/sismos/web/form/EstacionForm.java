package com.grupo6.sismos.web.form;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** Datos del formulario de estación de monitoreo (HU-10). */
public class EstacionForm {

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 10, message = "El código no puede superar los 10 caracteres.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombre;

    @NotNull(message = "Seleccione el ubigeo de la estación.")
    private Long ubigeoId;

    @NotNull(message = "La latitud es obligatoria.")
    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90.")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90.")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria.")
    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180.")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180.")
    private Double longitud;

    @NotNull(message = "La fecha de instalación es obligatoria.")
    @PastOrPresent(message = "La fecha de instalación no puede ser futura.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInstalacion;

    @NotBlank(message = "El estado es obligatorio.")
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "El estado debe ser ACTIVO o INACTIVO.")
    private String estado = "ACTIVO";

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getUbigeoId() {
        return ubigeoId;
    }

    public void setUbigeoId(Long ubigeoId) {
        this.ubigeoId = ubigeoId;
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

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(LocalDate fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
