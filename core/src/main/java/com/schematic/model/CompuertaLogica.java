package com.schematic.model;

public abstract class CompuertaLogica extends Componente {

    protected boolean entradaA;
    protected boolean entradaB;
    protected boolean salida;

    protected Terminal terminalEntradaA;
    protected Terminal terminalEntradaB;
    protected Terminal terminalSalida;

    public CompuertaLogica(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY);
        this.entradaA = entradaA;
        this.entradaB = entradaB;
        this.ancho = 60f;
        this.alto = 50f;

        
        this.terminalEntradaA = new Terminal(identificador + "_IN_A", this, 0f, alto * 0.75f, true);
        this.terminalEntradaB = new Terminal(identificador + "_IN_B", this, 0f, alto * 0.25f, true);
        this.terminalEntradaA.setValorLogico(entradaA);
        this.terminalEntradaB.setValorLogico(entradaB);

        
        this.terminalSalida = new Terminal(identificador + "_OUT", this, ancho, alto * 0.5f, false);

        this.terminales.add(terminalEntradaA);
        this.terminales.add(terminalEntradaB);
        this.terminales.add(terminalSalida);

        this.evaluarEstado();
    }

    public void setEntradas(boolean entradaA, boolean entradaB) {
        this.entradaA = entradaA;
        this.entradaB = entradaB;
        if (terminalEntradaA != null) terminalEntradaA.setValorLogico(entradaA);
        if (terminalEntradaB != null) terminalEntradaB.setValorLogico(entradaB);
        this.evaluarEstado();
    }

    public abstract boolean calcularSalida();

    @Override
    public boolean evaluarEstado() {
        if (terminalEntradaA != null) {
            this.entradaA = terminalEntradaA.getValorLogico();
        }
        if (terminalEntradaB != null) {
            this.entradaB = terminalEntradaB.getValorLogico();
        }

        this.salida = calcularSalida();

        if (terminalSalida != null) {
            terminalSalida.setValorLogico(salida);
        }

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
        if (terminalEntradaA != null) terminalEntradaA.setValorLogico(entradaA);
        this.evaluarEstado();
    }

    public boolean isEntradaB() {
        return entradaB;
    }

    public void setEntradaB(boolean entradaB) {
        this.entradaB = entradaB;
        if (terminalEntradaB != null) terminalEntradaB.setValorLogico(entradaB);
        this.evaluarEstado();
    }

    public boolean isSalida() {
        return salida;
    }

    public Terminal getTerminalEntradaA() {
        return terminalEntradaA;
    }

    public Terminal getTerminalEntradaB() {
        return terminalEntradaB;
    }

    public Terminal getTerminalSalida() {
        return terminalSalida;
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
