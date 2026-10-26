package com.schematic.model.componentes;

public class Switch extends Componente {

    private boolean cerrado;
    private Terminal terminalEntrada;
    private Terminal terminalSalida;

    public Switch(String identificador, float posicionX, float posicionY, boolean cerrado) {
        super(identificador, posicionX, posicionY, 240.0f, 140.0f);
        this.cerrado = cerrado;

        this.terminalEntrada = new Terminal(identificador + "_IN", this, 35.0f, 70.0f, true);
        this.terminalSalida = new Terminal(identificador + "_OUT", this, 205.0f, 70.0f, false);

        this.terminales.add(this.terminalEntrada);
        this.terminales.add(this.terminalSalida);

        this.agregarPuntoConexion(35, 70);
        this.agregarPuntoConexion(205, 70);

        this.evaluarEstado();
    }

    public Switch(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false);
    }

    public void conmutar() {
        this.cerrado = !this.cerrado;
        if (!this.cerrado) {
            this.estadoActual = "NEUTRO";
            if (terminalSalida != null) terminalSalida.setValorLogico(false);
        }
        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        if (!cerrado) {
            this.estadoActual = "NEUTRO";
            if (terminalSalida != null) {
                terminalSalida.setValorLogico(false);
            }
            return false;
        }

        boolean entradaActiva = (terminalEntrada != null && terminalEntrada.getValorLogico()) ||
                               (terminalSalida != null && terminalSalida.getValorLogico());

        if (entradaActiva) {
            if (terminalEntrada != null) terminalEntrada.setValorLogico(true);
            if (terminalSalida != null) terminalSalida.setValorLogico(true);
            this.estadoActual = "EXITO";
            return true;
        } else {
            this.estadoActual = "NEUTRO";
            if (terminalSalida != null) {
                terminalSalida.setValorLogico(false);
            }
            return false;
        }
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public void setCerrado(boolean cerrado) {
        this.cerrado = cerrado;
        if (!this.cerrado) {
            this.estadoActual = "NEUTRO";
            if (terminalSalida != null) terminalSalida.setValorLogico(false);
        }
        this.evaluarEstado();
    }

    public Terminal getTerminalEntrada() {
        return terminalEntrada;
    }

    public Terminal getTerminalSalida() {
        return terminalSalida;
    }

    @Override
    public String toString() {
        return "Switch{" +
                "identificador='" + identificador + '\'' +
                ", cerrado=" + cerrado +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
