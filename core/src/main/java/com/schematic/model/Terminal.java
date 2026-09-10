package com.schematic.model;

import com.badlogic.gdx.math.Vector2;

public class Terminal {

    public Terminal() {
    private String identificador;
    private Componente componentePadre;
    private float offsetX;
    private float offsetY;
    private boolean esEntrada;
    private boolean valorLogico;
    private Cable cableConectado;

    public Terminal(String identificador, Componente componentePadre, float offsetX, float offsetY, boolean esEntrada) {
        this.identificador = identificador;
        this.componentePadre = componentePadre;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.esEntrada = esEntrada;
        this.valorLogico = false;
        this.cableConectado = null;
    }

    public Vector2 getPosicionAbsoluta() {
        if (componentePadre == null) {
            return new Vector2(offsetX, offsetY);
        }
        return new Vector2(componentePadre.getPosicionX() + offsetX, componentePadre.getPosicionY() + offsetY);
    }

    public boolean estaConectado() {
        return cableConectado != null;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Componente getComponentePadre() {
        return componentePadre;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public boolean isEsEntrada() {
        return esEntrada;
    }

    public boolean getValorLogico() {
        return valorLogico;
    }

    public void setValorLogico(boolean valorLogico) {
        this.valorLogico = valorLogico;
    }

    public Cable getCableConectado() {
        return cableConectado;
    }

    public void setCableConectado(Cable cableConectado) {
        this.cableConectado = cableConectado;
    }

    @Override
    public String toString() {
        return "Terminal{}";
        return "Terminal{" +
                "id='" + identificador + '\'' +
                ", tipo=" + (esEntrada ? "ENTRADA" : "SALIDA") +
                ", valor=" + valorLogico +
                '}';
    }
}

/*
 que llevamos
  Esta clase sirve para representar las patitas o pines de conexión que tendrá
  cada componente (como la entrada y la salida de una compuerta o los dos lados
  de un foco). De momento la dejamos creada como estructura base.
 
 que falta
  Le agregaremos las coordenadas exactas de cada patita dentro del dibujo de la
  pieza, si es una entrada o salida, y el valor de energía que tiene en ese
  momento para poder unirla con un cable.

 */
