package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel1 extends Nivel {

    private boolean paso1Listo;
    private boolean paso2Listo;
    private boolean paso3Listo;

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
        this.tiempoCircuitoActivo = 0.0f;
        this.tiempoLimite = 30.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;
        this.mensajeEstado = "Conecta Fuente (+), resistencia 220 Ohm y LED.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        Resistencia resistencia = new Resistencia("R_1", 620f, 544f, 220, 220);
        LED led = new LED("LED_1", 1120f, 440f, true);

        this.componentes.add(fuente);
        this.componentes.add(resistencia);
        this.componentes.add(led);

        CasillaInventario casLed = new CasillaInventario("LED", "LED (Diodo)", 1);
        casLed.decrementar();
        this.inventario.add(casLed);

        CasillaInventario casRes = new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1);
        casRes.decrementar();
        this.inventario.add(casRes);
    }

    @Override
    public String[] obtenerPasosGuia() {
        return new String[]{
            "1. Fuente",
            "2. 220 Ohm",
            "3. LED"
        };
    }

    @Override
    public boolean[] obtenerEstadoPasosGuia() {
        return new boolean[]{paso1Listo, paso2Listo, paso3Listo};
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

        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;
        if (res != null && led != null) {
            for (Cable c : cables) {
                if (c.conectaTerminal(fuente.getTerminalPositivo()) &&
                        (c.conectaTerminal(res.getTerminalEntrada()) || c.conectaTerminal(res.getTerminalSalida()) || c.conectaTerminal(led.getTerminalAnodo()))) {
                    this.paso1Listo = true;
                }
                if ((c.conectaTerminal(res.getTerminalEntrada()) || c.conectaTerminal(res.getTerminalSalida())) &&
                        (c.conectaTerminal(led.getTerminalAnodo()) || c.conectaTerminal(led.getTerminalCatodo()))) {
                    this.paso2Listo = true;
                }
                if (c.conectaTerminal(fuente.getTerminalNegativo()) &&
                        (c.conectaTerminal(led.getTerminalCatodo()) || c.conectaTerminal(res.getTerminalEntrada()) || c.conectaTerminal(res.getTerminalSalida()))) {
                    this.paso3Listo = true;
                }
            }
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
            mensajeEstado = "Objetivo: Conecta la fuente (+), la resistencia y el LED para encenderlo.";
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
                        if (tiempoCircuitoActivo >= TIEMPO_REQUERIDO_VICTORIA) {
                            mensajeEstado = "¡CIRCUITO COMPLETO! El LED se mantuvo encendido 5 segundos. ¡Nivel superado!";
                        } else {
                            mensajeEstado = String.format("¡CIRCUITO ACTIVO! Manténlo encendido: %.1f / %.1f s", tiempoCircuitoActivo, TIEMPO_REQUERIDO_VICTORIA);
                        }
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
                        if (tiempoCircuitoActivo >= TIEMPO_REQUERIDO_VICTORIA) {
                            mensajeEstado = "¡CIRCUITO COMPLETO! El LED se mantuvo encendido 5 segundos. ¡Nivel superado!";
                        } else {
                            mensajeEstado = String.format("¡CIRCUITO ACTIVO! Manténlo encendido: %.1f / %.1f s", tiempoCircuitoActivo, TIEMPO_REQUERIDO_VICTORIA);
                        }
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
                } else if (led != null && destinoSalida == led.getTerminalCatodo()) {
                    cableSalida.setTieneEnergia(true);
                    destinoSalida.setValorLogico(true);
                    led.setQuemado(false);
                    led.setEstadoActual("NEUTRO");
                    res.setQuemada(false);
                    res.setEstadoActual("NEUTRO");
                    fuente.setEstadoActual("NEUTRO");
                    mensajeEstado = "Polaridad invertida: Conecta la resistencia al Ánodo (+) del LED.";
                    return false;
                } else if (destinoSalida == fuente.getTerminalNegativo()) {
                    cableSalida.setTieneEnergia(true);
                    fuente.getTerminalNegativo().setValorLogico(true);
                    res.setQuemada(false);
                    res.setEstadoActual("EXITO");
                    fuente.setEstadoActual("NEUTRO");
                    mensajeEstado = "Corriente fluyendo por la resistencia. Intercala el LED en el circuito.";
                    return false;
                }
            }
            res.setQuemada(false);
            res.setEstadoActual("NEUTRO");
            fuente.setEstadoActual("NEUTRO");
            mensajeEstado = "Conecta la salida de la resistencia hacia el Ánodo (+) del LED.";
            return false;
        } else if (led != null && destinoPos == led.getTerminalCatodo()) {
            led.setQuemado(false);
            led.setEstadoActual("NEUTRO");
            fuente.setEstadoActual("NEUTRO");
            mensajeEstado = "Polaridad invertida: El positivo (+) debe conectarse al Ánodo (+) del LED.";
            return false;
        }

        return false;
    }
}

