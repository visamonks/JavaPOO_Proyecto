package com.schematic.model;

import com.badlogic.gdx.math.Vector2;

public class Terminal {

    private String identificador;
    private Componente componentePadre;
    private float offsetX;
    private float offsetY;
    private boolean esEntrada;
    private boolean valorLogico;
    private Cable cableConectado;

    public Terminal() {
        this.identificador = "TERM_DEF";
        this.componentePadre = null;
        this.offsetX = 0f;
        this.offsetY = 0f;
        this.esEntrada = true;
        this.valorLogico = false;
        this.cableConectado = null;
    }

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

    public void desconectarCable() {
        this.cableConectado = null;
    }

   
    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public Componente getComponentePadre() {
        return componentePadre;
    }

    public void setComponentePadre(Componente componentePadre) {
        this.componentePadre = componentePadre;
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

    public void setEsEntrada(boolean esEntrada) {
        this.esEntrada = esEntrada;
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
        return "Terminal{" +
                "id='" + identificador + '\'' +
                ", tipo=" + (esEntrada ? "ENTRADA" : "SALIDA") +
                ", valor=" + valorLogico +
                ", conectado=" + estaConectado() +
                '}';
    }
}   