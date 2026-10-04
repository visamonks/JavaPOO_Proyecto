package com.schematic.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cable {

    private final List<Point> puntosDeDoblado;
    private boolean tieneEnergia;
    private Terminal terminalOrigen;
    private Terminal terminalDestino;
    private float codoPersonalizadoX;
    private float codoPersonalizadoY;

    public Cable() {
        this.puntosDeDoblado = new ArrayList<>();
        this.tieneEnergia = false;
        this.terminalOrigen = null;
        this.terminalDestino = null;
        this.codoPersonalizadoX = Float.NaN;
        this.codoPersonalizadoY = Float.NaN;
    }

    public Cable(Terminal terminalOrigen, Terminal terminalDestino) {
        this();
        this.terminalOrigen = terminalOrigen;
        this.terminalDestino = terminalDestino;
        if (terminalOrigen != null) {
            terminalOrigen.setCableConectado(this);
        }
        if (terminalDestino != null) {
            terminalDestino.setCableConectado(this);
        }
    }

    public boolean conectaTerminal(Terminal t) {
        return t != null && (t == terminalOrigen || t == terminalDestino);
    }

    public Terminal getOtroTerminal(Terminal t) {
        if (t == terminalOrigen) return terminalDestino;
        if (t == terminalDestino) return terminalOrigen;
        return null;
    }

    public void propagarSenal() {
        if (terminalOrigen != null && terminalDestino != null) {
            boolean senalOrigen = terminalOrigen.getValorLogico();
            boolean senalDestino = terminalDestino.getValorLogico();

            if (senalOrigen || senalDestino) {
                this.tieneEnergia = true;
                terminalOrigen.setValorLogico(true);
                terminalDestino.setValorLogico(true);
            } else {
                this.tieneEnergia = false;
            }
        } else if (terminalOrigen != null) {
            this.tieneEnergia = terminalOrigen.getValorLogico();
        } else if (terminalDestino != null) {
            this.tieneEnergia = terminalDestino.getValorLogico();
        } else {
            this.tieneEnergia = false;
        }
    }

    public void desconectar() {
        if (terminalOrigen != null) {
            terminalOrigen.desconectarCable();
            terminalOrigen = null;
        }
        if (terminalDestino != null) {
            terminalDestino.desconectarCable();
            terminalDestino = null;
        }
        this.tieneEnergia = false;
    }

    public Terminal getTerminalOrigen() {
        return terminalOrigen;
    }

    public void setTerminalOrigen(Terminal terminalOrigen) {
        this.terminalOrigen = terminalOrigen;
    }

    public Terminal getTerminalDestino() {
        return terminalDestino;
    }

    public void setTerminalDestino(Terminal terminalDestino) {
        this.terminalDestino = terminalDestino;
    }

    public void agregarPunto(int x, int y) {
        this.puntosDeDoblado.add(new Point(x, y));
    }

    public void agregarPunto(Point punto) {
        if (punto != null) {
            this.puntosDeDoblado.add(new Point(punto));
        }
    }

    public boolean removerPunto(Point punto) {
        return this.puntosDeDoblado.remove(punto);
    }

    public void limpiarPuntos() {
        this.puntosDeDoblado.clear();
    }

    public List<Point> getPuntosDeDoblado() {
        return Collections.unmodifiableList(puntosDeDoblado);
    }

    public boolean isTieneEnergia() {
        return tieneEnergia;
    }

    public void setTieneEnergia(boolean tieneEnergia) {
        this.tieneEnergia = tieneEnergia;
    }

    public boolean tieneCodoPersonalizado() {
        return !Float.isNaN(codoPersonalizadoX) && !Float.isNaN(codoPersonalizadoY);
    }

    public float getCodoPersonalizadoX() {
        return codoPersonalizadoX;
    }

    public float getCodoPersonalizadoY() {
        return codoPersonalizadoY;
    }

    public void setCodoPersonalizado(float x, float y) {
        this.codoPersonalizadoX = x;
        this.codoPersonalizadoY = y;
    }

    public void limpiarCodoPersonalizado() {
        this.codoPersonalizadoX = Float.NaN;
        this.codoPersonalizadoY = Float.NaN;
    }

    @Override
    public String toString() {
        return "Cable{" +
                "puntos=" + puntosDeDoblado.size() +
                ", conEnergia=" + tieneEnergia +
                ", origen=" + (terminalOrigen != null ? terminalOrigen.getIdentificador() : "null") +
                ", destino=" + (terminalDestino != null ? terminalDestino.getIdentificador() : "null") +
                '}';
    }
}
