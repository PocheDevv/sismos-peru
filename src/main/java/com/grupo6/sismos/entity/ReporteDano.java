package com.grupo6.sismos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte de daño asociado a un sismo: ReporteDano N ── 1 Sismo, ReporteDano N ── 1 TipoDano.
 * Las pantallas se construyen en el sprint 3 (HU-12, HU-13, HU-25, HU-26, HU-27, HU-28).
 */
@Entity
@Table(name = "reporte_dano")
public class ReporteDano {

    public static final List<String> NIVELES = List.of("BAJO", "MEDIO", "ALTO", "CRITICO");
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_VERIFICADO = "VERIFICADO";
    public static final String ESTADO_CERRADO = "CERRADO";
    public static final List<String> ESTADOS = List.of(ESTADO_PENDIENTE, ESTADO_VERIFICADO, ESTADO_CERRADO);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sismo_id")
    private Sismo sismo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_dano_id")
    private TipoDano tipoDano;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "cantidad_afectada", nullable = false)
    private int cantidadAfectada;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, length = 10)
    private String nivel = "MEDIO";

    @Column(nullable = false, length = 12)
    private String estado = ESTADO_PENDIENTE;

    public Long getId() {
        return id;
    }

    public Sismo getSismo() {
        return sismo;
    }

    public void setSismo(Sismo sismo) {
        this.sismo = sismo;
    }

    public TipoDano getTipoDano() {
        return tipoDano;
    }

    public void setTipoDano(TipoDano tipoDano) {
        this.tipoDano = tipoDano;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public int getCantidadAfectada() {
        return cantidadAfectada;
    }

    public void setCantidadAfectada(int cantidadAfectada) {
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
