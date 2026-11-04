package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.EstadoRegistro;
import com.grupo6.sismos.entity.TipoUbigeo;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.repository.EstacionMonitoreoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.UbigeoRepository;
import com.grupo6.sismos.web.error.RecursoNoEncontradoException;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.OpcionUbigeo;
import com.grupo6.sismos.web.form.UbigeoForm;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-09 (gestionar ubigeos) y HU-21 (datos para la selección en cascada). */
@Service
public class UbigeoService {

    private final UbigeoRepository ubigeoRepository;
    private final SismoRepository sismoRepository;
    private final EstacionMonitoreoRepository estacionRepository;

    public UbigeoService(UbigeoRepository ubigeoRepository, SismoRepository sismoRepository,
                         EstacionMonitoreoRepository estacionRepository) {
        this.ubigeoRepository = ubigeoRepository;
        this.sismoRepository = sismoRepository;
        this.estacionRepository = estacionRepository;
    }

    /** Hijos de un ubigeo, o los departamentos si padreId es null. */
    @Transactional(readOnly = true)
    public List<Ubigeo> listarHijos(Long padreId) {
        if (padreId == null) {
            return ubigeoRepository.findByPadreIsNullOrderByCodigoAsc();
        }
        return ubigeoRepository.findByPadreIdOrderByCodigoAsc(padreId);
    }

    @Transactional(readOnly = true)
    public List<Ubigeo> departamentos() {
        return ubigeoRepository.findByPadreIsNullOrderByCodigoAsc();
    }

    @Transactional(readOnly = true)
    public Ubigeo buscarPorId(Long id) {
        return ubigeoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ubigeo solicitado."));
    }

    @Transactional(readOnly = true)
    public long contarHijos(Long id) {
        return ubigeoRepository.countByPadreId(id);
    }

    /** HU-21: ubigeos activos para armar los selects en cascada (departamento, provincia, distrito). */
    @Transactional(readOnly = true)
    public List<OpcionUbigeo> opcionesCascada() {
        return ubigeoRepository.findByEstadoOrderByCodigoAsc(EstadoRegistro.ACTIVO).stream()
                .filter(u -> u.getPadre() == null || u.getPadre().isActivo())
                .map(u -> new OpcionUbigeo(u.getId(), u.getCodigo(), u.getNombre(),
                        u.getPadre() == null ? null : u.getPadre().getId()))
                .toList();
    }

    /** Tipo que tendrá un ubigeo nuevo según su padre (null = departamento o provincia constitucional). */
    @Transactional(readOnly = true)
    public TipoUbigeo tipoParaHijoDe(Long padreId) {
        if (padreId == null) {
            return TipoUbigeo.DEPARTAMENTO;
        }
        return buscarPorId(padreId).getTipo().getTipoHijo();
    }

    @Transactional
    public Ubigeo registrar(UbigeoForm form) {
        TipoUbigeo tipo = TipoUbigeo.desdeTexto(form.getTipo());
        if (tipo == null) {
            throw new ReglaNegocioException("tipo",
                    "El tipo debe ser DEPARTAMENTO, PROVINCIA_CONSTITUCIONAL, PROVINCIA o DISTRITO.");
        }
        Ubigeo padre = form.getPadreId() == null ? null : ubigeoRepository.findById(form.getPadreId())
                .orElseThrow(() -> new ReglaNegocioException(null, "El ubigeo padre no existe."));
        String codigo = form.getCodigo().trim();
        validarNivel(tipo, padre);
        validarCodigo(tipo, padre, codigo);
        if (ubigeoRepository.existsByCodigo(codigo)) {
            throw new ReglaNegocioException("codigo", "El código " + codigo + " ya está registrado.");
        }
        validarNombreUnico(padre, form.getNombre().trim(), -1L);

        Ubigeo ubigeo = new Ubigeo();
        ubigeo.setCodigo(codigo);
        ubigeo.setNombre(form.getNombre().trim());
        ubigeo.setTipo(tipo);
        ubigeo.setEstado(form.getEstado());
        ubigeo.setPadre(padre);
        return ubigeoRepository.save(ubigeo);
    }

    /** Al editar se cambian nombre y estado; el código y el nivel no cambian. */
    @Transactional
    public Ubigeo actualizar(Long id, UbigeoForm form) {
        Ubigeo ubigeo = buscarPorId(id);
        validarNombreUnico(ubigeo.getPadre(), form.getNombre().trim(), ubigeo.getId());
        ubigeo.setNombre(form.getNombre().trim());
        ubigeo.setEstado(form.getEstado());
        return ubigeoRepository.save(ubigeo);
    }

    /** Null si se puede eliminar; si no, el motivo para mostrarlo en la confirmación. */
    @Transactional(readOnly = true)
    public String motivoParaNoEliminar(Ubigeo ubigeo) {
        long hijos = ubigeoRepository.countByPadreId(ubigeo.getId());
        if (hijos > 0) {
            return ubigeo.getNombre() + " tiene " + hijos + " ubigeo(s) dependiente(s). Elimine primero esos registros.";
        }
        long sismos = sismoRepository.countByUbigeoId(ubigeo.getId());
        if (sismos > 0) {
            return ubigeo.getNombre() + " tiene " + sismos + " sismo(s) registrado(s). Puede desactivarlo en lugar de eliminarlo.";
        }
        long estaciones = estacionRepository.countByUbigeoId(ubigeo.getId());
        if (estaciones > 0) {
            return ubigeo.getNombre() + " tiene " + estaciones + " estación(es) asignada(s). Puede desactivarlo en lugar de eliminarlo.";
        }
        return null;
    }

    @Transactional
    public Ubigeo eliminar(Long id) {
        Ubigeo ubigeo = buscarPorId(id);
        String motivo = motivoParaNoEliminar(ubigeo);
        if (motivo != null) {
            throw new ReglaNegocioException(null, motivo);
        }
        ubigeoRepository.delete(ubigeo);
        return ubigeo;
    }

    public UbigeoForm formDe(Ubigeo ubigeo) {
        UbigeoForm form = new UbigeoForm();
        form.setPadreId(ubigeo.getPadre() == null ? null : ubigeo.getPadre().getId());
        form.setCodigo(ubigeo.getCodigo());
        form.setNombre(ubigeo.getNombre());
        form.setTipo(ubigeo.getTipo().name());
        form.setEstado(ubigeo.getEstado());
        return form;
    }

    /** El nivel debe corresponder al padre: departamento sin padre, provincia bajo departamento, distrito bajo provincia. */
    private void validarNivel(TipoUbigeo tipo, Ubigeo padre) {
        if (padre == null && !tipo.isRaiz()) {
            throw new ReglaNegocioException("tipo", "Un registro de tipo " + tipo.getNombre().toLowerCase()
                    + " debe pertenecer a un ubigeo superior.");
        }
        if (padre != null && padre.getTipo().getTipoHijo() != tipo) {
            throw new ReglaNegocioException("tipo", padre.getNombre() + " (" + padre.getTipo().getNombre()
                    + ") no puede contener un registro de tipo " + tipo.getNombre().toLowerCase() + ".");
        }
    }

    /** INEI: 2 dígitos para departamento, 4 para provincia y 6 para distrito, empezando con el código del padre. */
    private void validarCodigo(TipoUbigeo tipo, Ubigeo padre, String codigo) {
        if (codigo.length() != tipo.getLongitudCodigo()) {
            throw new ReglaNegocioException("codigo", "El código de " + tipo.getNombre().toLowerCase()
                    + " debe tener " + tipo.getLongitudCodigo() + " dígitos.");
        }
        if (padre != null && !codigo.startsWith(padre.getCodigo())) {
            throw new ReglaNegocioException("codigo", "El código debe empezar con " + padre.getCodigo()
                    + " (código de " + padre.getNombre() + ").");
        }
    }

    private void validarNombreUnico(Ubigeo padre, String nombre, Long idActual) {
        boolean repetido = padre == null
                ? ubigeoRepository.existsByPadreIsNullAndNombreIgnoreCaseAndIdNot(nombre, idActual)
                : ubigeoRepository.existsByPadreIdAndNombreIgnoreCaseAndIdNot(padre.getId(), nombre, idActual);
        if (repetido) {
            throw new ReglaNegocioException("nombre", "Ya existe " + nombre
                    + (padre == null ? " en el primer nivel." : " dentro de " + padre.getNombre() + "."));
        }
    }
}
