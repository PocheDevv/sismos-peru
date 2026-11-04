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
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteDanoService {

    public static final int TAMANIO_PAGINA = 10;

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

    @Transactional(readOnly = true)
    public ReporteDano buscarReporte(Long sismoId, Long id) {
        return reporteDanoRepository.findById(id)
                .filter(reporte -> reporte.getSismo().getId().equals(sismoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el reporte de afectación solicitado."));
    }

    @Transactional(readOnly = true)
    public List<ReporteDano> listarPorSismo(Long sismoId) {
        return reporteDanoRepository.findBySismoIdOrderByFechaReporteDesc(sismoId);
    }

    @Transactional(readOnly = true)
    public Long totalAfectadosPorSismo(Long sismoId) {
        Long total = reporteDanoRepository.sumarCantidadAfectadaPorSismo(sismoId);
        return total == null ? 0L : total;
    }

    @Transactional(readOnly = true)
    public Page<ReporteDano> listar(Integer pagina) {
        int numero = pagina == null || pagina < 1 ? 1 : pagina;
        Page<ReporteDano> resultado = reporteDanoRepository.findAll(paginaDeReportes(numero));
        if (resultado.getTotalPages() > 0 && numero > resultado.getTotalPages()) {
            resultado = reporteDanoRepository.findAll(paginaDeReportes(resultado.getTotalPages()));
        }
        return resultado;
    }

    private PageRequest paginaDeReportes(int numero) {
        return PageRequest.of(numero - 1, TAMANIO_PAGINA, Sort.by(Sort.Direction.DESC, "fechaReporte", "id"));
    }

    public String motivoParaNoReportar(Sismo sismo) {
        if (Sismo.ESTADO_CERRADO.equals(sismo.getEstado())) {
            return "El sismo " + sismo.getCodigo() + " está Cerrado y no admite nuevos reportes de afectación.";
        }
        return null;
    }

    public String motivoParaNoEditar(ReporteDano reporte) {
        if (reporte.isCerrado()) {
            return "El reporte está CERRADO y ya no se puede modificar.";
        }
        return null;
    }

    public String errorFechaReporte(Sismo sismo, LocalDateTime fechaReporte) {
        if (fechaReporte != null && fechaReporte.isBefore(sismo.getFechaHora())) {
            return "La fecha del reporte no puede ser anterior a la ocurrencia del sismo.";
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

    @Transactional
    public ReporteDano actualizar(Long sismoId, Long id, ReporteDanoForm form) {
        ReporteDano reporte = buscarReporte(sismoId, id);
        String motivo = motivoParaNoEditar(reporte);
        if (motivo != null) {
            throw new ReglaNegocioException(null, motivo);
        }
        aplicarDatos(reporte, form);
        return reporteDanoRepository.save(reporte);
    }

    public ReporteDanoForm formDe(ReporteDano reporte) {
        ReporteDanoForm form = new ReporteDanoForm();
        form.setTipoDanoId(reporte.getTipoDano().getId());
        form.setFechaReporte(reporte.getFechaReporte());
        form.setCantidadAfectada(reporte.getCantidadAfectada());
        form.setDescripcion(reporte.getDescripcion());
        form.setNivel(reporte.getNivel());
        return form;
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
        String errorFecha = errorFechaReporte(reporte.getSismo(), form.getFechaReporte());
        if (errorFecha != null) {
            throw new ReglaNegocioException("fechaReporte", errorFecha);
        }
        reporte.setTipoDano(tipo);
        reporte.setFechaReporte(form.getFechaReporte());
        reporte.setCantidadAfectada(form.getCantidadAfectada());
        reporte.setDescripcion(form.getDescripcion().trim());
        reporte.setNivel(form.getNivel());
    }
}
