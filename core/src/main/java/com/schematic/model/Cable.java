package com.schematic.model;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Cable {

    private Terminal terminalOrigen;
    private Terminal terminalDestino;
    private List<Vector2> puntos;
    private boolean activo;

    public Cable() {
        this(null, null);
    }

    public Cable(Terminal terminalOrigen, Terminal terminalDestino) {
        this.terminalOrigen = terminalOrigen;
        this.terminalDestino = terminalDestino;
        this.puntos = new ArrayList<>();
        this.activo = false;
        if (terminalOrigen != null) {
            terminalOrigen.setCableConectado(this);
        }
        if (terminalDestino != null) {
            terminalDestino.setCableConectado(this);
        }
        actualizarRuta();
    }

    public static List<Vector2> calcularRutaOrtogonal(Vector2 inicio, Vector2 fin) {
        List<Vector2> ruta = new ArrayList<>();
        ruta.add(new Vector2(inicio.x, inicio.y));

        float midX = (inicio.x + fin.x) / 2f;
        ruta.add(new Vector2(midX, inicio.y));
        ruta.add(new Vector2(midX, fin.y));
        ruta.add(new Vector2(fin.x, fin.y));

        return ruta;
    }

    public void actualizarRuta() {
        if (terminalOrigen == null || terminalDestino == null) {
            return;
        }
        Vector2 posOrigen = terminalOrigen.getPosicionAbsoluta();
        Vector2 posDestino = terminalDestino.getPosicionAbsoluta();
        this.puntos = calcularRutaOrtogonal(posOrigen, posDestino);
    }

    public void propagarSenal() {
        if (terminalOrigen != null) {
            this.activo = terminalOrigen.getValorLogico();
        } else {
            this.activo = false;
        }

        if (terminalDestino != null) {
            terminalDestino.setValorLogico(this.activo);
            if (terminalDestino.getComponentePadre() != null) {
                terminalDestino.getComponentePadre().evaluarEstado();
            }
        }
    }

    public void desconectar() {
        if (terminalOrigen != null && terminalOrigen.getCableConectado() == this) {
            terminalOrigen.setCableConectado(null);
        }
        if (terminalDestino != null && terminalDestino.getCableConectado() == this) {
            terminalDestino.setCableConectado(null);
            terminalDestino.setValorLogico(false);
            if (terminalDestino.getComponentePadre() != null) {
                terminalDestino.getComponentePadre().evaluarEstado();
            }
        }
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

    public List<Vector2> getPuntos() {
        return puntos;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Cable{" +
                "origen=" + (terminalOrigen != null ? terminalOrigen.getIdentificador() : "null") +
                ", destino=" + (terminalDestino != null ? terminalDestino.getIdentificador() : "null") +
                ", activo=" + activo +
                ", puntos=" + puntos.size() +
                '}';
    }
}
