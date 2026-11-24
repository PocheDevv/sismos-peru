package com.grupo6.sismos.service;

import com.grupo6.sismos.dto.reporte.AfectadosPorTipoDto;
import com.grupo6.sismos.dto.reporte.IndicadoresDashboardDto;
import com.grupo6.sismos.dto.reporte.SismosPorMesDto;
import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.repository.ReporteDanoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.UbigeoRepository;
import com.grupo6.sismos.web.form.DashboardFiltro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-PE");

    private final SismoRepository sismoRepository;
    private final ReporteDanoRepository reporteDanoRepository;
    private final UbigeoRepository ubigeoRepository;

    public DashboardService(SismoRepository sismoRepository, ReporteDanoRepository reporteDanoRepository,
                            UbigeoRepository ubigeoRepository) {
        this.sismoRepository = sismoRepository;
        this.reporteDanoRepository = reporteDanoRepository;
        this.ubigeoRepository = ubigeoRepository;
    }

    @Transactional(readOnly = true)
    public IndicadoresDashboardDto indicadores(DashboardFiltro filtro) {
        aplicarPeriodo(filtro);
        String departamento = codigoDepartamento(filtro);
        LocalDateTime desde = filtro.getDesde().atStartOfDay();
        LocalDateTime hasta = filtro.getHasta().plusDays(1).atStartOfDay();
        return new IndicadoresDashboardDto(
                sismoRepository.contarEnPeriodo(desde, hasta, departamento),
                sismoRepository.magnitudMaximaEnPeriodo(desde, hasta, departamento),
                reporteDanoRepository.sumarAfectadosEnPeriodo(desde, hasta, departamento),
                reporteDanoRepository.contarPorEstadoEnPeriodo(ReporteDano.ESTADO_PENDIENTE, desde, hasta, departamento));
    }

    @Transactional(readOnly = true)
    public List<SismosPorMesDto> sismosPorMes(DashboardFiltro filtro) {
        aplicarPeriodo(filtro);
        Map<YearMonth, Long> cantidades = new HashMap<>();
        for (SismosPorMesDto fila : sismoRepository.contarPorMes(filtro.getDesde().atStartOfDay(),
                filtro.getHasta().plusDays(1).atStartOfDay(), codigoDepartamento(filtro))) {
            cantidades.put(YearMonth.of(fila.getAnio(), fila.getMes()), fila.getCantidad());
        }
        List<SismosPorMesDto> resultado = new ArrayList<>();
        for (YearMonth mes = YearMonth.from(filtro.getDesde()); !mes.isAfter(YearMonth.from(filtro.getHasta())); mes = mes.plusMonths(1)) {
            resultado.add(new SismosPorMesDto(mes.getYear(), mes.getMonthValue(), cantidades.getOrDefault(mes, 0L)));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<AfectadosPorTipoDto> afectadosPorTipo(DashboardFiltro filtro) {
        aplicarPeriodo(filtro);
        return reporteDanoRepository.sumarAfectadosPorTipo(filtro.getDesde().atStartOfDay(),
                filtro.getHasta().plusDays(1).atStartOfDay(), codigoDepartamento(filtro));
    }

    public String etiquetaMes(SismosPorMesDto fila) {
        String mes = Month.of(fila.getMes()).getDisplayName(TextStyle.SHORT, ESPANOL).replace(".", "");
        return mes.substring(0, 1).toUpperCase(ESPANOL) + mes.substring(1) + " " + fila.getAnio();
    }

    private void aplicarPeriodo(DashboardFiltro filtro) {
        LocalDate hoy = LocalDate.now();
        if (filtro.getDesde() != null && filtro.getHasta() != null && filtro.getDesde().isAfter(filtro.getHasta())) {
            filtro.getAvisos().add("La fecha inicial no puede ser posterior a la final; se muestran los últimos 12 meses.");
            filtro.setDesde(null);
            filtro.setHasta(null);
        }
        if (filtro.getHasta() == null) {
            filtro.setHasta(hoy);
        }
        if (filtro.getDesde() == null) {
            filtro.setDesde(filtro.getHasta().minusMonths(11).withDayOfMonth(1));
        }
    }

    private String codigoDepartamento(DashboardFiltro filtro) {
        if (filtro.getDepartamentoId() == null) {
            return null;
        }
        Ubigeo departamento = ubigeoRepository.findById(filtro.getDepartamentoId()).orElse(null);
        if (departamento == null) {
            filtro.getAvisos().add("El departamento elegido no existe; se muestran todos.");
            filtro.setDepartamentoId(null);
            return null;
        }
        return departamento.getCodigo();
    }
}
