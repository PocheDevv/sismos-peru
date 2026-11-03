package com.grupo6.sismos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Bitácora de seguimiento de un sismo relevante (HU-14, sprint 3): Sismo 1 ── N SeguimientoSismo. */
@Entity
@Table(name = "seguimiento_sismo")
public class SeguimientoSismo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sismo_id")
    private Sismo sismo;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(nullable = false, length = 100)
    private String responsable;

    @Column(nullable = false, length = 500)
    private String observaciones;

    @Column(name = "acciones_realizadas", nullable = false, length = 500)
    private String accionesRealizadas;

    public Long getId() {
        return id;
    }

    public Sismo getSismo() {
        return sismo;
    }

    public void setSismo(Sismo sismo) {
        this.sismo = sismo;
    }

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
