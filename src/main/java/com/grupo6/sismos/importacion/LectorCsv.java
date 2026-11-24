package com.grupo6.sismos.importacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee un CSV en UTF-8 o en Latin-1 (ISO-8859-1, como el archivo oficial del INEI), separado
 * por comas o por punto y coma. Admite campos entre comillas dobles, quita el BOM de Excel
 * y omite las líneas vacías (también las que solo tienen separadores).
 */
public final class LectorCsv {

    private LectorCsv() {
    }

    /** Cada elemento es una línea: número de línea en el archivo y sus columnas. */
    public record Linea(int numero, List<String> columnas) {
    }

    public static List<Linea> leer(InputStream entrada) throws IOException {
        String contenido = decodificar(entrada.readAllBytes());
        List<Linea> lineas = new ArrayList<>();
        char separador = 0;
        try (BufferedReader lector = new BufferedReader(new StringReader(contenido))) {
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
                if (separador == 0) {
                    separador = detectarSeparador(texto);
                }
                List<String> columnas = separar(texto, separador);
                if (columnas.stream().allMatch(String::isEmpty)) {
                    continue;
                }
                lineas.add(new Linea(numero, columnas));
            }
        }
        return lineas;
    }

    /** UTF-8 si los bytes son UTF-8 válido; si no, Latin-1 (los acentos y la Ñ del INEI). */
    static String decodificar(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException noEsUtf8) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }

    /** El separador es el que más aparece en el encabezado: punto y coma o coma. */
    static char detectarSeparador(String encabezado) {
        long puntoYComa = encabezado.chars().filter(c -> c == ';').count();
        long comas = encabezado.chars().filter(c -> c == ',').count();
        return puntoYComa > comas ? ';' : ',';
    }

    static List<String> separar(String texto, char separador) {
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
            } else if (c == separador && !entreComillas) {
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
