package model;

public abstract class Componente {

    protected String identificador;
    protected float posicionX;
    protected float posicionY;
    protected String estadoActual;

    public Componente(String identificador, float posicionX, float posicionY) {
        this.identificador = identificador;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.estadoActual = "NEUTRO";
    }

    public abstract boolean evaluarEstado();

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

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String toString() {
        return "Componente{" +
                "identificador='" + identificador + '\'' +
                ", posicionX=" + posicionX +
                ", posicionY=" + posicionY +
                ", estadoActual='" + estadoActual + '\'' +
                '}';
    }
}
