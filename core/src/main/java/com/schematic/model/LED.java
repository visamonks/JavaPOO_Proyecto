package com.schematic.model;

public class LED extends Componente {

    private boolean polaridadCorrecta;
    private Terminal terminalAnodo;
    private Terminal terminalCatodo;

    public LED(String identificador, float posicionX, float posicionY, boolean polaridadCorrecta) {
        super(identificador, posicionX, posicionY);
        this.polaridadCorrecta = polaridadCorrecta;
        this.ancho = 50f;
        this.alto = 50f;

        this.terminalAnodo = new Terminal(identificador + "_ANODO", this, 0f, alto / 2f, true);
        this.terminalCatodo = new Terminal(identificador + "_CATODO", this, ancho, alto / 2f, false);

        this.terminales.add(terminalAnodo);
        this.terminales.add(terminalCatodo);

        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        boolean recibeEnergia = terminalAnodo != null && terminalAnodo.getValorLogico();
        if (recibeEnergia && polaridadCorrecta) {
            this.estadoActual = "EXITO";
            if (terminalCatodo != null) {
                terminalCatodo.setValorLogico(true);
            }
            return true;
        } else if (recibeEnergia && !polaridadCorrecta) {
            this.estadoActual = "ERROR";
            if (terminalCatodo != null) {
                terminalCatodo.setValorLogico(false);
            }
            return false;
        } else {
            this.estadoActual = "NEUTRO";
            if (terminalCatodo != null) {
                terminalCatodo.setValorLogico(false);
            }
            return false;
        }
    }

    public boolean isPolaridadCorrecta() {
        return polaridadCorrecta;
    }

    public void setPolaridadCorrecta(boolean polaridadCorrecta) {
        this.polaridadCorrecta = polaridadCorrecta;
        this.evaluarEstado();
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
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
