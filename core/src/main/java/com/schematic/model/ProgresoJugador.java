package com.schematic.model;

/**
 * Contenedor de datos encargado de estructurar la información del avance
 * registrado por el usuario en el juego.
 */
public class ProgresoJugador {

    private int nivelMaximoDesbloqueado;

    /**
     * Constructor por defecto.
     * Inicializa el progreso en el nivel 1. Requerido para la deserialización JSON de LibGDX.
     */
    public ProgresoJugador() {
        this.nivelMaximoDesbloqueado = 1;
    }

    /**
     * Constructor parametrizado.
     *
     * @param nivelMaximoDesbloqueado Nivel más alto que el jugador ha alcanzado o desbloqueado.
     */
    public ProgresoJugador(int nivelMaximoDesbloqueado) {
        this.nivelMaximoDesbloqueado = Math.max(1, nivelMaximoDesbloqueado);
    }

    public int getNivelMaximoDesbloqueado() {
        return nivelMaximoDesbloqueado;
    }

    public void setNivelMaximoDesbloqueado(int nivelMaximoDesbloqueado) {
        this.nivelMaximoDesbloqueado = Math.max(1, nivelMaximoDesbloqueado);
    }

    /**
     * Actualiza el nivel máximo alcanzado si el nuevo nivel supera al actual.
     *
     * @param nivel Nivel recién desbloqueado o completado.
     * @return true si se actualizó el progreso, false si el nivel ya estaba desbloqueado.
     */
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
