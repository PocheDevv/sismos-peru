package com.grupo6.sismos.controller;

import com.grupo6.sismos.importacion.CatalogoCsv;
import com.grupo6.sismos.importacion.ResultadoImportacion;
import com.grupo6.sismos.service.ImportacionService;
import com.grupo6.sismos.web.error.ReglaNegocioException;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** Importación de datos maestros desde CSV (administrador). */
@Controller
@RequestMapping("/importacion")
public class ImportacionController {

    private final ImportacionService importacionService;

    public ImportacionController(ImportacionService importacionService) {
        this.importacionService = importacionService;
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("catalogos", CatalogoCsv.values());
        return "importacion/formulario";
    }

    @PostMapping
    public String importar(@RequestParam(required = false) CatalogoCsv catalogo,
                           @RequestParam(required = false) MultipartFile archivo, Model model) {
        model.addAttribute("catalogos", CatalogoCsv.values());
        model.addAttribute("catalogoElegido", catalogo);
        if (catalogo == null) {
            model.addAttribute("error", "Seleccione el catálogo que va a importar.");
        } else if (archivo == null || archivo.isEmpty()) {
            model.addAttribute("error", "Seleccione el archivo CSV.");
        } else {
            try {
                ResultadoImportacion resultado =
                        importacionService.importar(catalogo, archivo.getOriginalFilename(), archivo.getInputStream());
                model.addAttribute("resultado", resultado);
            } catch (ReglaNegocioException error) {
                model.addAttribute("error", error.getMessage());
            } catch (IOException error) {
                model.addAttribute("error", "No se pudo leer el archivo.");
            }
        }
        return "importacion/formulario";
    }
}
