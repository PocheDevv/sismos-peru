package com.grupo6.sismos.entity;

import jakarta.persistence.*;
import java.util.List;

/** Clasificación de alerta: TipoAlerta 1 ── N Sismo. */
@Entity
@Table(name = "tipo_alerta")
public class TipoAlerta {

    public static final List<String> COLORES = List.of("VERDE", "AMARILLO", "NARANJA", "ROJO");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 15)
    private String color;

    @Column(nullable = false)
    private int prioridad;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 10)
    private String estado = EstadoRegistro.ACTIVO;

    public boolean isActivo() {
        return EstadoRegistro.ACTIVO.equals(estado);
    }

    /** Clase CSS del indicador de color (alerta-verde, alerta-rojo, ...). */
    public String getClaseColor() {
        return "alerta-color alerta-" + (color == null ? "verde" : color.toLowerCase());
    }

    public Long getId() {
        return id;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
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
