package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.EstadoRegistro;
import com.grupo6.sismos.entity.TipoDano;
import com.grupo6.sismos.repository.ReporteDanoRepository;
import com.grupo6.sismos.repository.TipoDanoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.TipoDanoForm;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogo de tipos de daño (también lo usa la importación de tipo_dano.csv). */
@Service
public class TipoDanoService {

    private final TipoDanoRepository tipoDanoRepository;
    private final ReporteDanoRepository reporteDanoRepository;

    public TipoDanoService(TipoDanoRepository tipoDanoRepository, ReporteDanoRepository reporteDanoRepository) {
        this.tipoDanoRepository = tipoDanoRepository;
        this.reporteDanoRepository = reporteDanoRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoDano> listar() {
        return tipoDanoRepository.findAllByOrderByCodigoAsc();
    }

    /** Para el formulario de reporte de daño (sprint 3): solo tipos activos. */
    @Transactional(readOnly = true)
    public List<TipoDano> activos() {
        return tipoDanoRepository.findByEstadoOrderByNombreAsc(EstadoRegistro.ACTIVO);
    }

    @Transactional(readOnly = true)
    public TipoDano buscarPorId(Long id) {
        return tipoDanoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de daño solicitado."));
    }

    @Transactional(readOnly = true)
    public long contarReportes(Long id) {
        return reporteDanoRepository.countByTipoDanoId(id);
    }

    @Transactional
    public TipoDano registrar(TipoDanoForm form) {
        String codigo = form.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (tipoDanoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado.");
        }
        if (!EstadoRegistro.esValido(form.getEstado())) {
            throw new ReglaNegocioException("estado", "El estado debe ser ACTIVO o INACTIVO.");
        }
        TipoDano tipo = new TipoDano();
        tipo.setCodigo(codigo);
        tipo.setNombre(form.getNombre().trim());
        tipo.setSector(form.getSector().trim().toUpperCase(Locale.ROOT));
        tipo.setDescripcion(form.getDescripcion() == null || form.getDescripcion().isBlank()
                ? null : form.getDescripcion().trim());
        tipo.setEstado(form.getEstado());
        return tipoDanoRepository.save(tipo);
    }

    /** Activa o desactiva. Un tipo inactivo no se puede elegir en reportes nuevos. */
    @Transactional
    public TipoDano cambiarEstado(Long id) {
        TipoDano tipo = buscarPorId(id);
        tipo.setEstado(tipo.isActivo() ? EstadoRegistro.INACTIVO : EstadoRegistro.ACTIVO);
        return tipoDanoRepository.save(tipo);
    }
}
