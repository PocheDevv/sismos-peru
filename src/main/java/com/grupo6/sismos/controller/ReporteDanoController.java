package com.grupo6.sismos.controller;

import com.grupo6.sismos.entity.ReporteDano;
import com.grupo6.sismos.entity.Sismo;
import com.grupo6.sismos.entity.TipoDano;
import com.grupo6.sismos.service.ReporteDanoService;
import com.grupo6.sismos.service.TipoDanoService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import com.grupo6.sismos.web.form.ReporteDanoForm;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
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
        cargarFormulario(model, reporteDanoService.formNuevo(), sismo, null);
        return "reportes/formulario";
    }

    @PostMapping("/sismos/{sismoId}/reportes")
    public String registrar(@PathVariable Long sismoId, @Valid @ModelAttribute("reporteForm") ReporteDanoForm form,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        Sismo sismo = reporteDanoService.buscarSismo(sismoId);
        validarFecha(sismo, form, errores);
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
        cargarFormulario(model, form, sismo, null);
        return "reportes/formulario";
    }

    @GetMapping("/sismos/{sismoId}/reportes/{id}/editar")
    public String editar(@PathVariable Long sismoId, @PathVariable Long id, Model model, RedirectAttributes redirect) {
        ReporteDano reporte = reporteDanoService.buscarReporte(sismoId, id);
        String motivo = reporteDanoService.motivoParaNoEditar(reporte);
        if (motivo != null) {
            redirect.addFlashAttribute("error", motivo);
            return "redirect:/sismos/" + sismoId;
        }
        cargarFormulario(model, reporteDanoService.formDe(reporte), reporte.getSismo(), reporte);
        return "reportes/formulario";
    }

    @PostMapping("/sismos/{sismoId}/reportes/{id}")
    public String actualizar(@PathVariable Long sismoId, @PathVariable Long id,
                             @Valid @ModelAttribute("reporteForm") ReporteDanoForm form, BindingResult errores,
                             Model model, RedirectAttributes redirect) {
        ReporteDano reporte = reporteDanoService.buscarReporte(sismoId, id);
        validarFecha(reporte.getSismo(), form, errores);
        if (!errores.hasErrors()) {
            try {
                reporteDanoService.actualizar(sismoId, id, form);
                redirect.addFlashAttribute("exito", "Reporte de afectación actualizado correctamente.");
                return "redirect:/sismos/" + sismoId;
            } catch (ReglaNegocioException error) {
                Errores.registrar(errores, error);
            }
        }
        cargarFormulario(model, form, reporte.getSismo(), reporte);
        return "reportes/formulario";
    }
    
    @PostMapping("/sismos/{sismoId}/reportes/{id}/estado")
    public String cambiarEstado(@PathVariable Long sismoId, @PathVariable Long id,
                                @RequestParam("estado") String nuevoEstado,
                                RedirectAttributes redirect) {
        try {
            reporteDanoService.actualizarEstado(sismoId, id, nuevoEstado);
            redirect.addFlashAttribute("exito", "El estado del reporte de afectación se actualizó correctamente.");
        } catch (ReglaNegocioException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/sismos/" + sismoId;
    }

    private void validarFecha(Sismo sismo, ReporteDanoForm form, BindingResult errores) {
        if (!errores.hasFieldErrors("fechaReporte")) {
            String errorFecha = reporteDanoService.errorFechaReporte(sismo, form.getFechaReporte());
            if (errorFecha != null) {
                errores.rejectValue("fechaReporte", "regla", errorFecha);
            }
        }
    }

    private void cargarFormulario(Model model, ReporteDanoForm form, Sismo sismo, ReporteDano reporte) {
        List<TipoDano> tiposDano = new ArrayList<>(tipoDanoService.activos());
        if (reporte != null && !reporte.getTipoDano().isActivo()) {
            tiposDano.add(reporte.getTipoDano());
        }
        model.addAttribute("reporteForm", form);
        model.addAttribute("sismo", sismo);
        model.addAttribute("reporte", reporte);
        model.addAttribute("tiposDano", tiposDano);
        model.addAttribute("niveles", ReporteDano.NIVELES);
        model.addAttribute("estados", List.of("PENDIENTE", "VERIFICADO", "CERRADO"));
    }
}
