package com.grupo6.sismos.controller;

import com.grupo6.sismos.dto.reporte.AfectadosPorTipoDto;
import com.grupo6.sismos.dto.reporte.SismosPorMesDto;
import com.grupo6.sismos.service.DashboardService;
import com.grupo6.sismos.service.UbigeoService;
import com.grupo6.sismos.web.form.DashboardFiltro;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final UbigeoService ubigeoService;

    public DashboardController(DashboardService dashboardService, UbigeoService ubigeoService) {
        this.dashboardService = dashboardService;
        this.ubigeoService = ubigeoService;
    }

    @GetMapping("/dashboard")
    public String mostrar(@ModelAttribute("filtro") DashboardFiltro filtro, BindingResult errores, Model model) {
        if (errores.hasErrors()) {
            filtro.getAvisos().add("Algunos filtros tenían un valor no válido y no se aplicaron.");
        }
        model.addAttribute("kpis", dashboardService.indicadores(filtro));
        List<SismosPorMesDto> porMes = dashboardService.sismosPorMes(filtro);
        List<AfectadosPorTipoDto> porTipo = dashboardService.afectadosPorTipo(filtro);
        model.addAttribute("etiquetasMeses", porMes.stream().map(dashboardService::etiquetaMes).toList());
        model.addAttribute("valoresMeses", porMes.stream().map(SismosPorMesDto::getCantidad).toList());
        model.addAttribute("etiquetasTipos", porTipo.stream().map(AfectadosPorTipoDto::getTipoDano).toList());
        model.addAttribute("valoresTipos", porTipo.stream().map(AfectadosPorTipoDto::getTotal).toList());
        model.addAttribute("departamentos", ubigeoService.departamentos());
        return "dashboard/index";
    }
}
