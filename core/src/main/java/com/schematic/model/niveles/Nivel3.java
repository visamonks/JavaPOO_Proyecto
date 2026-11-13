package com.schematic.model.niveles;

import com.schematic.constants.Constantes;
import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nivel3 extends Nivel {

    private boolean paso1Listo = false;
    private boolean paso2Listo = false;
    private boolean paso3Listo = false;
    private boolean paso4Listo = false;

    public Nivel3() {
        super(3, "NIVEL 3: COMPUERTA LÓGICA AND", Constantes.TIEMPO_LIMITE_NIVEL_3);
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
        this.tiempoLimite = Constantes.TIEMPO_LIMITE_NIVEL_3;
        this.tiempoRestante = this.tiempoLimite;
        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;
        this.paso4Listo = false;
        this.componentes.add(new FuenteAlimentacion("FUENTE_PODER", 80f, 280f));
        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1));
        this.inventario.add(new CasillaInventario("SWITCH", "Switch Divisor", 1));
        this.inventario.add(new CasillaInventario("AND", "Compuerta AND", 1));
        this.mensajeEstado = mensajeColocarPiezas("Objetivo: que la luz dependa de las palancas A y B.");
    }

    @Override
    public boolean usaSwitchDivisor() {
        return true;
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "COMPUERTA AND",
                "La compuerta AND solo deja salir corriente si sus dos entradas, A Y B, están encendidas.",
                "Una caja fuerte con dos llaves: solo se abre si las dos personas giran su llave al mismo tiempo.",
                TutorialNivel.Esquema.DIVISOR_AND,
                new String[]{"Cada palanca del switch abre su propio camino.", "La AND solo responde cuando le llegan A y B.", "Lo que sale de la compuerta es lo que prende la luz.", "El LED nunca va sin su protección."},
                "Enciende las palancas A y B a la vez por 5 segundos."
        ).conTablaVerdad(false, false, false, true)
         .conConsejo("Clic en la mitad de arriba del switch para A y en la de abajo para B.");
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        paso1Listo = false;
        paso2Listo = false;
        paso3Listo = false;
        paso4Listo = false;

        FuenteAlimentacion fuente = null;
        Switch sw = null;
        CompuertaAND compuertaAnd = null;
        Resistencia res = null;
        LED led = null;

        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) sw = (Switch) c;
            else if (c instanceof CompuertaAND) compuertaAnd = (CompuertaAND) c;
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

        for (Cable c : cables) {
            if (c.conectaTerminal(fuente.getTerminalPositivo()) && c.conectaTerminal(fuente.getTerminalNegativo())) {
                fuente.setEstadoActual("ERROR");
                c.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO DIRECTO! Nunca unas el (+) directo al (-) de la fuente.";
                return false;
            }
        }

        fuente.setEstadoActual("NEUTRO");
        if (sw != null) sw.setEstadoActual("NEUTRO");
        if (compuertaAnd != null) {
            compuertaAnd.setEntradas(false, false);
            compuertaAnd.setEstadoActual("NEUTRO");
        }
        if (res != null && !res.isQuemada()) res.setEstadoActual("NEUTRO");
        if (led != null && !led.isQuemado()) led.setEstadoActual("NEUTRO");

        List<Cable> cablesPos = buscarCablesConTerminal(fuente.getTerminalPositivo());
        if (cablesPos.isEmpty()) {
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            mensajeEstado = mensajeColocarPiezas("Objetivo: que la luz dependa de las palancas A y B.");
            this.ganado = false;
            this.completado = false;
            return false;
        }

        if (sw == null) {
            mensajeEstado = "Coloca el Switch en la mesa.";
            return false;
        }
        if (compuertaAnd == null) {
            mensajeEstado = "Coloca la compuerta AND en la mesa.";
            return false;
        }

        Cable cableFuenteASw = null;
        boolean conectadoDirectoACompuerta = false;
        for (Cable cp : cablesPos) {
            Terminal dest = cp.getOtroTerminal(fuente.getTerminalPositivo());
            if (dest == sw.getTerminalEntrada()) {
                cableFuenteASw = cp;
            } else if (dest == compuertaAnd.getTerminalEntradaA() || dest == compuertaAnd.getTerminalEntradaB()) {
                conectadoDirectoACompuerta = true;
            }
        }

        if (cableFuenteASw == null) {
            if (conectadoDirectoACompuerta) {
                mensajeEstado = "El switch no recibe energía.";
            } else {
                mensajeEstado = "El switch no recibe energía por su entrada.";
            }
            return false;
        }

        paso1Listo = true;
        cableFuenteASw.setTieneEnergia(true);
        sw.getTerminalEntrada().setValorLogico(true);

        List<Cable> cablesSalA = buscarCablesConTerminal(sw.getTerminalSalidaA());
        List<Cable> cablesSalB = buscarCablesConTerminal(sw.getTerminalSalidaB());

        for (Cable ca : cablesSalA) {
            if (ca.getOtroTerminal(sw.getTerminalSalidaA()) == fuente.getTerminalNegativo() && sw.isCerradoA()) {
                fuente.setEstadoActual("ERROR");
                ca.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO! Conectaste SAL A del Switch directo al polo negativo (-).";
                return false;
            }
        }
        for (Cable cb : cablesSalB) {
            if (cb.getOtroTerminal(sw.getTerminalSalidaB()) == fuente.getTerminalNegativo() && sw.isCerradoB()) {
                fuente.setEstadoActual("ERROR");
                cb.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO! Conectaste SAL B del Switch directo al polo negativo (-).";
                return false;
            }
        }

        boolean inAConectada = false;
        boolean inBConectada = false;
        boolean senalEnInA = false;
        boolean senalEnInB = false;

        for (Cable ca : cablesSalA) {
            Terminal dest = ca.getOtroTerminal(sw.getTerminalSalidaA());
            if (dest == compuertaAnd.getTerminalEntradaA()) {
                inAConectada = true;
                if (sw.isCerradoA()) {
                    senalEnInA = true;
                    ca.setTieneEnergia(true);
                }
            } else if (dest == compuertaAnd.getTerminalEntradaB()) {
                inBConectada = true;
                if (sw.isCerradoA()) {
                    senalEnInB = true;
                    ca.setTieneEnergia(true);
                }
            }
        }

        for (Cable cb : cablesSalB) {
            Terminal dest = cb.getOtroTerminal(sw.getTerminalSalidaB());
            if (dest == compuertaAnd.getTerminalEntradaB()) {
                inBConectada = true;
                if (sw.isCerradoB()) {
                    senalEnInB = true;
                    cb.setTieneEnergia(true);
                }
            } else if (dest == compuertaAnd.getTerminalEntradaA()) {
                inAConectada = true;
                if (sw.isCerradoB()) {
                    senalEnInA = true;
                    cb.setTieneEnergia(true);
                }
            }
        }

        sw.evaluarEstado();
        compuertaAnd.setEntradas(senalEnInA, senalEnInB);

        if (inAConectada && inBConectada) {
            paso2Listo = true;
        }

        Terminal terminalActual = compuertaAnd.getTerminalSalida();
        boolean pasoPorResistencia = false;
        boolean pasoPorLed = false;
        boolean llegoATierra = false;
        boolean polaridadInvertida = false;
        Set<Componente> componentesVisitados = new HashSet<>();
        List<Cable> cablesSalida = new ArrayList<>();
        Componente ultimoComponenteSalida = compuertaAnd;

        while (terminalActual != null) {
            Cable cableSaliente = buscarCableConTerminal(terminalActual);
            if (cableSaliente == null) {
                break;
            }
            cablesSalida.add(cableSaliente);

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
                break;
            }
            componentesVisitados.add(compDestino);
            ultimoComponenteSalida = compDestino;

            if (compDestino instanceof Resistencia) {
                Resistencia r = (Resistencia) compDestino;
                pasoPorResistencia = true;
                terminalActual = (destino == r.getTerminalEntrada()) ? r.getTerminalSalida() : r.getTerminalEntrada();
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

        if (llegoATierra && !pasoPorLed && !pasoPorResistencia && compuertaAnd.isSalida()) {
            fuente.setEstadoActual("ERROR");
            for (Cable c : cablesSalida) c.setTieneEnergia(true);
            mensajeEstado = "¡CORTOCIRCUITO DIRECTO! Conectaste la salida OUT directo a Fuente (-) sin componentes.";
            return false;
        }

        if (polaridadInvertida) {
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            mensajeEstado = "Polaridad invertida: el LED está al revés y no deja pasar la corriente.";
            return false;
        }

        if (llegoATierra && pasoPorLed && !pasoPorResistencia) {
            if (compuertaAnd.isSalida()) {
                if (led != null) {
                    led.setQuemado(true);
                    led.setEstadoActual("ERROR");
                }
                fuente.setEstadoActual("ERROR");
                for (Cable c : cablesSalida) {
                    c.setTieneEnergia(true);
                }
                mensajeEstado = "¡EL LED SE HA QUEMADO! Faltó la resistencia de 220 Ohm.";
                return false;
            } else {
                mensajeEstado = "Al LED le falta su protección.";
                return false;
            }
        }

        if (!paso2Listo) {
            if (!inAConectada && !inBConectada) {
                mensajeEstado = "La AND no recibe nada del switch.";
            } else if (!inAConectada) {
                mensajeEstado = "A la AND le falta una entrada: IN A está vacía.";
            } else {
                mensajeEstado = "A la AND le falta una entrada: IN B está vacía.";
            }
            return false;
        }

        if (!llegoATierra) {
            if (cablesSalida.isEmpty()) {
                mensajeEstado = "La salida de la AND no lleva a ninguna luz.";
            } else if (ultimoComponenteSalida instanceof Resistencia) {
                if (!pasoPorLed) {
                    mensajeEstado = "La corriente se queda atorada en la resistencia.";
                } else {
                    mensajeEstado = "El circuito está abierto: la corriente no regresa a la fuente.";
                }
            } else if (ultimoComponenteSalida instanceof LED) {
                if (pasoPorResistencia) {
                    mensajeEstado = "El circuito está abierto: la corriente no regresa a la fuente.";
                } else {
                    mensajeEstado = "La corriente se queda atorada en el LED.";
                }
            } else {
                mensajeEstado = "La salida de la AND no lleva a ninguna luz.";
            }
            return false;
        }

        if (!pasoPorLed) {
            mensajeEstado = "La corriente pasa, pero no hay ninguna luz en su camino.";
            return false;
        }

        paso3Listo = true;

        boolean cerradoA = sw.isCerradoA();
        boolean cerradoB = sw.isCerradoB();

        if (!cerradoA || !cerradoB) {
            compuertaAnd.setEstadoActual("NEUTRO");
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            for (Cable c : cablesSalida) {
                c.setTieneEnergia(false);
            }

            if (!cerradoA && !cerradoB) {
                mensajeEstado = "Todo conectado, pero la AND todavía no deja pasar nada.";
            } else if (cerradoA) {
                mensajeEstado = "Todo conectado, pero la AND todavía no deja pasar nada.";
            } else {
                mensajeEstado = "Todo conectado, pero la AND todavía no deja pasar nada.";
            }
            return false;
        }

        paso4Listo = true;
        fuente.setEstadoActual("EXITO");
        sw.setEstadoActual("EXITO");
        compuertaAnd.setEntradas(true, true);
        compuertaAnd.setEstadoActual("EXITO");

        if (res != null) {
            res.setQuemada(false);
            res.setEstadoActual("EXITO");
            res.getTerminalEntrada().setValorLogico(true);
            res.getTerminalSalida().setValorLogico(true);
        }
        if (led != null) {
            led.setQuemado(false);
            led.setEstadoActual("EXITO");
            led.getTerminalAnodo().setValorLogico(true);
            led.getTerminalCatodo().setValorLogico(true);
        }

        fuente.getTerminalPositivo().setValorLogico(true);
        fuente.getTerminalNegativo().setValorLogico(true);

        for (Cable c : cablesSalida) {
            c.setTieneEnergia(true);
        }

        if (tiempoCircuitoActivo >= TIEMPO_REQUERIDO_VICTORIA) {
            mensajeEstado = "¡Nivel superado!";
        } else {
            mensajeEstado = String.format("Circuito activo: %.1f / %.0f s", tiempoCircuitoActivo, TIEMPO_REQUERIDO_VICTORIA);
        }
        return true;
    }
}
