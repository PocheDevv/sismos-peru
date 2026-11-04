package com.grupo6.sismos.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ubigeo: Ubigeo 1 ── N Sismo. Es jerárquico: un distrito tiene como padre a su provincia, etc. */
@Entity
@Table(name = "ubigeo")
public class Ubigeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoUbigeo tipo;

    @Column(nullable = false, length = 10)
    private String estado = EstadoRegistro.ACTIVO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "padre_id")
    private Ubigeo padre;

    public boolean isActivo() {
        return EstadoRegistro.ACTIVO.equals(estado);
    }

    /** Ruta desde el departamento hasta este ubigeo: [Arequipa, Caylloma, Chivay]. */
    public List<Ubigeo> getRuta() {
        List<Ubigeo> ruta = new ArrayList<>();
        for (Ubigeo actual = this; actual != null; actual = actual.getPadre()) {
            ruta.add(actual);
        }
        Collections.reverse(ruta);
        return ruta;
    }

    /** Ej.: "Chivay, Caylloma, Arequipa". */
    public String getUbicacionCompleta() {
        StringBuilder texto = new StringBuilder(nombre);
        for (Ubigeo actual = padre; actual != null; actual = actual.getPadre()) {
            texto.append(", ").append(actual.getNombre());
        }
        return texto.toString();
    }

    /** Departamento (o provincia constitucional) al que pertenece. */
    public Ubigeo getRaiz() {
        Ubigeo actual = this;
        while (actual.getPadre() != null) {
            actual = actual.getPadre();
        }
        return actual;
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

    public TipoUbigeo getTipo() {
        return tipo;
    }

    public void setTipo(TipoUbigeo tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Ubigeo getPadre() {
        return padre;
    }

    public void setPadre(Ubigeo padre) {
        this.padre = padre;
    }
}
