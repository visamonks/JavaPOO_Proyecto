package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel1 extends Nivel {

    public Nivel1() {
        super(1, "NIVEL 1: ENCIENDE EL LED", 30.0f);
        inicializar();
    }

    @Override
    public void inicializar() {
        this.componentes.clear();
        this.componentesCinta.clear();
        this.cables.clear();
        this.inventario.clear();
        this.completado = false;
        this.ganado = false;
        this.tiempoLimite = 30.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.mensajeEstado = "Objetivo: Prende esta led.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        this.componentes.add(fuente);

        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ω", 1));
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
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
}

