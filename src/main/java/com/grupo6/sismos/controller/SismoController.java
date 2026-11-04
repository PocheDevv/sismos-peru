package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.service.EstacionService;
import com.grupo6.sismos.service.ReporteDanoService;
import com.grupo6.sismos.service.SismoService;
import com.grupo6.sismos.service.TipoAlertaService;
import com.grupo6.sismos.service.UbigeoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.SismoFiltro;
import com.grupo6.sismos.web.form.SismoForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-01 a HU-08, HU-11, HU-15, HU-30 y HU-31. */
@Controller
@RequestMapping("/sismos")
public class SismoController {

    private final SismoService sismoService;
    private final UbigeoService ubigeoService;
    private final TipoAlertaService tipoAlertaService;
    private final EstacionService estacionService;
    private final ReporteDanoService reporteDanoService;

    public SismoController(SismoService sismoService, UbigeoService ubigeoService,
                           TipoAlertaService tipoAlertaService, EstacionService estacionService,
                           ReporteDanoService reporteDanoService) {
        this.sismoService = sismoService;
        this.ubigeoService = ubigeoService;
        this.tipoAlertaService = tipoAlertaService;
        this.estacionService = estacionService;
        this.reporteDanoService = reporteDanoService;
    }

    @GetMapping
    public String listar(@ModelAttribute("filtro") SismoFiltro filtro, BindingResult errores, Model model) {
        if (errores.hasErrors()) {
            filtro.getAvisos().add("Algunos filtros tenían un valor no válido y no se aplicaron.");
        }
        Page<Sismo> pagina = sismoService.buscar(filtro);
        model.addAttribute("pagina", pagina);
        model.addAttribute("sismos", pagina.getContent());
        model.addAttribute("departamentos", ubigeoService.departamentos());
        model.addAttribute("alertas", tipoAlertaService.listar());
        model.addAttribute("estados", Sismo.ESTADOS);
        return "sismos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new SismoForm(), null);
        return "sismos/formulario";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("sismoForm") SismoForm form, BindingResult errores,
                            Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                Sismo sismo = sismoService.registrar(form);
                redirect.addFlashAttribute("exito", "Sismo " + sismo.getCodigo() + " registrado correctamente.");
                return "redirect:/sismos/" + sismo.getId();
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, null);
        return "sismos/formulario";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("sismo", sismoService.buscarPorId(id));
        model.addAttribute("reportes", reporteDanoService.listarPorSismo(id));
        model.addAttribute("totalAfectados", reporteDanoService.totalAfectadosPorSismo(id));
        return "sismos/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Sismo sismo = sismoService.buscarPorId(id);
        cargarFormulario(model, sismoService.formDe(sismo), sismo);
        return "sismos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("sismoForm") SismoForm form,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        Sismo sismo = sismoService.buscarPorId(id);
        if (!errores.hasErrors()) {
            try {
                sismoService.actualizar(id, form);
                redirect.addFlashAttribute("exito", "Sismo actualizado correctamente.");
                return "redirect:/sismos/" + id;
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, sismo);
        return "sismos/formulario";
    }

    @GetMapping("/{id}/eliminar")
    public String confirmarEliminar(@PathVariable Long id, Model model) {
        Sismo sismo = sismoService.buscarPorId(id);
        model.addAttribute("sismo", sismo);
        model.addAttribute("motivo", sismoService.motivoParaNoEliminar(sismo));
        model.addAttribute("reportes", sismoService.contarReportes(id));
        return "sismos/eliminar";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Sismo sismo = sismoService.eliminar(id);
            redirect.addFlashAttribute("exito", "Sismo " + sismo.getCodigo() + " eliminado.");
            return "redirect:/sismos";
        } catch (ReglaNegocioException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/sismos/" + id + "/eliminar";
        }
    }

    /** sismo es null al registrar y el sismo que se edita al editar. */
    private void cargarFormulario(Model model, SismoForm form, Sismo sismo) {
        model.addAttribute("sismoForm", form);
        model.addAttribute("sismo", sismo);
        model.addAttribute("ubigeosCascada", ubigeoService.opcionesCascada());
        model.addAttribute("alertas", tipoAlertaService.listar());
        model.addAttribute("estaciones", estacionService.listar());
        model.addAttribute("intensidades", Sismo.INTENSIDADES);
        model.addAttribute("estados", Sismo.ESTADOS);
        if (sismo != null) {
            model.addAttribute("ubigeoActual", sismo.getUbigeo());
        }
    }
}
