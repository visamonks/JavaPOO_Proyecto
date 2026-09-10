package com.schematic.model;

public class CompuertaAND extends CompuertaLogica {

    public CompuertaAND(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY, entradaA, entradaB);
    }

    public CompuertaAND(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false, false);
    }

    @Override
    public boolean calcularSalida() {
        return entradaA && entradaB;
    }

    @Override
    public String toString() {
        return "CompuertaAND{" +
                "identificador='" + identificador + '\'' +
                ", entradaA=" + entradaA +
                ", entradaB=" + entradaB +
                ", salida=" + salida +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}

/*
Que hay ahorita:
Una compuerta logica que revisa dos entradas y solo se activa con exito si ambas estan encendidas al mismo tiempo.

Que falta:
Conectar sus entradas y su salida con los cables cuando hagamos el sistema de conexion.

Recomendaciones:
Sirve como ejemplo perfecto para cuando quieras crear la compuerta OR u otras compuertas parecidas.
*/
