package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.entity.TipoDano;
import com.grupo6.sismos.repository.ReporteDanoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.TipoDanoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.ReporteDanoForm;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteDanoService {

    private final ReporteDanoRepository reporteDanoRepository;
    private final SismoRepository sismoRepository;
    private final TipoDanoRepository tipoDanoRepository;

    public ReporteDanoService(ReporteDanoRepository reporteDanoRepository, SismoRepository sismoRepository,
                              TipoDanoRepository tipoDanoRepository) {
        this.reporteDanoRepository = reporteDanoRepository;
        this.sismoRepository = sismoRepository;
        this.tipoDanoRepository = tipoDanoRepository;
    }

    @Transactional(readOnly = true)
    public Sismo buscarSismo(Long sismoId) {
        return sismoRepository.findById(sismoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el sismo solicitado."));
    }

    public String motivoParaNoReportar(Sismo sismo) {
        if (Sismo.ESTADO_CERRADO.equals(sismo.getEstado())) {
            return "El sismo " + sismo.getCodigo() + " está Cerrado y no admite nuevos reportes de afectación.";
        }
        return null;
    }

    public ReporteDanoForm formNuevo() {
        ReporteDanoForm form = new ReporteDanoForm();
        form.setFechaReporte(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        return form;
    }

    @Transactional
    public ReporteDano registrar(Long sismoId, ReporteDanoForm form) {
        Sismo sismo = buscarSismo(sismoId);
        String motivo = motivoParaNoReportar(sismo);
        if (motivo != null) {
            throw new ReglaNegocioException(null, motivo);
        }
        ReporteDano reporte = new ReporteDano();
        reporte.setSismo(sismo);
        reporte.setEstado(ReporteDano.ESTADO_PENDIENTE);
        aplicarDatos(reporte, form);
        return reporteDanoRepository.save(reporte);
    }

    private void aplicarDatos(ReporteDano reporte, ReporteDanoForm form) {
        TipoDano tipo = tipoDanoRepository.findById(form.getTipoDanoId())
                .orElseThrow(() -> new ReglaNegocioException("tipoDanoId", "El tipo de daño seleccionado no existe."));
        boolean mismoTipo = reporte.getTipoDano() != null && reporte.getTipoDano().getId().equals(tipo.getId());
        if (!tipo.isActivo() && !mismoTipo) {
            throw new ReglaNegocioException("tipoDanoId", "El tipo de daño " + tipo.getNombre()
                    + " está inactivo y no se puede usar en reportes nuevos.");
        }
        if (!ReporteDano.NIVELES.contains(form.getNivel())) {
            throw new ReglaNegocioException("nivel", "Seleccione un nivel de afectación válido.");
        }
        if (form.getFechaReporte().isBefore(reporte.getSismo().getFechaHora())) {
            throw new ReglaNegocioException("fechaReporte",
                    "La fecha del reporte no puede ser anterior a la ocurrencia del sismo.");
        }
        reporte.setTipoDano(tipo);
        reporte.setFechaReporte(form.getFechaReporte());
        reporte.setCantidadAfectada(form.getCantidadAfectada());
        reporte.setDescripcion(form.getDescripcion().trim());
        reporte.setNivel(form.getNivel());
    }
}
