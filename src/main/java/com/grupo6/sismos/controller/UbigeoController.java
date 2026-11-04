package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.TipoUbigeo;
import com.grupo6.sismos.entity.Ubigeo;
import com.grupo6.sismos.service.UbigeoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.UbigeoForm;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-09: listar, registrar, editar y eliminar ubigeos por niveles. */
@Controller
@RequestMapping("/ubigeos")
public class UbigeoController {

    private final UbigeoService ubigeoService;

    public UbigeoController(UbigeoService ubigeoService) {
        this.ubigeoService = ubigeoService;
    }

    /** Sin padreId muestra los departamentos; con padreId, los hijos de ese ubigeo. */
    @GetMapping
    public String listar(@RequestParam(required = false) Long padreId, Model model) {
        Ubigeo padre = padreId == null ? null : ubigeoService.buscarPorId(padreId);
        model.addAttribute("padre", padre);
        model.addAttribute("ubigeos", ubigeoService.listarHijos(padreId));
        model.addAttribute("tipoHijo", padre == null ? TipoUbigeo.DEPARTAMENTO : padre.getTipo().getTipoHijo());
        return "ubigeos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long padreId, Model model) {
        UbigeoForm form = new UbigeoForm();
        form.setPadreId(padreId);
        TipoUbigeo tipo = ubigeoService.tipoParaHijoDe(padreId);
        form.setTipo(tipo == null ? null : tipo.name());
        if (padreId != null) {
            form.setCodigo(ubigeoService.buscarPorId(padreId).getCodigo());
        }
        cargarFormulario(model, form, null);
        return "ubigeos/formulario";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("ubigeoForm") UbigeoForm form, BindingResult errores,
                            Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                Ubigeo ubigeo = ubigeoService.registrar(form);
                redirect.addFlashAttribute("exito", ubigeo.getTipo().getNombre() + " " + ubigeo.getNombre()
                        + " registrado correctamente.");
                return redirectLista(form.getPadreId());
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, null);
        return "ubigeos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Ubigeo ubigeo = ubigeoService.buscarPorId(id);
        cargarFormulario(model, ubigeoService.formDe(ubigeo), ubigeo);
        return "ubigeos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("ubigeoForm") UbigeoForm form,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        Ubigeo ubigeo = ubigeoService.buscarPorId(id);
        if (!errores.hasErrors()) {
            try {
                ubigeoService.actualizar(id, form);
                redirect.addFlashAttribute("exito", ubigeo.getTipo().getNombre() + " actualizado correctamente.");
                return redirectLista(ubigeo.getPadre() == null ? null : ubigeo.getPadre().getId());
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, ubigeo);
        return "ubigeos/formulario";
    }

    @GetMapping("/{id}/eliminar")
    public String confirmarEliminar(@PathVariable Long id, Model model) {
        Ubigeo ubigeo = ubigeoService.buscarPorId(id);
        model.addAttribute("ubigeo", ubigeo);
        model.addAttribute("motivo", ubigeoService.motivoParaNoEliminar(ubigeo));
        return "ubigeos/eliminar";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Ubigeo ubigeo = ubigeoService.eliminar(id);
            redirect.addFlashAttribute("exito", ubigeo.getTipo().getNombre() + " " + ubigeo.getNombre() + " eliminado.");
            return redirectLista(ubigeo.getPadre() == null ? null : ubigeo.getPadre().getId());
        } catch (ReglaNegocioException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/ubigeos/" + id + "/eliminar";
        }
    }

    private void cargarFormulario(Model model, UbigeoForm form, Ubigeo ubigeo) {
        Ubigeo padre = form.getPadreId() == null ? null : ubigeoService.buscarPorId(form.getPadreId());
        model.addAttribute("ubigeoForm", form);
        model.addAttribute("ubigeo", ubigeo);
        model.addAttribute("padre", padre);
        model.addAttribute("tiposRaiz", List.of(TipoUbigeo.DEPARTAMENTO, TipoUbigeo.PROVINCIA_CONSTITUCIONAL));
        model.addAttribute("tipoActual", TipoUbigeo.desdeTexto(form.getTipo()));
    }

    private static String redirectLista(Long padreId) {
        return padreId == null ? "redirect:/ubigeos" : "redirect:/ubigeos?padreId=" + padreId;
    }
}
