package com.schematic.model.niveles;

import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nivel3 extends Nivel {

    public Nivel3() {
        super(3, "NIVEL 3: DOBLE INTERRUPTOR (LÓGICA AND)", 30.0f);
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
        this.tiempoCircuitoActivo = 0.0f;
        this.tiempoLimite = 30.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.mensajeEstado = "Objetivo: Conecta la fuente, ambos interruptores en serie (Lógica AND), la resistencia y el LED.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        Switch sw1 = new Switch("SW_1", 380f, 560f, false);
        Switch sw2 = new Switch("SW_2", 700f, 560f, false);
        Resistencia resistencia = new Resistencia("R_1", 1060f, 515f, 220, 220);
        LED led = new LED("LED_1", 1100f, 160f, true);

        this.componentes.add(fuente);
        this.componentes.add(sw1);
        this.componentes.add(sw2);
        this.componentes.add(resistencia);
        this.componentes.add(led);

        CasillaInventario casSw = new CasillaInventario("SWITCH", "Interruptor", 2);
        casSw.decrementar();
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
        List<Switch> switches = new ArrayList<>();
        Resistencia res = null;
        LED led = null;

        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) switches.add((Switch) c);
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
            for (Switch s : switches) s.setEstadoActual("NEUTRO");
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            mensajeEstado = "Objetivo: Conecta la fuente, ambos interruptores en serie (Lógica AND), la resistencia y el LED.";
            this.ganado = false;
            this.completado = false;
            return false;
        }

        // Trazado del camino serie a partir de la fuente positiva
        Terminal terminalActual = fuente.getTerminalPositivo();
        Set<Switch> switchesVisitados = new HashSet<>();
        boolean pasoPorResistencia = false;
        boolean pasoPorLed = false;
        boolean llegoATierra = false;
        boolean polaridadInvertida = false;
        Set<Switch> switchesAbiertos = new HashSet<>();

        Set<Componente> componentesVisitados = new HashSet<>();
        List<Cable> cablesEnCamino = new ArrayList<>();

        while (true) {
            Cable cableSaliente = buscarCableConTerminal(terminalActual);
            if (cableSaliente == null) {
                break;
            }
            cablesEnCamino.add(cableSaliente);

            Terminal destino = cableSaliente.getOtroTerminal(terminalActual);
            if (destino == null) break;
            Componente compDestino = destino.getComponentePadre();
            if (compDestino == null) break;

            if (compDestino == fuente) {
                if (destino == fuente.getTerminalNegativo()) {
                    llegoATierra = true;
                }
                break;
            }

            if (componentesVisitados.contains(compDestino)) {
                // Ciclo detectado
                break;
            }
            componentesVisitados.add(compDestino);

            if (compDestino instanceof Switch) {
                Switch sw = (Switch) compDestino;
                switchesVisitados.add(sw);
                if (!sw.isCerrado()) {
                    switchesAbiertos.add(sw);
                }
                Terminal salidaSw = (destino == sw.getTerminalEntrada()) ? sw.getTerminalSalida() : sw.getTerminalEntrada();
                terminalActual = salidaSw;

            } else if (compDestino instanceof Resistencia) {
                Resistencia r = (Resistencia) compDestino;
                pasoPorResistencia = true;
                Terminal salidaR = (destino == r.getTerminalEntrada()) ? r.getTerminalSalida() : r.getTerminalEntrada();
                terminalActual = salidaR;

            } else if (compDestino instanceof LED) {
                LED l = (LED) compDestino;
                if (destino == l.getTerminalCatodo()) {
                    polaridadInvertida = true;
                    break;
                }
                pasoPorLed = true;
                terminalActual = l.getTerminalCatodo();
            } else {
                break;
            }
        }

        if (polaridadInvertida) {
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            fuente.setEstadoActual("NEUTRO");
            for (Switch s : switches) s.setEstadoActual("NEUTRO");
            if (res != null) res.setEstadoActual("NEUTRO");
            for (Cable c : cables) c.setTieneEnergia(false);
            mensajeEstado = "Polaridad invertida: La corriente debe entrar por el Ánodo (+) del LED.";
            return false;
        }

        if (llegoATierra) {
            if (pasoPorLed && !pasoPorResistencia) {
                if (led != null) {
                    led.setQuemado(true);
                    led.setEstadoActual("ERROR");
                }
                fuente.setEstadoActual("ERROR");
                for (Cable c : cablesEnCamino) {
                    c.setTieneEnergia(true);
                }
                mensajeEstado = "¡EL LED SE HA QUEMADO! Conectaste a tierra sin resistencia limitadora.";
                return false;
            }

            if (switchesVisitados.size() < 2) {
                fuente.setEstadoActual("NEUTRO");
                for (Switch s : switches) s.setEstadoActual("NEUTRO");
                if (res != null) res.setEstadoActual("NEUTRO");
                if (led != null) led.setEstadoActual("NEUTRO");
                for (Cable c : cables) c.setTieneEnergia(false);
                if (switchesVisitados.isEmpty()) {
                    mensajeEstado = "Falta intercalar los dos interruptores (Switches) en el circuito.";
                } else {
                    mensajeEstado = "Falta intercalar el segundo interruptor. Ambos deben estar en serie (Lógica AND).";
                }
                return false;
            }

            if (!pasoPorLed) {
                fuente.setEstadoActual("NEUTRO");
                for (Switch s : switches) s.setEstadoActual("NEUTRO");
                if (res != null) res.setEstadoActual("NEUTRO");
                for (Cable c : cables) c.setTieneEnergia(false);
                mensajeEstado = "Falta intercalar el LED en el circuito.";
                return false;
            }

            if (switchesVisitados.size() >= 2 && pasoPorResistencia && pasoPorLed) {
                if (!switchesAbiertos.isEmpty()) {
                    fuente.setEstadoActual("NEUTRO");
                    for (Switch s : switches) s.setEstadoActual("NEUTRO");
                    if (res != null) {
                        res.setQuemada(false);
                        res.setEstadoActual("NEUTRO");
                    }
                    if (led != null) {
                        led.setQuemado(false);
                        led.setEstadoActual("NEUTRO");
                    }
                    for (Cable c : cables) {
                        c.setTieneEnergia(false);
                    }

                    if (switchesAbiertos.size() >= 2) {
                        mensajeEstado = "Circuito conectado. Ambos interruptores están abiertos. Ciérralos para encender el LED.";
                    } else {
                        Switch abierto = switchesAbiertos.iterator().next();
                        String nombre = "SW_1".equals(abierto.getIdentificador()) ? "Interruptor A" :
                                       ("SW_2".equals(abierto.getIdentificador()) ? "Interruptor B" : abierto.getIdentificador());
                        mensajeEstado = String.format("El %s está abierto. Ciérralo para completar la condición AND.", nombre);
                    }
                    return false;
                } else {
                    fuente.setEstadoActual("EXITO");
                    for (Switch s : switchesVisitados) s.setEstadoActual("EXITO");
                    if (res != null) {
                        res.setQuemada(false);
                        res.setEstadoActual("EXITO");
                    }
                    if (led != null) {
                        led.setQuemado(false);
                        led.setEstadoActual("EXITO");
                    }
                    for (Cable c : cablesEnCamino) {
                        c.setTieneEnergia(true);
                    }

                    fuente.getTerminalPositivo().setValorLogico(true);
                    fuente.getTerminalNegativo().setValorLogico(true);
                    for (Switch s : switchesVisitados) {
                        s.getTerminalEntrada().setValorLogico(true);
                        s.getTerminalSalida().setValorLogico(true);
                    }
                    if (res != null) {
                        res.getTerminalEntrada().setValorLogico(true);
                        res.getTerminalSalida().setValorLogico(true);
                    }
                    if (led != null) {
                        led.getTerminalAnodo().setValorLogico(true);
                        led.getTerminalCatodo().setValorLogico(true);
                    }

                    if (tiempoCircuitoActivo >= TIEMPO_REQUERIDO_VICTORIA) {
                        mensajeEstado = "¡CIRCUITO COMPLETO! Lógica AND superada: el LED se mantuvo encendido 5 segundos. ¡Nivel superado!";
                    } else {
                        mensajeEstado = String.format("¡CIRCUITO ACTIVO! Mantén ambos interruptores cerrados: %.1f / %.1f s", tiempoCircuitoActivo, TIEMPO_REQUERIDO_VICTORIA);
                    }
                    return true;
                }
            }
        }

        // Si no ha llegado a tierra (circuito incompleto o abierto)
        fuente.setEstadoActual("NEUTRO");
        for (Switch s : switches) s.setEstadoActual("NEUTRO");
        if (res != null && !res.isQuemada()) res.setEstadoActual("NEUTRO");
        if (led != null && !led.isQuemado()) led.setEstadoActual("NEUTRO");
        for (Cable c : cables) {
            c.setTieneEnergia(false);
        }

        Componente ultimo = (terminalActual != null) ? terminalActual.getComponentePadre() : null;
        if (ultimo instanceof Switch) {
            if (switchesVisitados.size() < 2) {
                mensajeEstado = "Conecta la salida del Interruptor A hacia el Interruptor B en serie.";
            } else {
                mensajeEstado = "Conecta la salida del interruptor hacia la resistencia limitadora.";
            }
        } else if (ultimo instanceof Resistencia) {
            mensajeEstado = "Conecta la resistencia hacia el Ánodo (+) del LED o hacia tierra (-).";
        } else if (ultimo instanceof LED) {
            mensajeEstado = "Conecta el cátodo (-) del LED hacia el polo negativo (-) de la fuente.";
        } else {
            mensajeEstado = "Conecta la fuente (+), los dos interruptores en serie, la resistencia y el LED al polo (-).";
        }

        return false;
    }
}
