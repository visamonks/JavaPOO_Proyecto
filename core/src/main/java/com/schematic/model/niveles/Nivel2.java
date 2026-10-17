package com.schematic.model.niveles;

import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nivel2 extends Nivel {

    public Nivel2() {
        super(2, "NIVEL 2: CIRCUITO CON INTERRUPTOR", 30.0f);
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
        this.mensajeEstado = "Objetivo: Conecta la fuente, el interruptor, la resistencia y el LED.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        Switch interruptor = new Switch("SW_1", 450f, 440f, false);
        Resistencia resistencia = new Resistencia("R_1", 800f, 395f, 220, 220);
        LED led = new LED("LED_1", 1240f, 350f, true);

        this.componentes.add(fuente);
        this.componentes.add(interruptor);
        this.componentes.add(resistencia);
        this.componentes.add(led);

        CasillaInventario casSw = new CasillaInventario("SWITCH", "Interruptor", 1);
        casSw.decrementar();
        this.inventario.add(casSw);

        CasillaInventario casRes = new CasillaInventario("RESISTENCIA_220", "220 Ω", 1);
        casRes.decrementar();
        this.inventario.add(casRes);

        CasillaInventario casLed = new CasillaInventario("LED", "LED (Diodo)", 1);
        casLed.decrementar();
        this.inventario.add(casLed);
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        FuenteAlimentacion fuente = null;
        Switch interruptor = null;
        Resistencia res = null;
        LED led = null;

        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) interruptor = (Switch) c;
            else if (c instanceof Resistencia) res = (Resistencia) c;
            else if (c instanceof LED) led = (LED) c;
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

        // Cortocircuito directo entre terminales de la fuente
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
            if (interruptor != null) interruptor.setEstadoActual("NEUTRO");
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            mensajeEstado = "Objetivo: Conecta la fuente, el interruptor, la resistencia y el LED.";
            this.ganado = false;
            this.completado = false;
            return false;
        }

        // Trazado del circuito serie a partir de la fuente positiva
        Terminal terminalActual = fuente.getTerminalPositivo();
        boolean pasoPorSwitch = false;
        boolean pasoPorResistencia = false;
        boolean pasoPorLed = false;
        boolean switchAbiertoEnCamino = false;
        boolean llegoATierra = false;
        Set<Componente> componentesVisitados = new HashSet<>();

        while (true) {
            Cable cableSaliente = buscarCableConTerminal(terminalActual);
            if (cableSaliente == null) {
                break;
            }

            cableSaliente.setTieneEnergia(!switchAbiertoEnCamino);
            Terminal destino = cableSaliente.getOtroTerminal(terminalActual);
            destino.setValorLogico(!switchAbiertoEnCamino);

            Componente compDestino = destino.getComponentePadre();
            if (compDestino == null) {
                break;
            }

            if (compDestino == fuente) {
                if (destino == fuente.getTerminalNegativo()) {
                    llegoATierra = true;
                    fuente.getTerminalNegativo().setValorLogico(!switchAbiertoEnCamino);
                }
                break;
            }

            if (componentesVisitados.contains(compDestino)) {
                // Evitar ciclos infinitos
                break;
            }
            componentesVisitados.add(compDestino);

            if (compDestino instanceof Switch) {
                Switch sw = (Switch) compDestino;
                pasoPorSwitch = true;
                Terminal salidaSw = (destino == sw.getTerminalEntrada()) ? sw.getTerminalSalida() : sw.getTerminalEntrada();

                if (!sw.isCerrado()) {
                    switchAbiertoEnCamino = true;
                }
                salidaSw.setValorLogico(!switchAbiertoEnCamino);
                terminalActual = salidaSw;

            } else if (compDestino instanceof Resistencia) {
                Resistencia r = (Resistencia) compDestino;
                pasoPorResistencia = true;
                Terminal salidaR = (destino == r.getTerminalEntrada()) ? r.getTerminalSalida() : r.getTerminalEntrada();
                salidaR.setValorLogico(!switchAbiertoEnCamino);
                terminalActual = salidaR;

            } else if (compDestino instanceof LED) {
                LED l = (LED) compDestino;
                if (destino == l.getTerminalCatodo()) {
                    l.setQuemado(false);
                    l.setEstadoActual("NEUTRO");
                    fuente.setEstadoActual("NEUTRO");
                    mensajeEstado = "Polaridad invertida: La corriente debe entrar por el Ánodo (+) del LED.";
                    return false;
                }
                pasoPorLed = true;
                Terminal catodo = l.getTerminalCatodo();
                catodo.setValorLogico(!switchAbiertoEnCamino);
                terminalActual = catodo;
            } else {
                break;
            }
        }

        // Evaluar resultado de la conexión
        if (llegoATierra) {
            if (pasoPorLed && !pasoPorResistencia) {
                if (led != null) {
                    led.setQuemado(true);
                    led.setEstadoActual("ERROR");
                }
                fuente.setEstadoActual("ERROR");
                mensajeEstado = "¡EL LED SE HA QUEMADO! Conectaste a tierra sin resistencia limitadora.";
                return false;
            }

            if (!pasoPorSwitch) {
                mensajeEstado = "Falta intercalar el interruptor (Switch) en el circuito.";
                return false;
            }

            if (!pasoPorLed) {
                mensajeEstado = "Falta intercalar el LED en el circuito.";
                return false;
            }

            if (pasoPorSwitch && pasoPorResistencia && pasoPorLed) {
                if (switchAbiertoEnCamino) {
                    fuente.setEstadoActual("NEUTRO");
                    if (interruptor != null) interruptor.setEstadoActual("NEUTRO");
                    if (res != null) {
                        res.setQuemada(false);
                        res.setEstadoActual("NEUTRO");
                    }
                    if (led != null) {
                        led.setQuemado(false);
                        led.setEstadoActual("NEUTRO");
                    }
                    mensajeEstado = "Circuito conectado. Haz clic sobre el interruptor para cerrarlo y encender el LED.";
                    this.ganado = false;
                    this.completado = false;
                    return false;
                } else {
                    fuente.setEstadoActual("EXITO");
                    if (interruptor != null) interruptor.setEstadoActual("EXITO");
                    if (res != null) {
                        res.setQuemada(false);
                        res.setEstadoActual("EXITO");
                    }
                    if (led != null) {
                        led.setQuemado(false);
                        led.setEstadoActual("EXITO");
                    }
                    mensajeEstado = "¡CIRCUITO COMPLETO! El LED está encendido.";
                    this.ganado = true;
                    this.completado = true;
                    return true;
                }
            }
        }

        // Si aún no llega a tierra, proveer ayuda de guiado según el último componente
        Componente ultimo = terminalActual.getComponentePadre();
        if (ultimo instanceof Switch) {
            mensajeEstado = "Conecta la salida del Switch hacia el resto de componentes.";
        } else if (ultimo instanceof Resistencia) {
            mensajeEstado = "Conecta la resistencia hacia el siguiente componente o a tierra (-).";
        } else if (ultimo instanceof LED) {
            mensajeEstado = "Conecta el cátodo (-) del LED hacia el siguiente componente o a tierra (-).";
        } else {
            mensajeEstado = "Conecta los componentes en serie y cierra el circuito al polo (-).";
        }

        this.ganado = false;
        this.completado = false;
        return false;
    }
}
