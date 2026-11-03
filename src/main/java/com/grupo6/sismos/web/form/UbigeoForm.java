package com.grupo6.sismos.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos del formulario de ubigeo (HU-09) y de cada fila de ubigeo.csv. */
public class UbigeoForm {

    /** Null para departamentos y provincias constitucionales. */
    private Long padreId;

    @NotBlank(message = "El código de ubigeo es obligatorio.")
    @Pattern(regexp = "^\\d{2}(\\d{2}){0,2}$", message = "El código debe tener 2, 4 o 6 dígitos.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres.")
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio.")
    private String tipo;

    @NotBlank(message = "El estado es obligatorio.")
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "El estado debe ser ACTIVO o INACTIVO.")
    private String estado = "ACTIVO";

    public Long getPadreId() {
        return padreId;
    }

    public void setPadreId(Long padreId) {
        this.padreId = padreId;
    }

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
