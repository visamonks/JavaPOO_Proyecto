package com.schematic.model;

public class LED extends Componente {

    private boolean polaridadCorrecta;
    private Terminal terminalAnodo;
    private Terminal terminalCatodo;
    private boolean quemado;

    public LED(String identificador, float posicionX, float posicionY, boolean polaridadCorrecta) {
        super(identificador, posicionX, posicionY, 320.0f, 320.0f);
        this.polaridadCorrecta = polaridadCorrecta;
        this.quemado = false;

        this.terminalAnodo = new Terminal(identificador + "_ANODO", this, 143f, 105f, true);
        this.terminalCatodo = new Terminal(identificador + "_CATODO", this, 172f, 105f, false);

        this.terminales.add(terminalAnodo);
        this.terminales.add(terminalCatodo);

        this.agregarPuntoConexion(143, 105);
        this.agregarPuntoConexion(172, 105);

        this.evaluarEstado();
    }

    public LED(String identificador, float posicionX, float posicionY) {
        this(identificador, posicionX, posicionY, true);
    }

    @Override
    public boolean evaluarEstado() {
        if (quemado) {
            this.estadoActual = "ERROR";
            if (terminalCatodo != null) {
                terminalCatodo.setValorLogico(false);
            }
            return false;
        }

        boolean recibeEnergia = terminalAnodo != null && terminalAnodo.getValorLogico();

        if (recibeEnergia) {
            if (polaridadCorrecta) {
                this.estadoActual = "EXITO";
                if (terminalCatodo != null) {
                    terminalCatodo.setValorLogico(true);
                }
                return true;
            } else {
                this.estadoActual = "ERROR";
                if (terminalCatodo != null) {
                    terminalCatodo.setValorLogico(false);
                }
                return false;
            }
        } else {
            this.estadoActual = "NEUTRO";
            if (terminalCatodo != null) {
                terminalCatodo.setValorLogico(false);
            }
            return false;
        }
    }

    public boolean tienePolaridadCorrecta() {
        return polaridadCorrecta;
    }

    public boolean isPolaridadCorrecta() {
        return polaridadCorrecta;
    }

    public void setPolaridadCorrecta(boolean polaridadCorrecta) {
        this.polaridadCorrecta = polaridadCorrecta;
        this.evaluarEstado();
    }

    public boolean estaQuemado() {
        return quemado;
    }

    public boolean isQuemado() {
        return quemado;
    }

    public void setQuemado(boolean quemado) {
        this.quemado = quemado;
        if (quemado) {
            this.estadoActual = "ERROR";
        }
    }

    public Terminal getTerminalAnodo() {
        return terminalAnodo;
    }

    public Terminal getTerminalCatodo() {
        return terminalCatodo;
    }

    @Override
    public String toString() {
        return "LED{" +
                "identificador='" + identificador + '\'' +
                ", polaridadCorrecta=" + polaridadCorrecta +
                ", quemado=" + quemado +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
