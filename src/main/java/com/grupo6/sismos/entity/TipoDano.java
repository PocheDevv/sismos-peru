package com.grupo6.sismos.entity;

import jakarta.persistence.*;

/** Tipo de daño: TipoDano 1 ── N ReporteDano. */
@Entity
@Table(name = "tipo_dano")
public class TipoDano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String sector;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 10)
    private String estado = EstadoRegistro.ACTIVO;

    public boolean isActivo() {
        return EstadoRegistro.ACTIVO.equals(estado);
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

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
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
