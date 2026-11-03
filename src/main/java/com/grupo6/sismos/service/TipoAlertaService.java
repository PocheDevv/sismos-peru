package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.EstadoRegistro;
import com.grupo6.sismos.entity.TipoAlerta;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.TipoAlertaRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.TipoAlertaForm;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogo de tipos de alerta (también lo usa la importación de tipo_alerta.csv). */
@Service
public class TipoAlertaService {

    private final TipoAlertaRepository tipoAlertaRepository;
    private final SismoRepository sismoRepository;

    public TipoAlertaService(TipoAlertaRepository tipoAlertaRepository, SismoRepository sismoRepository) {
        this.tipoAlertaRepository = tipoAlertaRepository;
        this.sismoRepository = sismoRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoAlerta> listar() {
        return tipoAlertaRepository.findAllByOrderByPrioridadAscCodigoAsc();
    }

    @Transactional(readOnly = true)
    public List<TipoAlerta> activos() {
        return tipoAlertaRepository.findByEstadoOrderByPrioridadAsc(EstadoRegistro.ACTIVO);
    }

    @Transactional(readOnly = true)
    public TipoAlerta buscarPorId(Long id) {
        return tipoAlertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de alerta solicitado."));
    }

    @Transactional(readOnly = true)
    public long contarSismos(Long id) {
        return sismoRepository.countByTipoAlertaId(id);
    }

    @Transactional
    public TipoAlerta registrar(TipoAlertaForm form) {
        String codigo = form.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (tipoAlertaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado.");
        }
        if (form.getColor() == null || !TipoAlerta.COLORES.contains(form.getColor())) {
            throw new ReglaNegocioException("color", "El color debe ser VERDE, AMARILLO, NARANJA o ROJO.");
        }
        if (!EstadoRegistro.esValido(form.getEstado())) {
            throw new ReglaNegocioException("estado", "El estado debe ser ACTIVO o INACTIVO.");
        }
        TipoAlerta tipo = new TipoAlerta();
        tipo.setCodigo(codigo);
        tipo.setNombre(form.getNombre().trim());
        tipo.setColor(form.getColor());
        tipo.setPrioridad(form.getPrioridad());
        tipo.setDescripcion(textoOpcional(form.getDescripcion()));
        tipo.setEstado(form.getEstado());
        return tipoAlertaRepository.save(tipo);
    }

    /** Activa o desactiva. Un tipo inactivo no se puede elegir en sismos nuevos. */
    @Transactional
    public TipoAlerta cambiarEstado(Long id) {
        TipoAlerta tipo = buscarPorId(id);
        tipo.setEstado(tipo.isActivo() ? EstadoRegistro.INACTIVO : EstadoRegistro.ACTIVO);
        return tipoAlertaRepository.save(tipo);
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
