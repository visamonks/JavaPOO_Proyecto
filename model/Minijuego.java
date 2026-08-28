package model;

import java.util.ArrayList;
import java.util.List;

public class Minijuego {

    private float tiempoRestante;
    private boolean completado;
    private List<Componente> componentes;

    public Minijuego(float tiempoInicial) {
        this.tiempoRestante = tiempoInicial;
        this.completado = false;
        this.componentes = new ArrayList<>();
    }

    public void actualizar(float deltaTiempo) {
        if (completado) {
            return;
        }

        tiempoRestante -= deltaTiempo;
        if (tiempoRestante <= 0) {
            tiempoRestante = 0;
            completado = true;
        }
    }

    public void agregarComponente(Componente componente) {
        if (componente != null) {
            this.componentes.add(componente);
        }
    }

    public float getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(float tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public List<Componente> getComponentes() {
        return componentes;
    }
}
