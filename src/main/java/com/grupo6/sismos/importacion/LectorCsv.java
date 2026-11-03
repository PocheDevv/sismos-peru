package com.grupo6.sismos.importacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee un CSV separado por comas en UTF-8. Admite campos entre comillas dobles
 * (para descripciones que contengan comas) y quita el BOM de Excel si existe.
 */
public final class LectorCsv {

    private LectorCsv() {
    }

    /** Cada elemento es una línea: número de línea en el archivo y sus columnas. */
    public record Linea(int numero, List<String> columnas) {
    }

    public static List<Linea> leer(InputStream entrada) throws IOException {
        List<Linea> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String texto;
            int numero = 0;
            while ((texto = lector.readLine()) != null) {
                numero++;
                if (numero == 1 && texto.startsWith("﻿")) {
                    texto = texto.substring(1);
                }
                if (texto.isBlank()) {
                    continue;
                }
                lineas.add(new Linea(numero, separar(texto)));
            }
        }
        return lineas;
    }

    static List<String> separar(String texto) {
        List<String> columnas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                if (entreComillas && i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (c == ',' && !entreComillas) {
                columnas.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        columnas.add(actual.toString().trim());
        return columnas;
    }
}
