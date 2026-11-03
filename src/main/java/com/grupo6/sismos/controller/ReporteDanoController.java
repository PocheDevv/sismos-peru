package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.service.ReporteDanoService;
import com.grupo6.sismos.service.TipoDanoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.ReporteDanoForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReporteDanoController {

    private final ReporteDanoService reporteDanoService;
    private final TipoDanoService tipoDanoService;

    public ReporteDanoController(ReporteDanoService reporteDanoService, TipoDanoService tipoDanoService) {
        this.reporteDanoService = reporteDanoService;
        this.tipoDanoService = tipoDanoService;
    }

    @GetMapping("/reportes")
    public String listar(@RequestParam(required = false) String pagina, Model model) {
        Integer numero = pagina != null && pagina.matches("\\d{1,6}") ? Integer.valueOf(pagina) : null;
        Page<ReporteDano> resultado = reporteDanoService.listar(numero);
        model.addAttribute("pagina", resultado);
        model.addAttribute("reportes", resultado.getContent());
        return "reportes/lista";
    }

    @GetMapping("/sismos/{sismoId}/reportes/nuevo")
    public String nuevo(@PathVariable Long sismoId, Model model, RedirectAttributes redirect) {
        Sismo sismo = reporteDanoService.buscarSismo(sismoId);
        String motivo = reporteDanoService.motivoParaNoReportar(sismo);
        if (motivo != null) {
            redirect.addFlashAttribute("error", motivo);
            return "redirect:/sismos/" + sismoId;
        }
        cargarFormulario(model, reporteDanoService.formNuevo(), sismo);
        return "reportes/formulario";
    }

    @PostMapping("/sismos/{sismoId}/reportes")
    public String registrar(@PathVariable Long sismoId, @Valid @ModelAttribute("reporteForm") ReporteDanoForm form,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        Sismo sismo = reporteDanoService.buscarSismo(sismoId);
        if (!errores.hasFieldErrors("fechaReporte")) {
            String errorFecha = reporteDanoService.errorFechaReporte(sismo, form.getFechaReporte());
            if (errorFecha != null) {
                errores.rejectValue("fechaReporte", "regla", errorFecha);
            }
        }
        if (!errores.hasErrors()) {
            try {
                ReporteDano reporte = reporteDanoService.registrar(sismoId, form);
                redirect.addFlashAttribute("exito", "Reporte de afectación (" + reporte.getTipoDano().getNombre()
                        + ") registrado para el sismo " + sismo.getCodigo() + ".");
                return "redirect:/sismos/" + sismoId;
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, sismo);
        return "reportes/formulario";
    }

    private void cargarFormulario(Model model, ReporteDanoForm form, Sismo sismo) {
        model.addAttribute("reporteForm", form);
        model.addAttribute("sismo", sismo);
        model.addAttribute("tiposDano", tipoDanoService.activos());
        model.addAttribute("niveles", ReporteDano.NIVELES);
    }
}
