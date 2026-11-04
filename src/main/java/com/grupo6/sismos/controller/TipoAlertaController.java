package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.TipoAlerta;
import com.grupo6.sismos.service.TipoAlertaService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.TipoAlertaForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Maestro de tipos de alerta: listar, registrar y activar o desactivar. */
@Controller
@RequestMapping("/tipos-alerta")
public class TipoAlertaController {

    private final TipoAlertaService tipoAlertaService;

    public TipoAlertaController(TipoAlertaService tipoAlertaService) {
        this.tipoAlertaService = tipoAlertaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", tipoAlertaService.listar());
        return "tipos-alerta/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tipoAlertaForm", new TipoAlertaForm());
        model.addAttribute("colores", TipoAlerta.COLORES);
        return "tipos-alerta/formulario";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("tipoAlertaForm") TipoAlertaForm form, BindingResult errores,
                            Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                TipoAlerta tipo = tipoAlertaService.registrar(form);
                redirect.addFlashAttribute("exito", "Tipo de alerta " + tipo.getCodigo() + " registrado correctamente.");
                return "redirect:/tipos-alerta";
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        model.addAttribute("colores", TipoAlerta.COLORES);
        return "tipos-alerta/formulario";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirect) {
        TipoAlerta tipo = tipoAlertaService.cambiarEstado(id);
        redirect.addFlashAttribute("exito", "El tipo de alerta " + tipo.getCodigo() + " ahora está " + tipo.getEstado() + ".");
        return "redirect:/tipos-alerta";
    }
}
