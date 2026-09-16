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
    private String mensajeEstado;
    private List<Componente> componentes;
    private List<Cable> cables;

    public Minijuego(float tiempoInicial) {
        this(1, "NIVEL 1: CIRCUITO BÁSICO DC", tiempoInicial);
    }

    public Minijuego(int numeroNivel, String tituloReto, float tiempoLimite) {
        this.numeroNivel = numeroNivel;
        this.tituloReto = tituloReto;
        this.tiempoLimite = tiempoLimite;
        this.tiempoRestante = tiempoLimite;
        this.completado = false;
        this.ganado = false;
        this.mensajeEstado = "Objetivo: Conecta la fuente, la resistencia y el LED para encenderlo.";
        this.componentes = new ArrayList<>();
        this.cables = new ArrayList<>();

        inicializarNivel1();
    }

    public void inicializarNivel1() {
        this.componentes.clear();
        this.cables.clear();
        this.completado = false;
        this.ganado = false;
        this.mensajeEstado = "Objetivo: Conecta la fuente, la resistencia y el LED para encenderlo.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 100f, 380f);
        Resistencia resistencia = new Resistencia("R_1", 600f, 440f, 220, 220);
        LED led = new LED("LED_1", 1150f, 380f, true);

        this.componentes.add(fuente);
        this.componentes.add(resistencia);
        this.componentes.add(led);
    }

    public void actualizar(float deltaTiempo) {
        if (completado && ganado) {
            return;
        }

        tiempoRestante -= deltaTiempo;
        evaluarCircuito();
    }

    public void agregarComponente(Componente componente) {
        if (componente != null && !componentes.contains(componente)) {
            this.componentes.add(componente);
            evaluarCircuito();
        }
    }

    public void agregarCable(Cable cable) {
        if (cable != null && !cables.contains(cable)) {
            this.cables.add(cable);
            evaluarCircuito();
        }
    }

    public void eliminarCable(Cable cable) {
        if (cable != null) {
            cable.desconectar();
            this.cables.remove(cable);
            evaluarCircuito();
        }
    }

    private Cable buscarCableConTerminal(Terminal t) {
        if (t == null) return null;
        for (Cable c : cables) {
            if (c.conectaTerminal(t)) {
                return c;
            }
        }
        return null;
    }

    public boolean evaluarCircuito() {
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

        if (fuente == null || led == null || res == null) {
            return false;
        }

        fuente.getTerminalPositivo().setValorLogico(fuente.estaEncendida());

        for (Cable c : cables) {
            if (c.conectaTerminal(fuente.getTerminalPositivo()) && c.conectaTerminal(fuente.getTerminalNegativo())) {
                fuente.setEstadoActual("ERROR");
                c.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO DIRECTO! Desconecta el cable entre (+) y (-).";
                return false;
            }
        }

        Cable cablePos = buscarCableConTerminal(fuente.getTerminalPositivo());
        if (cablePos == null) {
            fuente.setEstadoActual("NEUTRO");
            led.setQuemado(false);
            led.setEstadoActual("NEUTRO");
            res.setQuemada(false);
            res.setEstadoActual("NEUTRO");
            mensajeEstado = "Objetivo: Conecta la fuente, la resistencia y el LED para encenderlo.";
            this.ganado = false;
            this.completado = false;
            return false;
        }

        cablePos.setTieneEnergia(true);
        Terminal destinoPos = cablePos.getOtroTerminal(fuente.getTerminalPositivo());
        destinoPos.setValorLogico(true);

        if (destinoPos == led.getTerminalAnodo()) {
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
                } else if (destinoCat == res.getTerminalEntrada() || destinoCat == res.getTerminalSalida()) {
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
                        mensajeEstado = "¡CIRCUITO COMPLETO! El LED está encendido con éxito.";
                        this.ganado = true;
                        this.completado = true;
                        return true;
                    }
                }
            }
        } else if (destinoPos == res.getTerminalEntrada() || destinoPos == res.getTerminalSalida()) {
            Terminal salidaRes = (destinoPos == res.getTerminalEntrada()) ? res.getTerminalSalida() : res.getTerminalEntrada();
            destinoPos.setValorLogico(true);
            salidaRes.setValorLogico(true);

            Cable cableSalida = buscarCableConTerminal(salidaRes);
            if (cableSalida != null) {
                Terminal destinoSalida = cableSalida.getOtroTerminal(salidaRes);
                if (destinoSalida == led.getTerminalAnodo()) {
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
                        mensajeEstado = "¡CIRCUITO COMPLETO! El LED está encendido con éxito.";
                        this.ganado = true;
                        this.completado = true;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<Componente> getComponentes() {
        return componentes;
    }

    public List<Cable> getCables() {
        return cables;
    }

    public boolean estaGanado() {
        return ganado;
    }

    public boolean isCompletado() {
        return completado;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    public float getTiempoRestante() {
        return tiempoRestante;
    }

    public int getNumeroNivel() {
        return numeroNivel;
    }

    public String getTituloReto() {
        return tituloReto;
    }
}
