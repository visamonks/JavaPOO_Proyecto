package com.schematic.model;

public class Switch extends Componente {

    private boolean cerrado;
    private Terminal terminalSalida;

    public Switch(String identificador, float posicionX, float posicionY, boolean cerrado) {
        super(identificador, posicionX, posicionY);
        this.cerrado = cerrado;
        this.ancho = 60f;
        this.alto = 40f;

        this.terminalSalida = new Terminal(identificador + "_OUT", this, ancho, alto / 2f, false);
        this.terminales.add(this.terminalSalida);

        this.evaluarEstado();
    }

    public void conmutar() {
        this.cerrado = !this.cerrado;
        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        if (terminalSalida != null) {
            terminalSalida.setValorLogico(cerrado);
        }
        
        if (cerrado) {
            this.estadoActual = "EXITO";
            return true;
        } else {
            this.estadoActual = "NEUTRO";
            return false;
        }
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public void setCerrado(boolean cerrado) {
        this.cerrado = cerrado;
        this.evaluarEstado();
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