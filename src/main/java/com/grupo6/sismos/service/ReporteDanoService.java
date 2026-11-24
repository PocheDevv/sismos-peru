package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.entity.TipoDano;
import com.grupo6.sismos.repository.ReporteDanoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.TipoDanoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.ReporteDanoFiltro;
import com.grupo6.sismos.web.form.ReporteDanoForm;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteDanoService {

    public static final int TAMANIO_PAGINA = 10;
    private static final Map<String, String> PROPIEDADES_ORDEN = Map.of(
            "fecha", "fechaReporte",
            "sismo", "sismo.codigo",
            "ubicacion", "sismo.ubigeo.codigo",
            "tipo", "tipoDano.nombre",
            "cantidad", "cantidadAfectada",
            "nivel", "nivel",
            "estado", "estado");

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
    public Page<ReporteDano> listar(ReporteDanoFiltro filtro) {
        Long tipoDanoId = validarTipoDano(filtro);
        validarNivel(filtro);
        validarEstado(filtro);
        validarPaginacion(filtro);

        int numero = filtro.getPaginaValida();
        Page<ReporteDano> resultado = buscarReportes(tipoDanoId, filtro, numero);
        if (filtro.isTieneFiltros()
                && (resultado.getTotalPages() == 0 || numero > resultado.getTotalPages())) {
            filtro.setPagina("1");
            resultado = buscarReportes(tipoDanoId, filtro, 1);
        } else if (!filtro.isTieneFiltros() && resultado.getTotalPages() > 0
                && numero > resultado.getTotalPages()) {
            int ultimaPagina = resultado.getTotalPages();
            filtro.setPagina(Integer.toString(ultimaPagina));
            resultado = buscarReportes(null, filtro, ultimaPagina);
        }
        return resultado;
    }

    private Long validarTipoDano(ReporteDanoFiltro filtro) {
        String valor = filtro.getTipoDanoId();
        if (valor == null || valor.isBlank()) {
            filtro.setTipoDanoId(null);
            return null;
        }
        try {
            Long id = Long.valueOf(valor.trim());
            if (id > 0 && tipoDanoRepository.existsById(id)) {
                filtro.setTipoDanoId(id.toString());
                return id;
            }
        } catch (NumberFormatException error) {
            filtro.getAvisos().add("El tipo de daño elegido no es válido; no se aplicó ese filtro.");
            filtro.setTipoDanoId(null);
            return null;
        }
        filtro.getAvisos().add("El tipo de daño elegido no es válido; no se aplicó ese filtro.");
        filtro.setTipoDanoId(null);
        return null;
    }

    private void validarNivel(ReporteDanoFiltro filtro) {
        String nivel = normalizar(filtro.getNivel());
        if (nivel == null) {
            filtro.setNivel(null);
        } else if (ReporteDano.NIVELES.contains(nivel)) {
            filtro.setNivel(nivel);
        } else {
            filtro.getAvisos().add("El nivel elegido no es válido; no se aplicó ese filtro.");
            filtro.setNivel(null);
        }
    }

    private void validarEstado(ReporteDanoFiltro filtro) {
        String estado = normalizar(filtro.getEstado());
        if (estado == null) {
            filtro.setEstado(null);
        } else if (ReporteDano.ESTADOS.contains(estado)) {
            filtro.setEstado(estado);
        } else {
            filtro.getAvisos().add("El estado elegido no es válido; no se aplicó ese filtro.");
            filtro.setEstado(null);
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase(Locale.ROOT);
    }

    private void validarPaginacion(ReporteDanoFiltro filtro) {
        String tamanio = filtro.getTamanio();
        if (tamanio != null && !tamanio.isBlank()) {
            try {
                if (!ReporteDanoFiltro.TAMANIOS_PAGINA.contains(Integer.parseInt(tamanio))) {
                    filtro.getAvisos().add("El tamaño de página no es válido; se usaron 10 registros.");
                }
            } catch (NumberFormatException error) {
                filtro.getAvisos().add("El tamaño de página no es válido; se usaron 10 registros.");
            }
        }
        filtro.setTamanio(Integer.toString(filtro.getTamanioValido()));

        if (filtro.getOrden() != null && !filtro.getOrden().isBlank()
                && !ReporteDanoFiltro.ORDENES.contains(filtro.getOrden())) {
            filtro.getAvisos().add("El campo de ordenamiento no es válido; se usó la fecha del reporte.");
        }
        filtro.setOrden(filtro.getOrdenValido());

        if (filtro.getDir() != null && !filtro.getDir().isBlank()
                && !"asc".equalsIgnoreCase(filtro.getDir()) && !"desc".equalsIgnoreCase(filtro.getDir())) {
            filtro.getAvisos().add("La dirección de ordenamiento no es válida; se usó el orden predeterminado.");
            filtro.setDir(null);
        }
        filtro.setDir(filtro.isDescendente() ? "desc" : "asc");
    }

    private Page<ReporteDano> buscarReportes(Long tipoDanoId, ReporteDanoFiltro filtro, int numero) {
        return reporteDanoRepository.buscarPorFiltros(tipoDanoId, filtro.getNivel(), filtro.getEstado(),
                paginaDeReportes(numero, filtro));
    }

    private PageRequest paginaDeReportes(int numero, ReporteDanoFiltro filtro) {
        Sort.Direction direccion = filtro.isDescendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort orden = Sort.by(direccion, PROPIEDADES_ORDEN.get(filtro.getOrdenValido()))
                .and(Sort.by(direccion, "id"));
        return PageRequest.of(numero - 1, filtro.getTamanioValido(), orden);
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

    /**
     * HU-26: el estado de un reporte solo avanza un paso: PENDIENTE → VERIFICADO → CERRADO.
     * Un reporte CERRADO ya no cambia.
     */
    @Transactional
    public ReporteDano actualizarEstado(Long sismoId, Long id, String nuevoEstado) {
        ReporteDano reporte = buscarReporte(sismoId, id);
        if (nuevoEstado == null || !ReporteDano.ESTADOS.contains(nuevoEstado)) {
            throw new ReglaNegocioException(null, "El estado seleccionado no es válido.");
        }
        if (reporte.isCerrado()) {
            throw new ReglaNegocioException(null, "El reporte está CERRADO y su estado ya no se puede cambiar.");
        }
        if (nuevoEstado.equals(reporte.getEstado())) {
            throw new ReglaNegocioException(null, "El reporte ya se encuentra en estado " + nuevoEstado + ".");
        }
        if (!nuevoEstado.equals(reporte.getSiguienteEstado())) {
            throw new ReglaNegocioException(null, "Un reporte " + reporte.getEstado() + " solo puede pasar a "
                    + reporte.getSiguienteEstado() + ".");
        }
        reporte.setEstado(nuevoEstado);
        return reporteDanoRepository.save(reporte);
    }
}
