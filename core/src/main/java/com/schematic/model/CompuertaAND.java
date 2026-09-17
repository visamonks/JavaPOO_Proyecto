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
