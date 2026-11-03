package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.EstacionMonitoreo;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.repository.EstacionMonitoreoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.UbigeoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.EstacionForm;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-10 (gestionar estaciones) y HU-11 (sismos registrados por cada estación). */
@Service
public class EstacionService {

    private final EstacionMonitoreoRepository estacionRepository;
    private final UbigeoRepository ubigeoRepository;
    private final SismoRepository sismoRepository;

    public EstacionService(EstacionMonitoreoRepository estacionRepository, UbigeoRepository ubigeoRepository,
                           SismoRepository sismoRepository) {
        this.estacionRepository = estacionRepository;
        this.ubigeoRepository = ubigeoRepository;
        this.sismoRepository = sismoRepository;
    }

    @Transactional(readOnly = true)
    public List<EstacionMonitoreo> listar() {
        return estacionRepository.findAllByOrderByCodigoAsc();
    }

    @Transactional(readOnly = true)
    public EstacionMonitoreo buscarPorId(Long id) {
        return estacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la estación solicitada."));
    }

    /** HU-11: id de estación → cantidad de sismos que registró. */
    @Transactional(readOnly = true)
    public Map<Long, Long> contarSismosPorEstacion() {
        Map<Long, Long> totales = new HashMap<>();
        for (Object[] fila : sismoRepository.contarPorEstacion()) {
            totales.put((Long) fila[0], (Long) fila[1]);
        }
        return totales;
    }

    @Transactional
    public EstacionMonitoreo registrar(EstacionForm form) {
        String codigo = form.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (estacionRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado.");
        }
        EstacionMonitoreo estacion = new EstacionMonitoreo();
        estacion.setCodigo(codigo);
        aplicarDatos(estacion, form);
        return estacionRepository.save(estacion);
    }

    @Transactional
    public EstacionMonitoreo actualizar(Long id, EstacionForm form) {
        EstacionMonitoreo estacion = buscarPorId(id);
        String codigo = form.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (estacionRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado en otra estación.");
        }
        estacion.setCodigo(codigo);
        aplicarDatos(estacion, form);
        return estacionRepository.save(estacion);
    }

    public EstacionForm formDe(EstacionMonitoreo estacion) {
        EstacionForm form = new EstacionForm();
        form.setCodigo(estacion.getCodigo());
        form.setNombre(estacion.getNombre());
        form.setUbigeoId(estacion.getUbigeo().getId());
        form.setLatitud(estacion.getLatitud());
        form.setLongitud(estacion.getLongitud());
        form.setFechaInstalacion(estacion.getFechaInstalacion());
        form.setEstado(estacion.getEstado());
        return form;
    }

    private void aplicarDatos(EstacionMonitoreo estacion, EstacionForm form) {
        Ubigeo ubigeo = ubigeoRepository.findById(form.getUbigeoId())
                .orElseThrow(() -> new ReglaNegocioException("ubigeoId", "El ubigeo seleccionado no existe."));
        boolean mismoUbigeo = estacion.getUbigeo() != null && estacion.getUbigeo().getId().equals(ubigeo.getId());
        if (!ubigeo.isActivo() && !mismoUbigeo) {
            throw new ReglaNegocioException("ubigeoId", "El ubigeo " + ubigeo.getNombre() + " está inactivo.");
        }
        estacion.setNombre(form.getNombre().trim());
        estacion.setUbigeo(ubigeo);
        estacion.setLatitud(form.getLatitud());
        estacion.setLongitud(form.getLongitud());
        estacion.setFechaInstalacion(form.getFechaInstalacion());
        estacion.setEstado(form.getEstado());
    }
}
