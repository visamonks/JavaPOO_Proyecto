package com.schematic.model.progreso;

public class ProgresoJugador {

    private int nivelMaximoDesbloqueado;

    public ProgresoJugador() {
        this.nivelMaximoDesbloqueado = 1;
    }

    public ProgresoJugador(int nivelMaximoDesbloqueado) {
        this.nivelMaximoDesbloqueado = Math.max(1, nivelMaximoDesbloqueado);
    }

    public int getNivelMaximoDesbloqueado() {
        return nivelMaximoDesbloqueado;
    }

    public void setNivelMaximoDesbloqueado(int nivelMaximoDesbloqueado) {
        this.nivelMaximoDesbloqueado = Math.max(1, nivelMaximoDesbloqueado);
    }

    public boolean desbloquearNivel(int nivel) {
        if (nivel > this.nivelMaximoDesbloqueado) {
            this.nivelMaximoDesbloqueado = nivel;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ProgresoJugador{" +
                "nivelMaximoDesbloqueado=" + nivelMaximoDesbloqueado +
                '}';
    }
}
