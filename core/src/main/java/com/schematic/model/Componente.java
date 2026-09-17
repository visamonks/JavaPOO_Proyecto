package com.schematic.model;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public abstract class Componente {

    protected String identificador;
    protected float posicionX;
    protected float posicionY;
    protected float ancho;
    protected float alto;
    protected String estadoActual;
    protected List<Terminal> terminales;

    public Componente(String identificador, float posicionX, float posicionY) {
        this.identificador = identificador;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.ancho = 60f;
        this.alto = 40f;
        this.estadoActual = "NEUTRO";
        this.terminales = new ArrayList<>();
    }

    public abstract boolean evaluarEstado();

    public void actualizarPosicion(float nuevaPosicionX, float nuevaPosicionY) {
        this.posicionX = nuevaPosicionX;
        this.posicionY = nuevaPosicionY;
    }

    public boolean contienePunto(float x, float y) {
        return x >= posicionX && x <= posicionX + ancho &&
               y >= posicionY && y <= posicionY + alto;
    }

    public Terminal buscarTerminalCercano(float x, float y, float radioTolerancia) {
        for (Terminal t : terminales) {
            Vector2 pos = t.getPosicionAbsoluta();
            if (pos.dst(x, y) <= radioTolerancia) {
                return t;
            }
        }
        return null;
    }

    public List<Terminal> getTerminales() {
        return terminales;
    }

    public float getAncho() {
        return ancho;
    }

    public void setAncho(float ancho) {
        this.ancho = ancho;
    }

    public float getAlto() {
        return alto;
    }

    public void setAlto(float alto) {
        this.alto = alto;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public float getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(float posicionX) {
        this.posicionX = posicionX;
    }

    public float getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(float posicionY) {
        this.posicionY = posicionY;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    @Override
    public String toString() {
        return "Componente{" +
                "identificador='" + identificador + '\'' +
                ", posicionX=" + posicionX +
                ", posicionY=" + posicionY +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
