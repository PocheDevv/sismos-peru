package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.service.ReporteDanoService;
import com.grupo6.sismos.service.TipoDanoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.ReporteDanoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sismos/{sismoId}/reportes")
public class ReporteDanoController {

    private final ReporteDanoService reporteDanoService;
    private final TipoDanoService tipoDanoService;

    public ReporteDanoController(ReporteDanoService reporteDanoService, TipoDanoService tipoDanoService) {
        this.reporteDanoService = reporteDanoService;
        this.tipoDanoService = tipoDanoService;
    }

    @GetMapping("/nuevo")
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

    @PostMapping
    public String registrar(@PathVariable Long sismoId, @Valid @ModelAttribute("reporteForm") ReporteDanoForm form,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        Sismo sismo = reporteDanoService.buscarSismo(sismoId);
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
