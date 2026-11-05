package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.EstacionMonitoreo;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.entity.TipoAlerta;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.repository.EstacionMonitoreoRepository;
import com.grupo6.sismos.repository.ReporteDanoRepository;
import com.grupo6.sismos.repository.SismoEspecificaciones;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.TipoAlertaRepository;
import com.grupo6.sismos.repository.UbigeoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.SismoFiltro;
import com.grupo6.sismos.web.form.SismoForm;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de sismos: HU-01 a HU-08 (CRUD, validaciones y estado), HU-11 (estación),
 * HU-15, HU-30 y HU-31 (búsqueda), y las reglas de la PA3 (ubigeo y alerta activos).
 */
@Service
public class SismoService {

    /** Columna de la lista (parámetro "orden") → propiedad de la entidad. */
    private static final Map<String, String> PROPIEDADES_ORDEN = Map.of(
            "codigo", "codigo",
            "fecha", "fechaHora",
            "magnitud", "magnitud",
            "profundidad", "profundidad",
            "ubicacion", "ubigeo.codigo",
            "alerta", "tipoAlerta.prioridad",
            "estado", "estado");

    private final SismoRepository sismoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final TipoAlertaRepository tipoAlertaRepository;
    private final EstacionMonitoreoRepository estacionRepository;
    private final ReporteDanoRepository reporteDanoRepository;

    public SismoService(SismoRepository sismoRepository, UbigeoRepository ubigeoRepository,
                        TipoAlertaRepository tipoAlertaRepository, EstacionMonitoreoRepository estacionRepository,
                        ReporteDanoRepository reporteDanoRepository) {
        this.sismoRepository = sismoRepository;
        this.ubigeoRepository = ubigeoRepository;
        this.tipoAlertaRepository = tipoAlertaRepository;
        this.estacionRepository = estacionRepository;
        this.reporteDanoRepository = reporteDanoRepository;
    }

    /** HU-15: filtra, ordena y pagina. Los filtros con rangos invertidos no se aplican y quedan como aviso. */
    @Transactional(readOnly = true)
    public Page<Sismo> buscar(SismoFiltro filtro) {
        if (filtro.getMagnitudMin() != null && filtro.getMagnitudMax() != null
                && filtro.getMagnitudMin() > filtro.getMagnitudMax()) {
            filtro.getAvisos().add("La magnitud mínima no puede ser mayor que la máxima; no se aplicó ese filtro.");
            filtro.setMagnitudMin(null);
            filtro.setMagnitudMax(null);
        }
        if (filtro.getDesde() != null && filtro.getHasta() != null && filtro.getDesde().isAfter(filtro.getHasta())) {
            filtro.getAvisos().add("La fecha inicial no puede ser posterior a la final; no se aplicó ese filtro.");
            filtro.setDesde(null);
            filtro.setHasta(null);
        }
        if (filtro.getEstado() != null && !filtro.getEstado().isBlank() && !Sismo.ESTADOS.contains(filtro.getEstado())) {
            filtro.getAvisos().add("El estado elegido no es válido; no se aplicó ese filtro.");
            filtro.setEstado(null);
        }
        String codigoDepartamento = null;
        if (filtro.getDepartamentoId() != null) {
            codigoDepartamento = ubigeoRepository.findById(filtro.getDepartamentoId())
                    .map(Ubigeo::getCodigo).orElse(null);
        }

        Sort.Direction direccion = filtro.isDescendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort orden = Sort.by(direccion, PROPIEDADES_ORDEN.get(filtro.getOrdenValido())).and(Sort.by(direccion, "id"));
        PageRequest pagina = PageRequest.of(filtro.getPaginaValida() - 1, SismoFiltro.TAMANIO_PAGINA, orden);

        Page<Sismo> resultado = sismoRepository.findAll(SismoEspecificaciones.segun(filtro, codigoDepartamento), pagina);
        if (resultado.getTotalPages() > 0 && filtro.getPaginaValida() > resultado.getTotalPages()) {
            filtro.setPagina(resultado.getTotalPages());
            pagina = PageRequest.of(resultado.getTotalPages() - 1, SismoFiltro.TAMANIO_PAGINA, orden);
            resultado = sismoRepository.findAll(SismoEspecificaciones.segun(filtro, codigoDepartamento), pagina);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Sismo buscarPorId(Long id) {
        return sismoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el sismo solicitado."));
    }

    @Transactional(readOnly = true)
    public long contarReportes(Long sismoId) {
        return reporteDanoRepository.countBySismoId(sismoId);
    }

    /** HU-02: registrar. El estado inicial siempre es Registrado (HU-08). */
    @Transactional
    public Sismo registrar(SismoForm form) {
        String codigo = form.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (sismoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado.");
        }
        Sismo sismo = new Sismo();
        sismo.setCodigo(codigo);
        sismo.setEstado(Sismo.ESTADO_REGISTRADO);
        aplicarDatos(sismo, form);
        return sismoRepository.save(sismo);
    }

    /** HU-06: editar con las mismas validaciones; HU-08: cambio de estado. */
    @Transactional
    public Sismo actualizar(Long id, SismoForm form) {
        Sismo sismo = buscarPorId(id);
        String codigo = form.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (sismoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado en otro sismo.");
        }
        if (form.getEstado() == null || !Sismo.ESTADOS.contains(form.getEstado())) {
            throw new ReglaNegocioException("estado", "Seleccione un estado válido.");
        }
        sismo.setCodigo(codigo);
        sismo.setEstado(form.getEstado());
        aplicarDatos(sismo, form);
        return sismoRepository.save(sismo);
    }

    /**
     * HU-29: marca o desmarca un sismo como relevante para seguimiento.
     * Al marcarlo pasa a En seguimiento; al desmarcarlo, si estaba En seguimiento, vuelve a En evaluación.
     */
    @Transactional
    public Sismo cambiarRelevancia(Long id, boolean relevante) {
        Sismo sismo = buscarPorId(id);
        if (relevante) {
            if (sismo.isRelevante()) {
                throw new ReglaNegocioException(null, "El sismo " + sismo.getCodigo() + " ya está marcado como relevante.");
            }
            if (Sismo.ESTADO_CERRADO.equals(sismo.getEstado())) {
                throw new ReglaNegocioException(null, "El sismo " + sismo.getCodigo()
                        + " está Cerrado y no se puede marcar como relevante.");
            }
            sismo.setRelevante(true);
            sismo.setEstado(Sismo.ESTADO_EN_SEGUIMIENTO);
        } else {
            if (!sismo.isRelevante()) {
                throw new ReglaNegocioException(null, "El sismo " + sismo.getCodigo() + " no está marcado como relevante.");
            }
            sismo.setRelevante(false);
            if (sismo.isEnSeguimiento()) {
                sismo.setEstado(Sismo.ESTADO_EN_EVALUACION);
            }
        }
        return sismoRepository.save(sismo);
    }

    /** HU-07: un sismo En seguimiento no se puede eliminar. Null si se puede. */
    public String motivoParaNoEliminar(Sismo sismo) {
        if (sismo.isEnSeguimiento()) {
            return "El sismo " + sismo.getCodigo() + " está En seguimiento y no se puede eliminar. "
                    + "Cambie primero su estado.";
        }
        return null;
    }

    /** HU-07: elimina el sismo; sus reportes de daño y seguimientos se eliminan en cascada (ON DELETE CASCADE). */
    @Transactional
    public Sismo eliminar(Long id) {
        Sismo sismo = buscarPorId(id);
        String motivo = motivoParaNoEliminar(sismo);
        if (motivo != null) {
            throw new ReglaNegocioException(null, motivo);
        }
        sismoRepository.delete(sismo);
        return sismo;
    }

    public SismoForm formDe(Sismo sismo) {
        SismoForm form = new SismoForm();
        form.setCodigo(sismo.getCodigo());
        form.setFechaHora(sismo.getFechaHora());
        form.setMagnitud(sismo.getMagnitud());
        form.setProfundidad(sismo.getProfundidad());
        form.setLatitud(sismo.getLatitud());
        form.setLongitud(sismo.getLongitud());
        form.setUbigeoId(sismo.getUbigeo().getId());
        form.setTipoAlertaId(sismo.getTipoAlerta().getId());
        form.setEstacionId(sismo.getEstacion() == null ? null : sismo.getEstacion().getId());
        form.setIntensidad(sismo.getIntensidad());
        form.setReferencia(sismo.getReferencia());
        form.setEstado(sismo.getEstado());
        return form;
    }

    /**
     * Reglas de negocio comunes a registrar y editar. Al editar se permite conservar un ubigeo,
     * alerta o estación que se desactivó después; lo que no se permite es elegir uno inactivo nuevo.
     */
    private void aplicarDatos(Sismo sismo, SismoForm form) {
        Ubigeo ubigeo = ubigeoRepository.findById(form.getUbigeoId())
                .orElseThrow(() -> new ReglaNegocioException("ubigeoId", "El ubigeo seleccionado no existe."));
        boolean mismoUbigeo = sismo.getUbigeo() != null && sismo.getUbigeo().getId().equals(ubigeo.getId());
        if (!ubigeo.isActivo() && !mismoUbigeo) {
            throw new ReglaNegocioException("ubigeoId", "El ubigeo " + ubigeo.getNombre()
                    + " está inactivo. Cada sismo debe registrarse en un ubigeo activo.");
        }

        TipoAlerta alerta = tipoAlertaRepository.findById(form.getTipoAlertaId())
                .orElseThrow(() -> new ReglaNegocioException("tipoAlertaId", "El tipo de alerta seleccionado no existe."));
        boolean mismaAlerta = sismo.getTipoAlerta() != null && sismo.getTipoAlerta().getId().equals(alerta.getId());
        if (!alerta.isActivo() && !mismaAlerta) {
            throw new ReglaNegocioException("tipoAlertaId", "La alerta " + alerta.getNombre()
                    + " está inactiva. Cada sismo debe tener una clasificación de alerta activa.");
        }

        EstacionMonitoreo estacion = null;
        if (form.getEstacionId() != null) {
            estacion = estacionRepository.findById(form.getEstacionId())
                    .orElseThrow(() -> new ReglaNegocioException("estacionId", "La estación seleccionada no existe."));
            boolean mismaEstacion = sismo.getEstacion() != null
                    && Objects.equals(sismo.getEstacion().getId(), estacion.getId());
            if (!estacion.isActiva() && !mismaEstacion) {
                throw new ReglaNegocioException("estacionId", "La estación " + estacion.getCodigo()
                        + " está inactiva y no puede recibir nuevos registros.");
            }
        }

        String intensidad = form.getIntensidad() == null || form.getIntensidad().isBlank() ? null : form.getIntensidad();
        if (intensidad != null && !Sismo.INTENSIDADES.contains(intensidad)) {
            throw new ReglaNegocioException("intensidad", "La intensidad seleccionada no es válida.");
        }

        sismo.setFechaHora(form.getFechaHora());
        sismo.setMagnitud(form.getMagnitud());
        sismo.setProfundidad(form.getProfundidad());
        sismo.setLatitud(form.getLatitud());
        sismo.setLongitud(form.getLongitud());
        sismo.setUbigeo(ubigeo);
        sismo.setTipoAlerta(alerta);
        sismo.setEstacion(estacion);
        sismo.setIntensidad(intensidad);
        sismo.setReferencia(form.getReferencia() == null || form.getReferencia().isBlank()
                ? null : form.getReferencia().trim());
    }
}
