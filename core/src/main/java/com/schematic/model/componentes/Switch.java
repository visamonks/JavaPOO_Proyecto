package com.schematic.model.componentes;

public class Switch extends Componente {

    private boolean esDoble;
    private boolean cerrado;
    private boolean cerradoB;
    private Terminal terminalEntrada;
    private Terminal terminalSalida;
    private Terminal terminalSalidaB;

    public Switch(String identificador, float posicionX, float posicionY, boolean cerrado) {
        super(identificador, posicionX, posicionY, 190.0f, 110.0f);
        this.esDoble = false;
        this.cerrado = cerrado;
        this.cerradoB = false;

        this.terminalEntrada = new Terminal(identificador + "_IN", this, 28.0f, 55.0f, true);
        this.terminalSalida = new Terminal(identificador + "_OUT", this, 162.0f, 55.0f, false);
        this.terminalSalidaB = null;

        this.terminales.add(this.terminalEntrada);
        this.terminales.add(this.terminalSalida);

        this.agregarPuntoConexion(28, 55);
        this.agregarPuntoConexion(162, 55);

        this.evaluarEstado();
    }

    public Switch(String identificador, float posicionX, float posicionY, boolean cerradoA, boolean cerradoB) {
        super(identificador, posicionX, posicionY, 230.0f, 180.0f);
        this.esDoble = true;
        this.cerrado = cerradoA;
        this.cerradoB = cerradoB;

        this.terminalEntrada = new Terminal(identificador + "_IN", this, 28.0f, 90.0f, true);
        this.terminalSalida = new Terminal(identificador + "_OUT_A", this, 202.0f, 136.0f, false);
        this.terminalSalidaB = new Terminal(identificador + "_OUT_B", this, 202.0f, 44.0f, false);

        this.terminales.add(this.terminalEntrada);
        this.terminales.add(this.terminalSalida);
        this.terminales.add(this.terminalSalidaB);

        this.agregarPuntoConexion(28, 90);
        this.agregarPuntoConexion(202, 136);
        this.agregarPuntoConexion(202, 44);

        this.evaluarEstado();
    }

    public Switch(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, false);
    }

    public void conmutar() {
        this.cerrado = !this.cerrado;
        if (!this.cerrado && terminalSalida != null) {
            terminalSalida.setValorLogico(false);
        }
        this.evaluarEstado();
    }

    public void conmutarA() {
        conmutar();
    }

    public void conmutarB() {
        this.cerradoB = !this.cerradoB;
        if (!this.cerradoB && terminalSalidaB != null) {
            terminalSalidaB.setValorLogico(false);
        }
        this.evaluarEstado();
    }

    public void conmutarSegunPosicionY(float mundoY) {
        if (!esDoble) {
            conmutar();
        } else {
            if (mundoY >= this.posicionY + (this.alto * 0.5f)) {
                conmutarA();
            } else {
                conmutarB();
            }
        }
    }

    @Override
    public boolean evaluarEstado() {
        if (!esDoble) {
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
        } else {
            boolean inActiva = terminalEntrada != null && terminalEntrada.getValorLogico();
            if (terminalSalida != null) {
                terminalSalida.setValorLogico(inActiva && cerrado);
            }
            if (terminalSalidaB != null) {
                terminalSalidaB.setValorLogico(inActiva && cerradoB);
            }
            if (inActiva && (cerrado || cerradoB)) {
                this.estadoActual = "EXITO";
                return true;
            } else {
                this.estadoActual = "NEUTRO";
                return false;
            }
        }
    }

    public boolean isDoble() {
        return esDoble;
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public void setCerrado(boolean cerrado) {
        this.cerrado = cerrado;
        if (!this.cerrado && terminalSalida != null) {
            terminalSalida.setValorLogico(false);
        }
        this.evaluarEstado();
    }

    public boolean isCerradoA() {
        return cerrado;
    }

    public void setCerradoA(boolean cerradoA) {
        setCerrado(cerradoA);
    }

    public boolean isCerradoB() {
        return cerradoB;
    }

    public void setCerradoB(boolean cerradoB) {
        this.cerradoB = cerradoB;
        if (!this.cerradoB && terminalSalidaB != null) {
            terminalSalidaB.setValorLogico(false);
        }
        this.evaluarEstado();
    }

    public Terminal getTerminalEntrada() {
        return terminalEntrada;
    }

    public Terminal getTerminalSalida() {
        return terminalSalida;
    }

    public Terminal getTerminalSalidaA() {
        return terminalSalida;
    }

    public Terminal getTerminalSalidaB() {
        return terminalSalidaB;
    }

    @Override
    public String toString() {
        return "Switch{" +
                "identificador='" + identificador + '\'' +
                ", esDoble=" + esDoble +
                ", cerrado=" + cerrado +
                ", cerradoB=" + cerradoB +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
