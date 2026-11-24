(() => {
    const peruBounds = [[-18.35, -81.35], [-0.03, -68.65]];

    document.querySelectorAll(".mapa-componente").forEach((contenedor) => {
        const canvas = contenedor.querySelector(".mapa-canvas");
        const aviso = contenedor.querySelector(".mapa-aviso");

        if (!window.L) {
            aviso.textContent = "El mapa no está disponible. Compruebe su conexión e inténtelo de nuevo.";
            return;
        }

        const centroLatitud = Number(contenedor.dataset.centroLatitud);
        const centroLongitud = Number(contenedor.dataset.centroLongitud);
        const tieneCentro = Number.isFinite(centroLatitud) && centroLatitud >= -90 && centroLatitud <= 90
            && Number.isFinite(centroLongitud) && centroLongitud >= -180 && centroLongitud <= 180;
        const zoom = Number(contenedor.dataset.zoom);
        const mapa = window.L.map(canvas, {scrollWheelZoom: false});
        const capa = window.L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap"
        });

        capa.on("tileerror", () => {
            aviso.textContent = "No se pudo cargar el mapa base; los marcadores disponibles siguen visibles.";
        });
        capa.addTo(mapa);

        const marcadores = [];
        contenedor.querySelectorAll(".mapa-datos > div").forEach((dato) => {
            const latitud = Number(dato.dataset.latitud);
            const longitud = Number(dato.dataset.longitud);
            if (!Number.isFinite(latitud) || latitud < -90 || latitud > 90
                || !Number.isFinite(longitud) || longitud < -180 || longitud > 180) {
                return;
            }

            const magnitud = Number(dato.dataset.magnitud);
            const magnitudValida = Number.isFinite(magnitud);
            const textoMagnitud = magnitudValida ? `M ${magnitud.toFixed(1)}` : "M —";
            const categoria = !magnitudValida || magnitud < 4 ? "magnitud-baja"
                : magnitud < 6 ? "magnitud-media" : "magnitud-alta";
            const etiqueta = document.createElement("span");
            etiqueta.className = `marcador-etiqueta ${categoria}`;
            etiqueta.textContent = textoMagnitud;
            const icono = window.L.divIcon({
                className: "marcador-magnitud",
                html: etiqueta,
                iconSize: [66, 28],
                iconAnchor: [33, 14]
            });
            const marcador = window.L.marker([latitud, longitud], {icon: icono});
            marcador.bindPopup(crearResumen(dato.dataset, textoMagnitud));
            marcador.addTo(mapa);
            marcadores.push(marcador);
        });

        if (tieneCentro) {
            mapa.setView([centroLatitud, centroLongitud], Number.isFinite(zoom) ? zoom : 8);
        } else if (marcadores.length > 1) {
            mapa.fitBounds(window.L.featureGroup(marcadores).getBounds().pad(0.15), {maxZoom: 8});
        } else if (marcadores.length === 1) {
            mapa.setView(marcadores[0].getLatLng(), 7);
        } else {
            mapa.fitBounds(peruBounds);
        }

        if (marcadores.length === 0) {
            aviso.textContent = "No hay sismos con coordenadas válidas para mostrar en el mapa.";
        } else {
            aviso.textContent = `${marcadores.length} marcador(es) con coordenadas válidas.`;
        }
    });

    function crearResumen(datos, textoMagnitud) {
        const resumen = document.createElement("div");
        resumen.className = "resumen-marcador";
        const titulo = document.createElement("strong");
        titulo.textContent = textoMagnitud;
        resumen.appendChild(titulo);

        [
            ["Profundidad", `${numero(datos.profundidad, 1)} km`],
            ["Fecha", datos.fecha || "No disponible"],
            ["Referencia", datos.referencia || "Sin referencia"]
        ].forEach(([etiqueta, valor]) => {
            const fila = document.createElement("p");
            const nombre = document.createElement("strong");
            nombre.textContent = `${etiqueta}: `;
            fila.append(nombre, document.createTextNode(valor));
            resumen.appendChild(fila);
        });

        if (datos.url) {
            const enlace = document.createElement("a");
            enlace.href = datos.url;
            enlace.textContent = "Abrir detalle";
            resumen.appendChild(enlace);
        }
        return resumen;
    }

    function numero(valor, decimales) {
        const resultado = Number(valor);
        return Number.isFinite(resultado) ? resultado.toFixed(decimales) : "No disponible";
    }
})();
