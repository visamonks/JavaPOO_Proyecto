package com.schematic.model.componentes;

public abstract class CompuertaLogica extends Componente {

    protected boolean entradaA;
    protected boolean entradaB;
    protected boolean salida;
    protected Terminal terminalEntradaA;
    protected Terminal terminalEntradaB;
    protected Terminal terminalSalida;

    public CompuertaLogica(String identificador, float posicionX, float posicionY, boolean entradaA, boolean entradaB) {
        super(identificador, posicionX, posicionY, 280.0f, 280.0f);
        this.entradaA = entradaA;
        this.entradaB = entradaB;

        this.terminalEntradaA = new Terminal(identificador + "_IN_A", this, 44.0f, 161.0f, true);
        this.terminalEntradaB = new Terminal(identificador + "_IN_B", this, 44.0f, 136.0f, true);
        this.terminalSalida = new Terminal(identificador + "_OUT", this, 212.0f, 150.0f, false);

        this.terminalEntradaA.setValorLogico(entradaA);
        this.terminalEntradaB.setValorLogico(entradaB);

        this.terminales.add(this.terminalEntradaA);
        this.terminales.add(this.terminalEntradaB);
        this.terminales.add(this.terminalSalida);

        this.agregarPuntoConexion(44, 161);
        this.agregarPuntoConexion(44, 136);
        this.agregarPuntoConexion(212, 150);

        this.evaluarEstado();
    }

    public CompuertaLogica(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false, false);
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
        if (terminalEntradaA != null && terminalEntradaA.estaConectado()) {
            this.entradaA = terminalEntradaA.getValorLogico();
        }
        if (terminalEntradaB != null && terminalEntradaB.estaConectado()) {
            this.entradaB = terminalEntradaB.getValorLogico();
        }
        this.salida = calcularSalida();
        if (terminalSalida != null) {
            terminalSalida.setValorLogico(this.salida);
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
