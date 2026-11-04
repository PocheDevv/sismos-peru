package com.grupo6.sismos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento sísmico. Ubigeo 1 ── N Sismo, TipoAlerta 1 ── N Sismo, Estación 1 ── N Sismo (opcional)
 * y Sismo 1 ── N ReporteDano.
 */
@Entity
@Table(name = "sismo")
public class Sismo {

    public static final String ESTADO_REGISTRADO = "Registrado";
    public static final String ESTADO_EN_EVALUACION = "En evaluación";
    public static final String ESTADO_EN_SEGUIMIENTO = "En seguimiento";
    public static final String ESTADO_CERRADO = "Cerrado";
    public static final List<String> ESTADOS = List.of(
            ESTADO_REGISTRADO, ESTADO_EN_EVALUACION, ESTADO_EN_SEGUIMIENTO, ESTADO_CERRADO);

    /** Escala de Mercalli Modificada (MM). */
    public static final List<String> INTENSIDADES = List.of(
            "I (MM)", "II (MM)", "III (MM)", "IV (MM)", "V (MM)", "VI (MM)",
            "VII (MM)", "VIII (MM)", "IX (MM)", "X (MM)", "XI (MM)", "XII (MM)");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private double magnitud;

    @Column(nullable = false)
    private double profundidad;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(length = 150)
    private String referencia;

    @Column(length = 10)
    private String intensidad;

    @Column(nullable = false, length = 20)
    private String estado = ESTADO_REGISTRADO;

    /** HU-29: marca de sismo relevante para seguimiento. */
    @Column(nullable = false)
    private boolean relevante;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ubigeo_id")
    private Ubigeo ubigeo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_alerta_id")
    private TipoAlerta tipoAlerta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estacion_id")
    private EstacionMonitoreo estacion;

    public boolean isEnSeguimiento() {
        return ESTADO_EN_SEGUIMIENTO.equals(estado);
    }

    /** Clase CSS del badge de estado. */
    public String getClaseEstado() {
        return switch (estado) {
            case ESTADO_EN_SEGUIMIENTO -> "badge badge-seguimiento";
            case ESTADO_EN_EVALUACION -> "badge badge-evaluacion";
            case ESTADO_CERRADO -> "badge badge-cerrado";
            default -> "badge badge-registrado";
        };
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public double getMagnitud() {
        return magnitud;
    }

    public void setMagnitud(double magnitud) {
        this.magnitud = magnitud;
    }

    public double getProfundidad() {
        return profundidad;
    }

    public void setProfundidad(double profundidad) {
        this.profundidad = profundidad;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(String intensidad) {
        this.intensidad = intensidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isRelevante() {
        return relevante;
    }

    public void setRelevante(boolean relevante) {
        this.relevante = relevante;
    }

    public Ubigeo getUbigeo() {
        return ubigeo;
    }

    public void setUbigeo(Ubigeo ubigeo) {
        this.ubigeo = ubigeo;
    }

    public TipoAlerta getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(TipoAlerta tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public EstacionMonitoreo getEstacion() {
        return estacion;
    }

    public void setEstacion(EstacionMonitoreo estacion) {
        this.estacion = estacion;
    }
}
