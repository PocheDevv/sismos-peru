package com.grupo6.sismos.importacion;

/** HU-40: una fila del CSV que no se importó y el motivo. */
public record FilaRechazada(int linea, String codigo, String motivo) {
}
