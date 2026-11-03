package com.grupo6.sismos.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos del formulario de tipo de alerta y de cada fila de tipo_alerta.csv. */
public class TipoAlertaForm {

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 10, message = "El código no puede superar los 10 caracteres.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
    private String nombre;

    @NotBlank(message = "El color es obligatorio.")
    @Pattern(regexp = "VERDE|AMARILLO|NARANJA|ROJO", message = "El color debe ser VERDE, AMARILLO, NARANJA o ROJO.")
    private String color;

    @NotNull(message = "La prioridad es obligatoria.")
    @Positive(message = "La prioridad debe ser un número positivo.")
    private Integer prioridad;

    @Size(max = 200, message = "La descripción no puede superar los 200 caracteres.")
    private String descripcion;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
