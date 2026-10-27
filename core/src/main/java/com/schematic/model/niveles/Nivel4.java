package com.schematic.model.niveles;

import com.schematic.constants.Constantes;
import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nivel4 extends Nivel {

    private boolean paso1Listo = false;
    private boolean paso2Listo = false;
    private boolean paso3Listo = false;
    private boolean paso4Listo = false;

    public Nivel4() {
        super(4, "NIVEL 4: COMPUERTA LÓGICA OR", Constantes.TIEMPO_LIMITE_NIVEL_4);
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
        this.tiempoLimite = Constantes.TIEMPO_LIMITE_NIVEL_4;
        this.tiempoRestante = this.tiempoLimite;
        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;
        this.paso4Listo = false;
        this.mensajeEstado = "Conecta Fuente (+) al Switch.";

        FuenteAlimentacion fuente = new FuenteAlimentacion(
                Constantes.ID_NIVEL4_FUENTE,
                Constantes.NIVEL4_FUENTE_X,
                Constantes.NIVEL4_FUENTE_Y
        );
        Switch swDoble = new Switch(
                Constantes.ID_NIVEL4_SWITCH,
                Constantes.NIVEL4_SWITCH_X,
                Constantes.NIVEL4_SWITCH_Y,
                false,
                false
        );
        CompuertaOR compuertaOr = new CompuertaOR(
                Constantes.ID_NIVEL4_OR,
                Constantes.NIVEL4_OR_X,
                Constantes.NIVEL4_OR_Y,
                false,
                false
        );
        Resistencia resistencia = new Resistencia(
                Constantes.ID_NIVEL4_RESISTENCIA,
                Constantes.NIVEL4_RESISTENCIA_X,
                Constantes.NIVEL4_RESISTENCIA_Y,
                220,
                220
        );
        LED led = new LED(
                Constantes.ID_NIVEL4_LED,
                Constantes.NIVEL4_LED_X,
                Constantes.NIVEL4_LED_Y,
                true
        );

        this.componentes.add(fuente);
        this.componentes.add(swDoble);
        this.componentes.add(compuertaOr);
        this.componentes.add(resistencia);
        this.componentes.add(led);

        CasillaInventario casLed = new CasillaInventario("LED", "LED (Diodo)", 1);
        casLed.decrementar();
        this.inventario.add(casLed);

        CasillaInventario casRes = new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1);
        casRes.decrementar();
        this.inventario.add(casRes);

        CasillaInventario casSw = new CasillaInventario("SWITCH", "Switch Divisor", 1);
        casSw.decrementar();
        this.inventario.add(casSw);

        CasillaInventario casOr = new CasillaInventario("OR", "Compuerta OR", 1);
        casOr.decrementar();
        this.inventario.add(casOr);
    }

    @Override
    public String[] obtenerPasosGuia() {
        return new String[]{
            "1. Entrada",
            "2. Ramas A+B",
            "3. 220 + LED",
            "4. A o B ON"
        };
    }

    @Override
    public boolean[] obtenerEstadoPasosGuia() {
        return new boolean[]{paso1Listo, paso2Listo, paso3Listo, paso4Listo};
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        paso1Listo = false;
        paso2Listo = false;
        paso3Listo = false;
        paso4Listo = false;

        FuenteAlimentacion fuente = null;
        Switch sw = null;
        CompuertaOR compuertaOr = null;
        Resistencia res = null;
        LED led = null;

        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) sw = (Switch) c;
            else if (c instanceof CompuertaOR) compuertaOr = (CompuertaOR) c;
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
        if (compuertaOr != null) {
            compuertaOr.setEntradas(false, false);
            compuertaOr.setEstadoActual("NEUTRO");
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
            mensajeEstado = "Conecta Fuente (+) al pin izquierdo del Switch.";
            this.ganado = false;
            this.completado = false;
            return false;
        }

        if (sw == null) {
            mensajeEstado = "Coloca el Switch en la mesa.";
            return false;
        }
        if (compuertaOr == null) {
            mensajeEstado = "Coloca la compuerta OR en la mesa.";
            return false;
        }

        Cable cableFuenteASw = null;
        boolean conectadoDirectoACompuerta = false;
        for (Cable cp : cablesPos) {
            Terminal dest = cp.getOtroTerminal(fuente.getTerminalPositivo());
            if (dest == sw.getTerminalEntrada()) {
                cableFuenteASw = cp;
            } else if (dest == compuertaOr.getTerminalEntradaA() || dest == compuertaOr.getTerminalEntradaB()) {
                conectadoDirectoACompuerta = true;
            }
        }

        if (cableFuenteASw == null) {
            if (conectadoDirectoACompuerta) {
                mensajeEstado = "Conecta Fuente (+) primero al Switch.";
            } else {
                mensajeEstado = "Conecta Fuente (+) al pin izquierdo del Switch.";
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
            if (dest == compuertaOr.getTerminalEntradaA()) {
                inAConectada = true;
                if (sw.isCerradoA()) {
                    senalEnInA = true;
                    ca.setTieneEnergia(true);
                }
            } else if (dest == compuertaOr.getTerminalEntradaB()) {
                inBConectada = true;
                if (sw.isCerradoA()) {
                    senalEnInB = true;
                    ca.setTieneEnergia(true);
                }
            }
        }

        for (Cable cb : cablesSalB) {
            Terminal dest = cb.getOtroTerminal(sw.getTerminalSalidaB());
            if (dest == compuertaOr.getTerminalEntradaB()) {
                inBConectada = true;
                if (sw.isCerradoB()) {
                    senalEnInB = true;
                    cb.setTieneEnergia(true);
                }
            } else if (dest == compuertaOr.getTerminalEntradaA()) {
                inAConectada = true;
                if (sw.isCerradoB()) {
                    senalEnInA = true;
                    cb.setTieneEnergia(true);
                }
            }
        }

        sw.evaluarEstado();
        compuertaOr.setEntradas(senalEnInA, senalEnInB);

        if (inAConectada && inBConectada) {
            paso2Listo = true;
        }

        Terminal terminalActual = compuertaOr.getTerminalSalida();
        boolean pasoPorResistencia = false;
        boolean pasoPorLed = false;
        boolean llegoATierra = false;
        boolean polaridadInvertida = false;
        Set<Componente> componentesVisitados = new HashSet<>();
        List<Cable> cablesSalida = new ArrayList<>();
        Componente ultimoComponenteSalida = compuertaOr;

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

        if (llegoATierra && !pasoPorLed && !pasoPorResistencia && compuertaOr.isSalida()) {
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
            mensajeEstado = "Polaridad invertida: entra por el Ánodo (+) rojo del LED y sal por el Cátodo (-) azul.";
            return false;
        }

        if (llegoATierra && pasoPorLed && !pasoPorResistencia) {
            if (compuertaOr.isSalida()) {
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
                mensajeEstado = "Agrega la resistencia de 220 Ohm para proteger el LED.";
                return false;
            }
        }

        if (!paso2Listo) {
            if (!inAConectada && !inBConectada) {
                mensajeEstado = "Conecta las ramas A y B del Switch hacia OR.";
            } else if (!inAConectada) {
                mensajeEstado = "Falta conectar la entrada IN A de OR.";
            } else {
                mensajeEstado = "Falta conectar la entrada IN B de OR.";
            }
            return false;
        }

        if (!llegoATierra) {
            if (cablesSalida.isEmpty()) {
                mensajeEstado = "Conecta la salida de OR a la resistencia 220 Ohm.";
            } else if (ultimoComponenteSalida instanceof Resistencia) {
                if (!pasoPorLed) {
                    mensajeEstado = "Conecta la resistencia al polo (+) del LED.";
                } else {
                    mensajeEstado = "Cierra hacia el polo (-) de la Fuente.";
                }
            } else if (ultimoComponenteSalida instanceof LED) {
                if (pasoPorResistencia) {
                    mensajeEstado = "Cierra desde LED (-) hacia Fuente (-).";
                } else {
                    mensajeEstado = "Conecta LED (-) a 220 Ohm y luego a Fuente (-).";
                }
            } else {
                mensajeEstado = "Conecta la salida hacia 220 Ohm, LED y Fuente (-).";
            }
            return false;
        }

        if (!pasoPorLed) {
            mensajeEstado = "Falta intercalar el LED en la salida.";
            return false;
        }

        paso3Listo = true;

        boolean cerradoA = sw.isCerradoA();
        boolean cerradoB = sw.isCerradoB();

        if (!cerradoA && !cerradoB) {
            compuertaOr.setEstadoActual("NEUTRO");
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

            mensajeEstado = "Activa la palanca A o B en el Switch.";
            return false;
        }

        paso4Listo = true;
        fuente.setEstadoActual("EXITO");
        sw.setEstadoActual("EXITO");
        compuertaOr.setEntradas(senalEnInA, senalEnInB);
        compuertaOr.setEstadoActual("EXITO");

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
