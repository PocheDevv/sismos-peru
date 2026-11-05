package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.SeguimientoSismo;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.repository.SeguimientoSismoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.SeguimientoForm;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-14: bitácora de seguimiento de un sismo relevante (Sismo 1 ── N SeguimientoSismo). */
@Service
public class SeguimientoService {

    private final SeguimientoSismoRepository seguimientoRepository;
    private final SismoRepository sismoRepository;

    public SeguimientoService(SeguimientoSismoRepository seguimientoRepository, SismoRepository sismoRepository) {
        this.seguimientoRepository = seguimientoRepository;
        this.sismoRepository = sismoRepository;
    }

    @Transactional(readOnly = true)
    public Sismo buscarSismo(Long sismoId) {
        return sismoRepository.findById(sismoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el sismo solicitado."));
    }

    @Transactional(readOnly = true)
    public List<SeguimientoSismo> listarPorSismo(Long sismoId) {
        return seguimientoRepository.findBySismoIdOrderByFechaActualizacionDesc(sismoId);
    }

    /** Null si el sismo admite seguimiento; si no, el motivo. */
    public String motivoParaNoRegistrar(Sismo sismo) {
        if (!sismo.isRelevante()) {
            return "El sismo " + sismo.getCodigo() + " no está marcado como relevante. "
                    + "Márquelo primero para registrar su seguimiento.";
        }
        if (Sismo.ESTADO_CERRADO.equals(sismo.getEstado())) {
            return "El sismo " + sismo.getCodigo() + " está Cerrado y no admite nuevos seguimientos.";
        }
        return null;
    }

    /** Null si la fecha es válida para el sismo; si no, el mensaje de error. */
    public String errorFecha(Sismo sismo, LocalDateTime fecha) {
        if (fecha != null && fecha.isBefore(sismo.getFechaHora())) {
            return "La fecha del seguimiento no puede ser anterior a la ocurrencia del sismo.";
        }
        return null;
    }

    public SeguimientoForm formNuevo() {
        SeguimientoForm form = new SeguimientoForm();
        form.setFechaActualizacion(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        return form;
    }

    @Transactional
    public SeguimientoSismo registrar(Long sismoId, SeguimientoForm form) {
        Sismo sismo = buscarSismo(sismoId);
        String motivo = motivoParaNoRegistrar(sismo);
        if (motivo != null) {
            throw new ReglaNegocioException(null, motivo);
        }
        String error = errorFecha(sismo, form.getFechaActualizacion());
        if (error != null) {
            throw new ReglaNegocioException("fechaActualizacion", error);
        }
        SeguimientoSismo seguimiento = new SeguimientoSismo();
        seguimiento.setSismo(sismo);
        seguimiento.setFechaActualizacion(form.getFechaActualizacion());
        seguimiento.setResponsable(form.getResponsable().trim());
        seguimiento.setObservaciones(form.getObservaciones().trim());
        seguimiento.setAccionesRealizadas(form.getAccionesRealizadas().trim());
        return seguimientoRepository.save(seguimiento);
    }
}
