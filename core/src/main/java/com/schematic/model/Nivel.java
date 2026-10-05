package com.schematic.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Nivel {

    public static class CasillaInventario {
        private String tipo;
        private String titulo;
        private int cantidadDisponible;
        private int cantidadTotal;

        public CasillaInventario(String tipo, String titulo, int cantidadTotal) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.cantidadTotal = cantidadTotal;
            this.cantidadDisponible = cantidadTotal;
        }

        public String getTipo() { return tipo; }
        public String getTitulo() { return titulo; }
        public int getCantidadDisponible() { return cantidadDisponible; }
        public int getCantidadTotal() { return cantidadTotal; }

        public boolean puedeUsar() {
            return cantidadDisponible > 0;
        }

        public void decrementar() {
            if (cantidadDisponible > 0) {
                cantidadDisponible--;
            }
        }

        public void incrementar() {
            if (cantidadDisponible < cantidadTotal) {
                cantidadDisponible++;
            }
        }

        public void reiniciar() {
            this.cantidadDisponible = cantidadTotal;
        }
    }

    protected int numeroNivel;
    protected String tituloReto;
    protected float tiempoLimite;
    protected float tiempoRestante;
    protected boolean completado;
    protected boolean ganado;
    protected String mensajeEstado;

    protected List<Componente> componentes;
    protected List<Componente> componentesCinta;
    protected List<Cable> cables;
    protected List<CasillaInventario> inventario;

    public Nivel(int numeroNivel, String tituloReto, float tiempoLimite) {
        this.numeroNivel = numeroNivel;
        this.tituloReto = tituloReto;
        this.tiempoLimite = tiempoLimite > 0 ? tiempoLimite : 30.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.completado = false;
        this.ganado = false;
        this.mensajeEstado = "";
        this.componentes = new ArrayList<>();
        this.componentesCinta = new ArrayList<>();
        this.cables = new ArrayList<>();
        this.inventario = new ArrayList<>();
    }

    public abstract void inicializar();

    public void reiniciarNivelActual() {
        inicializar();
    }

    public abstract boolean evaluarCircuitoCompleto();

    public void actualizar(float deltaTiempo) {
        if (completado && ganado) {
            return;
        }

        if (tiempoRestante > 0) {
            tiempoRestante -= deltaTiempo;
            if (tiempoRestante <= 0) {
                tiempoRestante = 0;
            }
        }

        evaluarCircuitoCompleto();
    }

    public CasillaInventario buscarCasilla(String tipo) {
        for (CasillaInventario casilla : inventario) {
            if (casilla.getTipo().equals(tipo)) {
                return casilla;
            }
        }
        return null;
    }

    public Componente crearYColocarComponente(String tipo, float x, float y) {
        CasillaInventario casilla = buscarCasilla(tipo);
        if (casilla == null || !casilla.puedeUsar()) {
            return null;
        }

        Componente nuevo = fabricarComponentePorTipo(tipo, x, y);

        if (nuevo != null) {
            casilla.decrementar();
            agregarComponente(nuevo);
            actualizar(0f);
        }
        return nuevo;
    }

    protected Componente fabricarComponentePorTipo(String tipo, float x, float y) {
        if ("LED".equals(tipo)) {
            return new LED("LED_1", x, y, true);
        } else if ("RESISTENCIA_220".equals(tipo)) {
            return new Resistencia("R_1", x, y, 220, 220);
        } else if ("SWITCH".equals(tipo)) {
            return new Switch("SW_1", x, y, false);
        } else if ("AND".equals(tipo)) {
            return new CompuertaAND("AND_1", x, y, false, false);
        } else if ("OR".equals(tipo)) {
            return new CompuertaOR("OR_1", x, y, false, false);
        }
        return null;
    }

    public void devolverAlInventario(Componente comp) {
        if (comp == null || comp instanceof FuenteAlimentacion) return;

        if (comp instanceof LED) {
            CasillaInventario casilla = buscarCasilla("LED");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Resistencia) {
            CasillaInventario casilla = buscarCasilla("RESISTENCIA_220");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Switch) {
            CasillaInventario casilla = buscarCasilla("SWITCH");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof CompuertaAND) {
            CasillaInventario casilla = buscarCasilla("AND");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof CompuertaOR) {
            CasillaInventario casilla = buscarCasilla("OR");
            if (casilla != null) casilla.incrementar();
        }

        eliminarComponente(comp);
        actualizar(0f);
    }

    public Cable buscarCableConTerminal(Terminal terminal) {
        if (terminal == null) return null;
        for (Cable c : cables) {
            if (c.conectaTerminal(terminal)) {
                return c;
            }
        }
        return null;
    }

    public void agregarComponente(Componente componente) {
        if (componente != null && !componentes.contains(componente)) {
            this.componentes.add(componente);
            this.componentesCinta.remove(componente);
        }
    }

    public void eliminarComponente(Componente componente) {
        if (componente == null) return;
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
            actualizar(0f);
        }
    }

    public void eliminarCable(Cable cable) {
        if (cable != null) {
            cable.desconectar();
            this.cables.remove(cable);
            actualizar(0f);
        }
    }

    public int getNumeroNivel() { return numeroNivel; }
    public void setNumeroNivel(int numeroNivel) { this.numeroNivel = numeroNivel; }

    public String getTituloReto() { return tituloReto; }
    public void setTituloReto(String tituloReto) { this.tituloReto = tituloReto; }

    public float getTiempoLimite() { return tiempoLimite; }
    public void setTiempoLimite(float tiempoLimite) { this.tiempoLimite = tiempoLimite; }

    public float getTiempoRestante() { return tiempoRestante; }
    public void setTiempoRestante(float tiempoRestante) { this.tiempoRestante = tiempoRestante; }

    public boolean estaCompletado() { return completado; }
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public boolean estaGanado() { return ganado; }
    public boolean isGanado() { return ganado; }
    public void setGanado(boolean ganado) { this.ganado = ganado; }

    public String getMensajeEstado() { return mensajeEstado; }
    public void setMensajeEstado(String mensajeEstado) { this.mensajeEstado = mensajeEstado; }

    public List<Componente> getComponentes() { return componentes; }
    public List<Componente> getComponentesCinta() { return componentesCinta; }
    public List<Cable> getCables() { return cables; }
    public List<CasillaInventario> getInventario() { return inventario; }
}

