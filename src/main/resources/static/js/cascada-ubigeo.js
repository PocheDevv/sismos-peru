// HU-21: selección de ubigeo en cascada. Lee window.UBIGEOS = [{id, codigo, nombre, padreId}, ...]
// y deja en el campo oculto #ubigeoId el nivel más preciso elegido.
(function () {
    const ubigeos = window.UBIGEOS || [];
    const selects = [1, 2, 3].map(n => document.getElementById("cascada-" + n));
    const oculto = document.getElementById("ubigeoId");
    const textoVacio = ["Seleccione el departamento", "Seleccione la provincia", "Seleccione el distrito"];

    const hijosDe = padreId => ubigeos
        .filter(u => u.padreId === padreId)
        .sort((a, b) => a.nombre.localeCompare(b.nombre));

    function llenar(nivel, padreId) {
        const select = selects[nivel];
        const opciones = padreId === undefined ? [] : hijosDe(padreId);
        select.innerHTML = "";
        const vacia = document.createElement("option");
        vacia.value = "";
        vacia.textContent = opciones.length || nivel === 0 ? textoVacio[nivel] : "—";
        select.appendChild(vacia);
        opciones.forEach(u => {
            const opcion = document.createElement("option");
            opcion.value = u.id;
            opcion.textContent = u.nombre + " (" + u.codigo + ")";
            select.appendChild(opcion);
        });
        select.disabled = nivel > 0 && opciones.length === 0;
    }

    function actualizarOculto() {
        const elegido = [...selects].reverse().find(s => s.value !== "");
        if (elegido) {
            oculto.value = elegido.value;
        } else if (window.UBIGEO_ACTUAL == null) {
            oculto.value = "";
        }
    }

    function alCambiar(nivel) {
        for (let n = nivel + 1; n < selects.length; n++) {
            llenar(n, n === nivel + 1 && selects[nivel].value !== "" ? Number(selects[nivel].value) : undefined);
        }
        actualizarOculto();
    }

    selects.forEach((select, nivel) => select.addEventListener("change", () => alCambiar(nivel)));

    // Precarga: si el formulario ya trae un ubigeo (editar o volver con errores), marca su ruta completa.
    llenar(0, null);
    llenar(1, undefined);
    llenar(2, undefined);
    const ruta = [];
    let actual = ubigeos.find(u => String(u.id) === oculto.value);
    while (actual) {
        ruta.unshift(actual);
        actual = ubigeos.find(u => u.id === actual.padreId);
    }
    ruta.forEach((u, nivel) => {
        if (nivel < selects.length) {
            selects[nivel].value = u.id;
            if (nivel + 1 < selects.length) {
                llenar(nivel + 1, u.id);
            }
        }
    });
})();
