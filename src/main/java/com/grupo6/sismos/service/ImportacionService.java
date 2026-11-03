package com.grupo6.sismos.service;

import com.grupo6.sismos.entity.TipoUbigeo;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.importacion.CatalogoCsv;
import com.grupo6.sismos.importacion.LectorCsv;
import com.grupo6.sismos.importacion.ResultadoImportacion;
import com.grupo6.sismos.repository.UbigeoRepository;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.TipoAlertaForm;
import com.grupo6.sismos.web.form.TipoDanoForm;
import com.grupo6.sismos.web.form.UbigeoForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Importación de datos maestros desde CSV (ubigeo, tipo_alerta, tipo_dano).
 * Cada fila pasa por las mismas validaciones que el formulario: primero Bean Validation
 * sobre el Form y luego las reglas del servicio del catálogo. Las filas válidas se guardan
 * (cada una en su propia transacción) y las inválidas se informan con su motivo (HU-39, HU-40).
 */
@Service
public class ImportacionService {

    private final Validator validator;
    private final UbigeoService ubigeoService;
    private final TipoAlertaService tipoAlertaService;
    private final TipoDanoService tipoDanoService;
    private final UbigeoRepository ubigeoRepository;

    public ImportacionService(Validator validator, UbigeoService ubigeoService, TipoAlertaService tipoAlertaService,
                              TipoDanoService tipoDanoService, UbigeoRepository ubigeoRepository) {
        this.validator = validator;
        this.ubigeoService = ubigeoService;
        this.tipoAlertaService = tipoAlertaService;
        this.tipoDanoService = tipoDanoService;
        this.ubigeoRepository = ubigeoRepository;
    }

    /**
     * @throws ReglaNegocioException si el archivo completo no se puede procesar
     *                               (vacío, no es CSV o le faltan columnas)
     */
    public ResultadoImportacion importar(CatalogoCsv catalogo, String nombreArchivo, InputStream contenido)
            throws IOException {
        if (nombreArchivo == null || !nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new ReglaNegocioException("archivo", "Seleccione un archivo con extensión .csv.");
        }
        List<LectorCsv.Linea> lineas = LectorCsv.leer(contenido);
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("archivo", "El archivo está vacío.");
        }
        Map<String, Integer> indices = indicesDeEncabezado(catalogo, lineas.get(0).columnas());

        List<LectorCsv.Linea> datos = new ArrayList<>(lineas.subList(1, lineas.size()));
        if (datos.isEmpty()) {
            throw new ReglaNegocioException("archivo", "El archivo solo tiene encabezado; no hay filas para importar.");
        }
        if (catalogo == CatalogoCsv.UBIGEO) {
            // Primero los niveles superiores, para que provincias y distritos encuentren a su padre.
            datos.sort(Comparator.comparingInt((LectorCsv.Linea l) -> valor(l, indices, "codigo").length())
                    .thenComparingInt(LectorCsv.Linea::numero));
        }

        ResultadoImportacion resultado = new ResultadoImportacion(catalogo, nombreArchivo);
        Set<String> codigosDelArchivo = new HashSet<>();
        for (LectorCsv.Linea linea : datos) {
            resultado.sumarLeida();
            String codigo = valor(linea, indices, "codigo");
            if (linea.columnas().size() != lineas.get(0).columnas().size()) {
                resultado.rechazar(linea.numero(), codigo, "La fila tiene " + linea.columnas().size()
                        + " columnas y el encabezado " + lineas.get(0).columnas().size() + ".");
                continue;
            }
            if (!codigo.isEmpty() && !codigosDelArchivo.add(codigo.toUpperCase(Locale.ROOT))) {
                resultado.rechazar(linea.numero(), codigo, "El código está repetido dentro del archivo.");
                continue;
            }
            try {
                switch (catalogo) {
                    case UBIGEO -> importarUbigeo(linea, indices);
                    case TIPO_ALERTA -> importarTipoAlerta(linea, indices);
                    case TIPO_DANO -> importarTipoDano(linea, indices);
                }
                resultado.sumarImportada();
            } catch (ReglaNegocioException error) {
                resultado.rechazar(linea.numero(), codigo, error.getMessage());
            } catch (DataAccessException error) {
                resultado.rechazar(linea.numero(), codigo, "La base de datos rechazó la fila.");
            }
        }
        resultado.getRechazadas().sort(Comparator.comparingInt(f -> f.linea()));
        return resultado;
    }

    /** HU-39: el encabezado debe tener todas las columnas mínimas del catálogo (en cualquier orden). */
    private Map<String, Integer> indicesDeEncabezado(CatalogoCsv catalogo, List<String> encabezado) {
        Map<String, Integer> indices = new HashMap<>();
        for (int i = 0; i < encabezado.size(); i++) {
            indices.put(encabezado.get(i).trim().toLowerCase(Locale.ROOT), i);
        }
        List<String> faltantes = catalogo.getColumnas().stream().filter(c -> !indices.containsKey(c)).toList();
        if (!faltantes.isEmpty()) {
            throw new ReglaNegocioException("archivo", "El archivo no corresponde a " + catalogo.getNombre()
                    + ": faltan las columnas " + String.join(", ", faltantes)
                    + ". Columnas esperadas: " + String.join(", ", catalogo.getColumnas()) + ".");
        }
        return indices;
    }

    private void importarUbigeo(LectorCsv.Linea linea, Map<String, Integer> indices) {
        UbigeoForm form = new UbigeoForm();
        form.setCodigo(valor(linea, indices, "codigo"));
        form.setNombre(valor(linea, indices, "nombre"));
        form.setTipo(valor(linea, indices, "tipo").toUpperCase(Locale.ROOT));
        form.setEstado(valor(linea, indices, "estado").toUpperCase(Locale.ROOT));
        validarForm(form);

        // El padre se deduce del código: 4 dígitos → departamento (2), 6 dígitos → provincia (4).
        TipoUbigeo tipo = TipoUbigeo.desdeTexto(form.getTipo());
        if (tipo != null && !tipo.isRaiz() && form.getCodigo().length() > 2) {
            String codigoPadre = form.getCodigo().substring(0, form.getCodigo().length() - 2);
            Ubigeo padre = ubigeoRepository.findByCodigo(codigoPadre).orElseThrow(() ->
                    new ReglaNegocioException("codigo", "No existe el ubigeo " + codigoPadre + " al que pertenece."));
            form.setPadreId(padre.getId());
        }
        ubigeoService.registrar(form);
    }

    private void importarTipoAlerta(LectorCsv.Linea linea, Map<String, Integer> indices) {
        TipoAlertaForm form = new TipoAlertaForm();
        form.setCodigo(valor(linea, indices, "codigo"));
        form.setNombre(valor(linea, indices, "nombre"));
        form.setColor(valor(linea, indices, "color").toUpperCase(Locale.ROOT));
        form.setDescripcion(valor(linea, indices, "descripcion"));
        form.setEstado(valor(linea, indices, "estado").toUpperCase(Locale.ROOT));
        String prioridad = valor(linea, indices, "prioridad");
        try {
            form.setPrioridad(prioridad.isEmpty() ? null : Integer.valueOf(prioridad));
        } catch (NumberFormatException error) {
            throw new ReglaNegocioException("prioridad", "La prioridad \"" + prioridad + "\" no es un número entero.");
        }
        validarForm(form);
        tipoAlertaService.registrar(form);
    }

    private void importarTipoDano(LectorCsv.Linea linea, Map<String, Integer> indices) {
        TipoDanoForm form = new TipoDanoForm();
        form.setCodigo(valor(linea, indices, "codigo"));
        form.setNombre(valor(linea, indices, "nombre"));
        form.setSector(valor(linea, indices, "sector"));
        form.setDescripcion(valor(linea, indices, "descripcion"));
        form.setEstado(valor(linea, indices, "estado").toUpperCase(Locale.ROOT));
        validarForm(form);
        tipoDanoService.registrar(form);
    }

    /** Bean Validation sobre el Form; si falla, la fila se rechaza con todos los mensajes. */
    private void validarForm(Object form) {
        Set<ConstraintViolation<Object>> errores = validator.validate(form);
        if (!errores.isEmpty()) {
            String mensajes = errores.stream()
                    .sorted(Comparator.comparing(e -> e.getPropertyPath().toString()))
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(" "));
            throw new ReglaNegocioException(null, mensajes);
        }
    }

    private static String valor(LectorCsv.Linea linea, Map<String, Integer> indices, String columna) {
        Integer indice = indices.get(columna);
        if (indice == null || indice >= linea.columnas().size()) {
            return "";
        }
        return linea.columnas().get(indice).trim();
    }
}
