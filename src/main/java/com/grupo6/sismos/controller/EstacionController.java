package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.EstacionMonitoreo;
import com.grupo6.sismos.service.EstacionService;
import com.grupo6.sismos.service.UbigeoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.EstacionForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-10: listar, registrar y editar estaciones (incluye activar o desactivar). */
@Controller
@RequestMapping("/estaciones")
public class EstacionController {

    private final EstacionService estacionService;
    private final UbigeoService ubigeoService;

    public EstacionController(EstacionService estacionService, UbigeoService ubigeoService) {
        this.estacionService = estacionService;
        this.ubigeoService = ubigeoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estaciones", estacionService.listar());
        model.addAttribute("sismosPorEstacion", estacionService.contarSismosPorEstacion());
        return "estaciones/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new EstacionForm(), null);
        return "estaciones/formulario";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("estacionForm") EstacionForm form, BindingResult errores,
                            Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                EstacionMonitoreo estacion = estacionService.registrar(form);
                redirect.addFlashAttribute("exito", "Estación " + estacion.getCodigo() + " registrada correctamente.");
                return "redirect:/estaciones";
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, null);
        return "estaciones/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        EstacionMonitoreo estacion = estacionService.buscarPorId(id);
        cargarFormulario(model, estacionService.formDe(estacion), estacion);
        return "estaciones/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("estacionForm") EstacionForm form,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        EstacionMonitoreo estacion = estacionService.buscarPorId(id);
        if (!errores.hasErrors()) {
            try {
                estacionService.actualizar(id, form);
                redirect.addFlashAttribute("exito", "Estación " + form.getCodigo().trim().toUpperCase()
                        + " actualizada correctamente.");
                return "redirect:/estaciones";
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, estacion);
        return "estaciones/formulario";
    }

    private void cargarFormulario(Model model, EstacionForm form, EstacionMonitoreo estacion) {
        model.addAttribute("estacionForm", form);
        model.addAttribute("estacion", estacion);
        model.addAttribute("ubigeosCascada", ubigeoService.opcionesCascada());
        if (estacion != null) {
            model.addAttribute("ubigeoActual", estacion.getUbigeo());
        }
    }
}
