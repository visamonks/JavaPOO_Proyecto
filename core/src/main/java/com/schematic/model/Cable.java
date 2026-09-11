package com.schematic.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cable {

    
    private final List<Point> puntosDeDoblado;
    
    
    private boolean tieneEnergia;

    public Cable() {
        this.puntosDeDoblado = new ArrayList<>();
        this.tieneEnergia = false;
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
                '}';
    }
}
