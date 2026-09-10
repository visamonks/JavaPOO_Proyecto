package com.schematic.model;

import java.util.ArrayList;
import java.util.List;

public class Minijuego {

    private int numeroNivel;
    private String tituloReto;
    private float tiempoLimite;
    private float tiempoRestante;
    private boolean completado;
    private boolean ganado;
    private List<Componente> componentes;
    private List<Componente> componentesCinta;
    private List<Cable> cables;

    public Minijuego(float tiempoInicial) {
        this(1, "Reto: ¡Conecta el circuito y enciende el LED!", tiempoInicial);
    }

    public Minijuego(int numeroNivel, String tituloReto, float tiempoLimite) {
        this.numeroNivel = numeroNivel;
        this.tituloReto = tituloReto;
        this.tiempoLimite = tiempoLimite;
        this.tiempoRestante = tiempoLimite;
        this.tiempoRestante = tiempoInicial;
        this.completado = false;
        this.ganado = false;
        this.componentes = new ArrayList<>();
        this.componentesCinta = new ArrayList<>();
        this.cables = new ArrayList<>();

        inicializarComponentesCinta();
    }

    public void inicializarComponentesCinta() {
        componentesCinta.clear();
        // Suministro de piezas para la cinta transportadora:
        componentesCinta.add(new Switch("SW_1", 0, 0, true));
        componentesCinta.add(new CompuertaAND("AND_1", 0, 0, false, false));
        componentesCinta.add(new Resistencia("R_1", 0, 0, 220, 220));
        componentesCinta.add(new LED("LED_1", 0, 0, true));
    }

    public void actualizar(float deltaTiempo) {
        if (completado) {
            return;
        }

        tiempoRestante -= deltaTiempo;

        // Propagación de señal eléctrica por todos los cables
        for (int iter = 0; iter < 2; iter++) {
            for (Cable c : cables) {
                c.propagarSenal();
            }
            for (Componente comp : componentes) {
                comp.evaluarEstado();
            }
        }

        // Evaluar si se cumplió el objetivo del nivel
        if (evaluarCircuitoCompleto()) {
            this.ganado = true;
            this.completado = true;
        } else if (tiempoRestante <= 0) {
        if (tiempoRestante <= 0) {
            tiempoRestante = 0;
            this.completado = true;
            this.ganado = false;
            completado = true;
        }
    }

    public void agregarComponente(Componente componente) {
        if (componente != null && !componentes.contains(componente)) {
        if (componente != null) {
            this.componentes.add(componente);
            this.componentesCinta.remove(componente);
        }
    }

    public void eliminarComponente(Componente componente) {
        if (componente == null) return;
        // Desconectar y remover cables asociados a esta pieza
        List<Cable> cablesAEliminar = new ArrayList<>();
        for (Cable cable : cables) {
            if ((cable.getTerminalOrigen() != null && cable.getTerminalOrigen().getComponentePadre() == componente) ||
                (cable.getTerminalDestino() != null && cable.getTerminalDestino().getComponentePadre() == componente)) {
                cablesAEliminar.add(cable);
            }
        }
        for (Cable cable : cablesAEliminar) {
            eliminarCable(cable);
        }
        this.componentes.remove(componente);
    }

    public void agregarCable(Cable cable) {
        if (cable != null && !cables.contains(cable)) {
            this.cables.add(cable);
            cable.propagarSenal();
        }
    }

    public void eliminarCable(Cable cable) {
        if (cable != null) {
            cable.desconectar();
            this.cables.remove(cable);
        }
    }

    public boolean evaluarCircuitoCompleto() {
        if (componentes.isEmpty()) {
            return false;
        }
        // El objetivo se cumple si al menos un LED colocado en el grid está encendido ("EXITO")
        boolean hayLED = false;
        for (Componente comp : componentes) {
            if (comp instanceof LED) {
                hayLED = true;
                if ("EXITO".equals(comp.getEstadoActual())) {
                    return true;
                }
            }
        }
        if (!hayLED) {
            // Si no hay LED, verificar que todos los componentes colocados estén en éxito
            for (Componente comp : componentes) {
                if (!"EXITO".equals(comp.getEstadoActual())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int getNumeroNivel() {
        return numeroNivel;
    }

    public void setNumeroNivel(int numeroNivel) {
        this.numeroNivel = numeroNivel;
    }

    public String getTituloReto() {
        return tituloReto;
    }

    public void setTituloReto(String tituloReto) {
        this.tituloReto = tituloReto;
    }

    public float getTiempoLimite() {
        return tiempoLimite;
    }

    public void setTiempoLimite(float tiempoLimite) {
        this.tiempoLimite = tiempoLimite;
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

    public boolean isGanado() {
        return ganado;
    }

    public void setGanado(boolean ganado) {
        this.ganado = ganado;
    }

    public List<Componente> getComponentes() {
        return componentes;
    }

    public List<Componente> getComponentesCinta() {
        return componentesCinta;
    }

    public List<Cable> getCables() {
        return cables;
    }

    @Override
    public String toString() {
        return "Minijuego{" +
                "numeroNivel=" + numeroNivel +
                ", tituloReto='" + tituloReto + '\'' +
                ", tiempoLimite=" + tiempoLimite +
                ", tiempoRestante=" + tiempoRestante +
                "tiempoRestante=" + tiempoRestante +
                ", completado=" + completado +
                ", ganado=" + ganado +
                ", componentes=" + componentes +
                '}';
    }
}

/*
Que hay ahorita:
Controla el tiempo que queda en el nivel y guarda la lista de piezas que estan en la pantalla.

Que falta:
Tener la lista de piezas que vienen en la cinta lateral y verificar si ya ganaste el nivel al encender el circuito.

Recomendaciones:
Toda la logica de reglas y victoria debe estar  aqui para mantener el modelo ordenado.
*/

