package com.schematic.model;

public class Cable {

    public Cable() {
    }

    @Override
    public String toString() {
        return "Cable{}";
    }
}

/*
Que hay ahorita:
La clase base creada y lista para los cables que uniran los componentes.

Que falta:
Guardar los puntos donde dobla en 90 grados y pasar la energia de una pieza a otra.

Recomendaciones:
Mantenlo separado de las piezas para que sea facil borrar cables sin romper los componentes.
*/
