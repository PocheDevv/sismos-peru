package com.grupo6.sismos.service;

import com.grupo6.sismos.dto.reporte.IndicadoresDashboardDto;
import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.repository.ReporteDanoRepository;
import com.grupo6.sismos.repository.SismoRepository;
import com.grupo6.sismos.repository.UbigeoRepository;
import com.grupo6.sismos.web.form.DashboardFiltro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

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
