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

    public Cable() {
        this.puntosDeDoblado = new ArrayList<>();
        this.tieneEnergia = false;
        this.terminalOrigen = null;
        this.terminalDestino = null;
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

    public void propagarSenal() {
        if (terminalOrigen != null) {
            this.tieneEnergia = terminalOrigen.getValorLogico();
            if (terminalDestino != null) {
                terminalDestino.setValorLogico(this.tieneEnergia);
            }
        }
    }

    public void desconectar() {
        if (terminalOrigen != null) {
            terminalOrigen.desconectarCable();
            terminalOrigen = null;
        }
        if (terminalDestino != null) {
            terminalDestino.desconectarCable();
            terminalDestino.setValorLogico(false);
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
