package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.service.SeguimientoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.SeguimientoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-14: registrar el seguimiento de un sismo relevante desde su detalle. */
@Controller
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping("/sismos/{sismoId}/seguimientos/nuevo")
    public String nuevo(@PathVariable Long sismoId, Model model, RedirectAttributes redirect) {
        Sismo sismo = seguimientoService.buscarSismo(sismoId);
        String motivo = seguimientoService.motivoParaNoRegistrar(sismo);
        if (motivo != null) {
            redirect.addFlashAttribute("error", motivo);
            return "redirect:/sismos/" + sismoId;
        }
        model.addAttribute("seguimientoForm", seguimientoService.formNuevo());
        model.addAttribute("sismo", sismo);
        return "seguimientos/formulario";
    }

    @PostMapping("/sismos/{sismoId}/seguimientos")
    public String registrar(@PathVariable Long sismoId, @Valid @ModelAttribute("seguimientoForm") SeguimientoForm form,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        Sismo sismo = seguimientoService.buscarSismo(sismoId);
        if (!errores.hasFieldErrors("fechaActualizacion")) {
            String errorFecha = seguimientoService.errorFecha(sismo, form.getFechaActualizacion());
            if (errorFecha != null) {
                errores.rejectValue("fechaActualizacion", "regla", errorFecha);
            }
        }
        if (!errores.hasErrors()) {
            try {
                seguimientoService.registrar(sismoId, form);
                redirect.addFlashAttribute("exito", "Seguimiento registrado para el sismo " + sismo.getCodigo() + ".");
                return "redirect:/sismos/" + sismoId;
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        model.addAttribute("sismo", sismo);
        return "seguimientos/formulario";
    }
}
