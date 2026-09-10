package com.schematic.model;

public abstract class CompuertaLogica extends Componente {

    protected boolean entradaA;
    protected boolean entradaB;
    protected boolean salida;

    public CompuertaLogica(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY);
        this.entradaA = entradaA;
        this.entradaB = entradaB;
        this.evaluarEstado();
    }

    public void setEntradas(boolean entradaA, boolean entradaB) {
        this.entradaA = entradaA;
        this.entradaB = entradaB;
        this.evaluarEstado();
    }

    public abstract boolean calcularSalida();

    @Override
    public boolean evaluarEstado() {
        this.salida = calcularSalida();
        if (salida) {
            this.estadoActual = "EXITO";
        } else {
            this.estadoActual = "NEUTRO";
        }
        return salida;
    }

    public boolean isEntradaA() {
        return entradaA;
    }

    public void setEntradaA(boolean entradaA) {
        this.entradaA = entradaA;
        this.evaluarEstado();
    }

    public boolean isEntradaB() {
        return entradaB;
    }

    public void setEntradaB(boolean entradaB) {
        this.entradaB = entradaB;
        this.evaluarEstado();
    }

    public boolean isSalida() {
        return salida;
    }

    @Override
    public String toString() {
        return "CompuertaLogica{" +
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
Plantilla intermedia para cualquier compuerta que recibe dos valores logicos y calcula un resultado.

Que falta:
Conectar sus dos entradas y su salida a cables cuando implementemos el cableado en la segunda semana.

*/
