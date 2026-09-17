package com.schematic.model;

import java.util.ArrayList;
import java.util.List;

public class Minijuego {

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

    private int numeroNivel;
    private String tituloReto;
    private float tiempoLimite;
    private float tiempoRestante;
    private boolean completado;
    private boolean ganado;
    private String mensajeEstado;
    private List<Componente> componentes;
    private List<Componente> componentesCinta;
    private List<Cable> cables;
    private List<CasillaInventario> inventario;

    public Minijuego(float tiempoInicial) {
        this(1, "NIVEL 1: ENCIENDE EL LED", tiempoInicial);
    }

    public Minijuego(int numeroNivel, String tituloReto, float tiempoLimite) {
        this.numeroNivel = numeroNivel;
        this.tituloReto = tituloReto;
        this.tiempoLimite = tiempoLimite;
        this.tiempoRestante = tiempoLimite;
        this.completado = false;
        this.ganado = false;
        this.mensajeEstado = "Objetivo: Prende esta led.";
        this.componentes = new ArrayList<>();
        this.componentesCinta = new ArrayList<>();
        this.cables = new ArrayList<>();
        this.inventario = new ArrayList<>();

        if (numeroNivel == 1) {
            inicializarNivel1();
        } else {
            inicializarComponentesCinta();
        }
    }

    public void inicializarNivel1() {
        this.componentes.clear();
        this.componentesCinta.clear();
        this.cables.clear();
        this.inventario.clear();
        this.completado = false;
        this.ganado = false;
        this.mensajeEstado = "Objetivo: Prende esta led.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        this.componentes.add(fuente);

        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ω", 1));
    }

    public void inicializarComponentesCinta() {
        componentesCinta.clear();
        componentesCinta.add(new Switch("SW_1", 0, 0, true));
        componentesCinta.add(new CompuertaAND("AND_1", 0, 0, false, false));
        componentesCinta.add(new Resistencia("R_1", 0, 0, 220, 220));
        componentesCinta.add(new LED("LED_1", 0, 0, true));
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

        Componente nuevo = null;
        if ("LED".equals(tipo)) {
            nuevo = new LED("LED_1", x, y, true);
        } else if ("RESISTENCIA_220".equals(tipo)) {
            nuevo = new Resistencia("R_1", x, y, 220, 220);
        }

        if (nuevo != null) {
            casilla.decrementar();
            agregarComponente(nuevo);
            actualizar(0f);
        }
        return nuevo;
    }

    public void devolverAlInventario(Componente comp) {
        if (comp == null || comp instanceof FuenteAlimentacion) return;

        if (comp instanceof LED) {
            CasillaInventario casilla = buscarCasilla("LED");
            if (casilla != null) casilla.incrementar();
        } else if (comp instanceof Resistencia) {
            CasillaInventario casilla = buscarCasilla("RESISTENCIA_220");
            if (casilla != null) casilla.incrementar();
        }

        eliminarComponente(comp);
        actualizar(0f);
    }

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

    public Cable buscarCableConTerminal(Terminal terminal) {
        if (terminal == null) return null;
        for (Cable c : cables) {
            if (c.conectaTerminal(terminal)) {
                return c;
            }
        }
        return null;
    }

    public boolean evaluarCircuitoCompleto() {
        if (numeroNivel == 1) {
            return evaluarNivel1();
        }

        if (componentes.isEmpty()) {
            return false;
        }

        for (int iter = 0; iter < 2; iter++) {
            for (Cable c : cables) {
                c.propagarSenal();
            }
            for (Componente comp : componentes) {
                comp.evaluarEstado();
            }
        }

        boolean hayLED = false;
        for (Componente comp : componentes) {
            if (comp instanceof LED) {
                hayLED = true;
                if ("EXITO".equals(comp.getEstadoActual())) {
                    this.ganado = true;
                    this.completado = true;
                    return true;
                }
            }
        }

        return false;
    }

    private boolean evaluarNivel1() {
        FuenteAlimentacion fuente = null;
        LED led = null;
        Resistencia res = null;

        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof LED) led = (LED) c;
            else if (c instanceof Resistencia) res = (Resistencia) c;
        }

        for (Componente c : componentes) {
            for (Terminal t : c.getTerminales()) {
                t.setValorLogico(false);
            }
        }
        for (Cable c : cables) {
            c.setTieneEnergia(false);
        }

        if (fuente == null) {
            return false;
        }

        fuente.getTerminalPositivo().setValorLogico(fuente.estaEncendida());

        for (Cable c : cables) {
            if (c.conectaTerminal(fuente.getTerminalPositivo()) && c.conectaTerminal(fuente.getTerminalNegativo())) {
                fuente.setEstadoActual("ERROR");
                c.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO DIRECTO! La fuente de poder ha estallado.";
                return false;
            }
        }

        Cable cablePos = buscarCableConTerminal(fuente.getTerminalPositivo());
        if (cablePos == null) {
            fuente.setEstadoActual("NEUTRO");
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            mensajeEstado = "Objetivo: Prende esta led.";
            this.ganado = false;
            this.completado = false;
            return false;
        }

        cablePos.setTieneEnergia(true);
        Terminal destinoPos = cablePos.getOtroTerminal(fuente.getTerminalPositivo());
        destinoPos.setValorLogico(true);

        if (led != null && destinoPos == led.getTerminalAnodo()) {
            Cable cableCat = buscarCableConTerminal(led.getTerminalCatodo());
            if (cableCat != null) {
                Terminal destinoCat = cableCat.getOtroTerminal(led.getTerminalCatodo());

                if (destinoCat == fuente.getTerminalNegativo()) {
                    led.setQuemado(true);
                    led.setEstadoActual("ERROR");
                    fuente.setEstadoActual("ERROR");
                    cableCat.setTieneEnergia(true);
                    fuente.getTerminalNegativo().setValorLogico(true);
                    mensajeEstado = "¡EL LED SE HA QUEMADO! Conectaste a tierra sin resistencia limitadora.";
                    return false;
                } else if (res != null && (destinoCat == res.getTerminalEntrada() || destinoCat == res.getTerminalSalida())) {
                    Terminal salidaRes = (destinoCat == res.getTerminalEntrada()) ? res.getTerminalSalida() : res.getTerminalEntrada();
                    cableCat.setTieneEnergia(true);
                    destinoCat.setValorLogico(true);
                    salidaRes.setValorLogico(true);

                    Cable cableRet = buscarCableConTerminal(salidaRes);
                    if (cableRet != null && cableRet.conectaTerminal(fuente.getTerminalNegativo())) {
                        cableRet.setTieneEnergia(true);
                        fuente.getTerminalNegativo().setValorLogico(true);

                        led.setQuemado(false);
                        led.setEstadoActual("EXITO");
                        res.setQuemada(false);
                        res.setEstadoActual("EXITO");
                        fuente.setEstadoActual("EXITO");
                        mensajeEstado = "¡CIRCUITO COMPLETO! El LED está encendido.";
                        this.ganado = true;
                        this.completado = true;
                        return true;
                    } else {
                        led.setQuemado(false);
                        led.setEstadoActual("NEUTRO");
                        res.setQuemada(false);
                        res.setEstadoActual("NEUTRO");
                        fuente.setEstadoActual("NEUTRO");
                        mensajeEstado = "Conecta la resistencia hacia el polo negativo (-).";
                        return false;
                    }
                }
            } else {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
                mensajeEstado = "Conecta el cátodo (-) del LED hacia la resistencia.";
                return false;
            }
        } else if (res != null && (destinoPos == res.getTerminalEntrada() || destinoPos == res.getTerminalSalida())) {
            Terminal salidaRes = (destinoPos == res.getTerminalEntrada()) ? res.getTerminalSalida() : res.getTerminalEntrada();
            destinoPos.setValorLogico(true);
            salidaRes.setValorLogico(true);

            Cable cableSalida = buscarCableConTerminal(salidaRes);
            if (cableSalida != null) {
                Terminal destinoSalida = cableSalida.getOtroTerminal(salidaRes);
                if (led != null && destinoSalida == led.getTerminalAnodo()) {
                    cableSalida.setTieneEnergia(true);
                    led.getTerminalAnodo().setValorLogico(true);

                    Cable cableRet = buscarCableConTerminal(led.getTerminalCatodo());
                    if (cableRet != null && cableRet.conectaTerminal(fuente.getTerminalNegativo())) {
                        cableRet.setTieneEnergia(true);
                        fuente.getTerminalNegativo().setValorLogico(true);

                        led.setQuemado(false);
                        led.setEstadoActual("EXITO");
                        res.setQuemada(false);
                        res.setEstadoActual("EXITO");
                        fuente.setEstadoActual("EXITO");
                        mensajeEstado = "¡CIRCUITO COMPLETO! El LED está encendido.";
                        this.ganado = true;
                        this.completado = true;
                        return true;
                    } else {
                        cableSalida.setTieneEnergia(true);
                        led.setQuemado(false);
                        led.setEstadoActual("NEUTRO");
                        res.setQuemada(false);
                        res.setEstadoActual("NEUTRO");
                        fuente.setEstadoActual("NEUTRO");
                        mensajeEstado = "Conecta el cátodo del LED al polo negativo (-).";
                        return false;
                    }
                } else if (destinoSalida == fuente.getTerminalNegativo()) {
                    cableSalida.setTieneEnergia(true);
                    fuente.getTerminalNegativo().setValorLogico(true);
                    res.setQuemada(false);
                    res.setEstadoActual("EXITO");
                    fuente.setEstadoActual("NEUTRO");
                    mensajeEstado = "Corriente fluyendo por la resistencia. Falta colocar el LED.";
                    return false;
                }
            }
            res.setQuemada(false);
            res.setEstadoActual("NEUTRO");
            fuente.setEstadoActual("NEUTRO");
            mensajeEstado = "Conecta la salida de la resistencia hacia el LED.";
            return false;
        } else if (led != null && destinoPos == led.getTerminalCatodo()) {
            led.setQuemado(false);
            led.setEstadoActual("NEUTRO");
            fuente.setEstadoActual("NEUTRO");
            mensajeEstado = "Polaridad invertida: El positivo (+) debe conectarse al Ánodo del LED.";
            return false;
        }

        return false;
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

    @Override
    public String toString() {
        return "Minijuego{" +
                "numeroNivel=" + numeroNivel +
                ", tituloReto='" + tituloReto + '\'' +
                ", tiempoLimite=" + tiempoLimite +
                ", tiempoRestante=" + tiempoRestante +
                ", completado=" + completado +
                ", ganado=" + ganado +
                ", componentes=" + componentes.size() +
                ", cables=" + cables.size() +
                '}';
    }
}
