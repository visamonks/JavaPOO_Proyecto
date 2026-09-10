package com.schematic.model;

public class CompuertaOR extends CompuertaLogica {

    public CompuertaOR(String identificador, float posicionX, float posicionY) {
        super(identificador, posicionX, posicionY, false, false);
    }

    public CompuertaOR(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY, entradaA, entradaB);
    }

    @Override
    public boolean calcularSalida() {
        return entradaA || entradaB;
    }

    @Override
    public String toString() {
        return "CompuertaOR{" +
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
Una compuerta logica que revisa dos entradas y se activa con exito si al menos una de las dos esta encendida.

Que falta:
Conectar sus entradas y salidas a los cables.

*/
