package com.schematic.model;

import com.badlogic.gdx.math.Vector2;

public class FuenteAlimentacion extends Componente {

    private Terminal terminalPositivo;
    private Terminal terminalNegativo;
    private float voltaje;
    private boolean encendida;

    public FuenteAlimentacion(String identificador, float posicionX, float posicionY) {
        super(identificador, posicionX, posicionY, 220.0f, 260.0f);
        this.voltaje = 5.0f;
        this.encendida = true;

        this.terminalPositivo = new Terminal(identificador + "_POSITIVO", this, 200f, 190f, true);
        this.terminalNegativo = new Terminal(identificador + "_NEGATIVO", this, 200f, 70f, false);

        this.terminales.add(terminalPositivo);
        this.terminales.add(terminalNegativo);

        this.agregarPuntoConexion(200, 190);
        this.agregarPuntoConexion(200, 70);

        this.evaluarEstado();
    }

    @Override
    public boolean evaluarEstado() {
        if (terminalPositivo != null) {
            terminalPositivo.setValorLogico(encendida);
        }
        if (terminalNegativo != null) {
            terminalNegativo.setValorLogico(false);
        }
        return encendida;
    }

    public Terminal getTerminalPositivo() {
        return terminalPositivo;
    }

    public Terminal getTerminalNegativo() {
        return terminalNegativo;
    }

    public float getVoltaje() {
        return voltaje;
    }

    public void setVoltaje(float voltaje) {
        this.voltaje = voltaje;
    }

    public boolean estaEncendida() {
        return encendida;
    }

    public void setEncendida(boolean encendida) {
        this.encendida = encendida;
        this.evaluarEstado();
    }

    public Vector2 getPosicionTerminalPositivo() {
        return terminalPositivo.getPosicionAbsoluta();
    }

    public Vector2 getPosicionTerminalNegativo() {
        return terminalNegativo.getPosicionAbsoluta();
    }
}

