package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.TipoDano;
import com.grupo6.sismos.service.TipoDanoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.TipoDanoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Maestro de tipos de daño: listar, registrar y activar o desactivar. */
@Controller
@RequestMapping("/tipos-dano")
public class TipoDanoController {

    private final TipoDanoService tipoDanoService;

    public TipoDanoController(TipoDanoService tipoDanoService) {
        this.tipoDanoService = tipoDanoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", tipoDanoService.listar());
        return "tipos-dano/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tipoDanoForm", new TipoDanoForm());
        return "tipos-dano/formulario";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("tipoDanoForm") TipoDanoForm form, BindingResult errores,
                            RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                TipoDano tipo = tipoDanoService.registrar(form);
                redirect.addFlashAttribute("exito", "Tipo de daño " + tipo.getCodigo() + " registrado correctamente.");
                return "redirect:/tipos-dano";
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        return "tipos-dano/formulario";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirect) {
        TipoDano tipo = tipoDanoService.cambiarEstado(id);
        redirect.addFlashAttribute("exito", "El tipo de daño " + tipo.getCodigo() + " ahora está " + tipo.getEstado() + ".");
        return "redirect:/tipos-dano";
    }
}
