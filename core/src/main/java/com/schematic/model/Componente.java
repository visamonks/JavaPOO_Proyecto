package com.schematic.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Componente {

    protected String identificador;
    protected float posicionX;
    protected float posicionY;
    protected float ancho;
    protected float alto;
    protected String estadoActual;

    protected final List<Point> puntosConexion;

    public Componente(String identificador, float posicionX, float posicionY, float ancho, float alto) {
        this.identificador = identificador;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.ancho = ancho;
        this.alto = alto;
        this.estadoActual = "NEUTRO";
        this.puntosConexion = new ArrayList<>();
    }

    public abstract boolean evaluarEstado();

    public void agregarPuntoConexion(int offsetX, int offsetY) {
        this.puntosConexion.add(new Point(offsetX, offsetY));
    }

    public List<Point> getPuntosConexion() {
        return Collections.unmodifiableList(puntosConexion);
    }

    
    public Point getPosicionAbsolutaPunto(int indice) {
        if (indice >= 0 && indice < puntosConexion.size()) {
            Point p = puntosConexion.get(indice);
            return new Point((int) (posicionX + p.x), (int) (posicionY + p.y));
        }
        return null;
    }

    // getters y setters

    public void actualizarPosicion(float nuevaPosicionX, float nuevaPosicionY) {
        this.posicionX = nuevaPosicionX;
        this.posicionY = nuevaPosicionY;
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
                ", x=" + posicionX +
                ", y=" + posicionY +
                ", ancho=" + ancho +
                ", alto=" + alto +
                ", estado='" + estadoActual + '\'' +
                ", patitas=" + puntosConexion.size() +
                '}';
    }
}