package com.schematic.model;

public abstract class CompuertaLogica extends Componente {

    protected boolean entradaA;
    protected boolean entradaB;
    protected boolean salida;

    public CompuertaLogica(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
       
        super(identificador, posicionX, posicionY, 60.0f, 40.0f);
        this.entradaA = entradaA;
        this.entradaB = entradaB;
        this.evaluarEstado();
    }

    public CompuertaLogica(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false, false);
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

    // getters y setters
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