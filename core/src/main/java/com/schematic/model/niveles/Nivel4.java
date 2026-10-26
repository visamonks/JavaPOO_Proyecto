package com.schematic.model.niveles;

import com.schematic.constants.Constantes;
import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nivel4 extends Nivel {

    public static class RutaCircuito {
        public List<Cable> cables = new ArrayList<>();
        public Set<Terminal> terminales = new HashSet<>();
        public Set<Componente> componentes = new HashSet<>();
        public Set<Switch> switches = new HashSet<>();
        public boolean tieneResistencia = false;
        public boolean tieneLed = false;
        public boolean polaridadInvertida = false;
    }

    public Nivel4() {
        super(4, "NIVEL 4: DOBLE SWITCH EN PARALELO (LÓGICA OR)", Constantes.TIEMPO_LIMITE_NIVEL_4);
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
        this.mensajeEstado = "Objetivo: Conecta la fuente, ambos interruptores en paralelo (Lógica OR), la resistencia y el LED.";

        FuenteAlimentacion fuente = new FuenteAlimentacion(
                Constantes.ID_NIVEL4_FUENTE,
                Constantes.NIVEL4_FUENTE_X,
                Constantes.NIVEL4_FUENTE_Y
        );
        Switch sw1 = new Switch(
                Constantes.ID_NIVEL4_SWITCH_A,
                Constantes.NIVEL4_SWITCH_A_X,
                Constantes.NIVEL4_SWITCH_A_Y,
                false
        );
        Switch sw2 = new Switch(
                Constantes.ID_NIVEL4_SWITCH_B,
                Constantes.NIVEL4_SWITCH_B_X,
                Constantes.NIVEL4_SWITCH_B_Y,
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
        Switch sw1 = null;
        Switch sw2 = null;
        Resistencia res = null;
        LED led = null;

        for (Componente c : componentes) {
            if (c instanceof FuenteAlimentacion) {
                fuente = (FuenteAlimentacion) c;
            } else if (c instanceof Switch) {
                if (Constantes.ID_NIVEL4_SWITCH_A.equals(c.getIdentificador())) {
                    sw1 = (Switch) c;
                } else if (Constantes.ID_NIVEL4_SWITCH_B.equals(c.getIdentificador())) {
                    sw2 = (Switch) c;
                } else if (sw1 == null) {
                    sw1 = (Switch) c;
                } else {
                    sw2 = (Switch) c;
                }
            } else if (c instanceof Resistencia) {
                res = (Resistencia) c;
            } else if (c instanceof LED) {
                led = (LED) c;
            }
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

        // Cortocircuito directo entre polos de la fuente
        for (Cable c : cables) {
            if (c.conectaTerminal(fuente.getTerminalPositivo()) && c.conectaTerminal(fuente.getTerminalNegativo())) {
                fuente.setEstadoActual("ERROR");
                c.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO DIRECTO! La fuente de poder ha estallado.";
                return false;
            }
        }

        if (buscarCableConTerminal(fuente.getTerminalPositivo()) == null) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            mensajeEstado = "Objetivo: Conecta la fuente, ambos interruptores en paralelo (Lógica OR), la resistencia y el LED.";
            this.ganado = false;
            this.completado = false;
            return false;
        }

        // Búsqueda de todas las rutas posibles desde fuente(+) hasta fuente(-)
        List<RutaCircuito> todasRutas = new ArrayList<>();
        RutaCircuito rutaInicial = new RutaCircuito();
        rutaInicial.terminales.add(fuente.getTerminalPositivo());
        Set<Terminal> termVisitados = new HashSet<>();
        termVisitados.add(fuente.getTerminalPositivo());
        buscarRutasDesdeTerminal(
                fuente.getTerminalPositivo(),
                fuente.getTerminalNegativo(),
                rutaInicial,
                todasRutas,
                new HashSet<>(),
                termVisitados,
                new HashSet<>()
        );

        // 1. Verificación de cortocircuito sin carga (camino a tierra sin resistencia ni LED)
        for (RutaCircuito r : todasRutas) {
            if (!r.tieneResistencia && !r.tieneLed) {
                fuente.setEstadoActual("ERROR");
                for (Cable c : r.cables) c.setTieneEnergia(true);
                mensajeEstado = "¡CORTOCIRCUITO DIRECTO! La fuente de poder ha estallado.";
                return false;
            }
        }

        // 2. Verificación de LED quemado (LED sin resistencia limitadora en camino cerrado)
        for (RutaCircuito r : todasRutas) {
            if (r.tieneLed && !r.tieneResistencia) {
                boolean conduce = true;
                for (Switch s : r.switches) {
                    if (!s.isCerrado()) {
                        conduce = false;
                        break;
                    }
                }
                if (conduce) {
                    if (led != null) {
                        led.setQuemado(true);
                        led.setEstadoActual("ERROR");
                    }
                    fuente.setEstadoActual("ERROR");
                    for (Cable c : r.cables) c.setTieneEnergia(true);
                    mensajeEstado = "¡EL LED SE HA QUEMADO! Conectaste a tierra sin resistencia limitadora.";
                    return false;
                }
            }
        }

        // 3. Verificación de polaridad invertida
        boolean algunaValida = false;
        boolean algunaInvertida = false;
        for (RutaCircuito r : todasRutas) {
            if (r.tieneLed && r.tieneResistencia) {
                if (r.polaridadInvertida) {
                    algunaInvertida = true;
                } else {
                    algunaValida = true;
                }
            }
        }
        if (algunaInvertida && !algunaValida) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null) res.setEstadoActual("NEUTRO");
            if (led != null) led.setEstadoActual("NEUTRO");
            for (Cable c : cables) c.setTieneEnergia(false);
            mensajeEstado = "Polaridad invertida: La corriente debe entrar por el Ánodo (+) del LED.";
            return false;
        }

        // 4. Filtrado de rutas válidas hacia tierra con protección
        List<RutaCircuito> rutasValidas = new ArrayList<>();
        for (RutaCircuito r : todasRutas) {
            if (r.tieneLed && r.tieneResistencia && !r.polaridadInvertida) {
                rutasValidas.add(r);
            }
        }

        if (rutasValidas.isEmpty()) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null && !res.isQuemada()) res.setEstadoActual("NEUTRO");
            if (led != null && !led.estaQuemado()) led.setEstadoActual("NEUTRO");
            for (Cable c : cables) c.setTieneEnergia(false);

            determinarMensajeCircuitoIncompleto(fuente, sw1, sw2, res, led);
            return false;
        }

        // Verificar si los interruptores fueron conectados en serie (Lógica AND) en lugar de paralelo
        boolean todasSonSerie = true;
        for (RutaCircuito r : rutasValidas) {
            if (!(r.switches.contains(sw1) && r.switches.contains(sw2))) {
                todasSonSerie = false;
                break;
            }
        }
        if (todasSonSerie) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null) res.setEstadoActual("NEUTRO");
            if (led != null) led.setEstadoActual("NEUTRO");
            for (Cable c : cables) c.setTieneEnergia(false);
            mensajeEstado = "Los interruptores están en serie (Lógica AND). Conéctalos en paralelo para la Lógica OR.";
            return false;
        }

        // Verificar existencia de ambas ramas en paralelo
        boolean tieneRamaSw1 = false;
        boolean tieneRamaSw2 = false;
        for (RutaCircuito r : rutasValidas) {
            if (r.switches.contains(sw1) && !r.switches.contains(sw2)) {
                tieneRamaSw1 = true;
            }
            if (r.switches.contains(sw2) && !r.switches.contains(sw1)) {
                tieneRamaSw2 = true;
            }
        }

        if (!tieneRamaSw1 && !tieneRamaSw2) {
            mensajeEstado = "Falta intercalar los interruptores en paralelo en el circuito.";
            return false;
        }
        if (tieneRamaSw1 && !tieneRamaSw2) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null) res.setEstadoActual("NEUTRO");
            if (led != null) led.setEstadoActual("NEUTRO");
            for (Cable c : cables) c.setTieneEnergia(false);
            mensajeEstado = "Falta conectar el segundo interruptor (Switch B) en paralelo para completar la Lógica OR.";
            return false;
        }
        if (!tieneRamaSw1 && tieneRamaSw2) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null) res.setEstadoActual("NEUTRO");
            if (led != null) led.setEstadoActual("NEUTRO");
            for (Cable c : cables) c.setTieneEnergia(false);
            mensajeEstado = "Falta conectar el primer interruptor (Switch A) en paralelo para completar la Lógica OR.";
            return false;
        }

        // --- TOPOLOGÍA EN PARALELO VERIFICADA ---
        boolean sw1Cerrado = (sw1 != null && sw1.isCerrado());
        boolean sw2Cerrado = (sw2 != null && sw2.isCerrado());

        if (!sw1Cerrado && !sw2Cerrado) {
            fuente.setEstadoActual("NEUTRO");
            if (sw1 != null) sw1.setEstadoActual("NEUTRO");
            if (sw2 != null) sw2.setEstadoActual("NEUTRO");
            if (res != null) {
                res.setQuemada(false);
                res.setEstadoActual("NEUTRO");
            }
            if (led != null) {
                led.setQuemado(false);
                led.setEstadoActual("NEUTRO");
            }
            for (Cable c : cables) c.setTieneEnergia(false);
            mensajeEstado = "Circuito en paralelo conectado. Ambos interruptores están abiertos. Cierra al menos uno para encender el LED.";
            return false;
        }

        // Al menos una rama conduce: LÓGICA OR SATISFECHA
        fuente.setEstadoActual("EXITO");
        fuente.getTerminalPositivo().setValorLogico(true);
        fuente.getTerminalNegativo().setValorLogico(true);

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

        if (sw1 != null) {
            if (sw1Cerrado) {
                sw1.setEstadoActual("EXITO");
                sw1.getTerminalEntrada().setValorLogico(true);
                sw1.getTerminalSalida().setValorLogico(true);
            } else {
                sw1.setEstadoActual("NEUTRO");
            }
        }
        if (sw2 != null) {
            if (sw2Cerrado) {
                sw2.setEstadoActual("EXITO");
                sw2.getTerminalEntrada().setValorLogico(true);
                sw2.getTerminalSalida().setValorLogico(true);
            } else {
                sw2.setEstadoActual("NEUTRO");
            }
        }

        // Energizar cables en las rutas que conducen corriente
        for (RutaCircuito r : rutasValidas) {
            boolean rutaConduce = true;
            for (Switch s : r.switches) {
                if (!s.isCerrado()) {
                    rutaConduce = false;
                    break;
                }
            }
            if (rutaConduce) {
                for (Cable c : r.cables) {
                    c.setTieneEnergia(true);
                }
            }
        }

        if (tiempoCircuitoActivo >= TIEMPO_REQUERIDO_VICTORIA) {
            mensajeEstado = "¡CIRCUITO COMPLETO! Lógica OR superada: el LED se mantuvo encendido 5 segundos. ¡Nivel superado!";
        } else {
            String detalleRamas = (sw1Cerrado && sw2Cerrado) ? "Ambas ramas conducen" :
                    (sw1Cerrado ? "Rama A conduce" : "Rama B conduce");
            mensajeEstado = String.format("¡CIRCUITO ACTIVO! Lógica OR (%s): %.1f / %.1f s", detalleRamas, tiempoCircuitoActivo, TIEMPO_REQUERIDO_VICTORIA);
        }
        return true;
    }

    private void buscarRutasDesdeTerminal(
            Terminal actual,
            Terminal destinoFinal,
            RutaCircuito rutaActual,
            List<RutaCircuito> rutasEncontradas,
            Set<Cable> cablesEnRuta,
            Set<Terminal> terminalesEnRuta,
            Set<Componente> componentesEnRuta
    ) {
        if (actual == destinoFinal) {
            RutaCircuito copia = new RutaCircuito();
            copia.cables.addAll(rutaActual.cables);
            copia.terminales.addAll(rutaActual.terminales);
            copia.componentes.addAll(rutaActual.componentes);
            copia.switches.addAll(rutaActual.switches);
            copia.tieneResistencia = rutaActual.tieneResistencia;
            copia.tieneLed = rutaActual.tieneLed;
            copia.polaridadInvertida = rutaActual.polaridadInvertida;
            rutasEncontradas.add(copia);
            return;
        }

        List<Cable> salientes = buscarCablesConTerminal(actual);
        for (Cable cable : salientes) {
            if (cablesEnRuta.contains(cable)) continue;

            Terminal destino = cable.getOtroTerminal(actual);
            if (destino == null || terminalesEnRuta.contains(destino)) continue;

            cablesEnRuta.add(cable);
            terminalesEnRuta.add(destino);
            rutaActual.cables.add(cable);
            rutaActual.terminales.add(destino);

            if (destino == destinoFinal) {
                RutaCircuito copia = new RutaCircuito();
                copia.cables.addAll(rutaActual.cables);
                copia.terminales.addAll(rutaActual.terminales);
                copia.componentes.addAll(rutaActual.componentes);
                copia.switches.addAll(rutaActual.switches);
                copia.tieneResistencia = rutaActual.tieneResistencia;
                copia.tieneLed = rutaActual.tieneLed;
                copia.polaridadInvertida = rutaActual.polaridadInvertida;
                rutasEncontradas.add(copia);
            } else {
                Componente comp = destino.getComponentePadre();
                if (comp != null && !componentesEnRuta.contains(comp) && !(comp instanceof FuenteAlimentacion)) {
                    componentesEnRuta.add(comp);
                    rutaActual.componentes.add(comp);

                    if (comp instanceof Switch) {
                        Switch sw = (Switch) comp;
                        rutaActual.switches.add(sw);
                        Terminal salida = (destino == sw.getTerminalEntrada()) ? sw.getTerminalSalida() : sw.getTerminalEntrada();
                        if (!terminalesEnRuta.contains(salida)) {
                            terminalesEnRuta.add(salida);
                            rutaActual.terminales.add(salida);

                            buscarRutasDesdeTerminal(salida, destinoFinal, rutaActual, rutasEncontradas, cablesEnRuta, terminalesEnRuta, componentesEnRuta);

                            rutaActual.terminales.remove(salida);
                            terminalesEnRuta.remove(salida);
                        }
                        rutaActual.switches.remove(sw);

                    } else if (comp instanceof Resistencia) {
                        Resistencia r = (Resistencia) comp;
                        boolean antesRes = rutaActual.tieneResistencia;
                        rutaActual.tieneResistencia = true;
                        Terminal salida = (destino == r.getTerminalEntrada()) ? r.getTerminalSalida() : r.getTerminalEntrada();
                        if (!terminalesEnRuta.contains(salida)) {
                            terminalesEnRuta.add(salida);
                            rutaActual.terminales.add(salida);

                            buscarRutasDesdeTerminal(salida, destinoFinal, rutaActual, rutasEncontradas, cablesEnRuta, terminalesEnRuta, componentesEnRuta);

                            rutaActual.terminales.remove(salida);
                            terminalesEnRuta.remove(salida);
                        }
                        rutaActual.tieneResistencia = antesRes;

                    } else if (comp instanceof LED) {
                        LED l = (LED) comp;
                        boolean antesLed = rutaActual.tieneLed;
                        boolean antesPol = rutaActual.polaridadInvertida;
                        rutaActual.tieneLed = true;

                        if (destino == l.getTerminalCatodo()) {
                            rutaActual.polaridadInvertida = true;
                            Terminal salida = l.getTerminalAnodo();
                            if (!terminalesEnRuta.contains(salida)) {
                                terminalesEnRuta.add(salida);
                                rutaActual.terminales.add(salida);

                                buscarRutasDesdeTerminal(salida, destinoFinal, rutaActual, rutasEncontradas, cablesEnRuta, terminalesEnRuta, componentesEnRuta);

                                rutaActual.terminales.remove(salida);
                                terminalesEnRuta.remove(salida);
                            }
                        } else {
                            Terminal salida = l.getTerminalCatodo();
                            if (!terminalesEnRuta.contains(salida)) {
                                terminalesEnRuta.add(salida);
                                rutaActual.terminales.add(salida);

                                buscarRutasDesdeTerminal(salida, destinoFinal, rutaActual, rutasEncontradas, cablesEnRuta, terminalesEnRuta, componentesEnRuta);

                                rutaActual.terminales.remove(salida);
                                terminalesEnRuta.remove(salida);
                            }
                        }
                        rutaActual.tieneLed = antesLed;
                        rutaActual.polaridadInvertida = antesPol;
                    }

                    rutaActual.componentes.remove(comp);
                    componentesEnRuta.remove(comp);
                }

                // Permite continuar por otro cable conectado al mismo terminal (nodo común)
                buscarRutasDesdeTerminal(destino, destinoFinal, rutaActual, rutasEncontradas, cablesEnRuta, terminalesEnRuta, componentesEnRuta);
            }

            rutaActual.terminales.remove(destino);
            rutaActual.cables.remove(cable);
            terminalesEnRuta.remove(destino);
            cablesEnRuta.remove(cable);
        }
    }

    private void determinarMensajeCircuitoIncompleto(FuenteAlimentacion fuente, Switch sw1, Switch sw2, Resistencia res, LED led) {
        boolean sw1Conectado = (sw1 != null && (buscarCableConTerminal(sw1.getTerminalEntrada()) != null || buscarCableConTerminal(sw1.getTerminalSalida()) != null));
        boolean sw2Conectado = (sw2 != null && (buscarCableConTerminal(sw2.getTerminalEntrada()) != null || buscarCableConTerminal(sw2.getTerminalSalida()) != null));
        boolean resConectada = (res != null && (buscarCableConTerminal(res.getTerminalEntrada()) != null || buscarCableConTerminal(res.getTerminalSalida()) != null));
        boolean ledConectado = (led != null && (buscarCableConTerminal(led.getTerminalAnodo()) != null || buscarCableConTerminal(led.getTerminalCatodo()) != null));

        if (!sw1Conectado && !sw2Conectado) {
            mensajeEstado = "Conecta la fuente (+) a las entradas de los interruptores en paralelo.";
        } else if (!resConectada) {
            mensajeEstado = "Conecta las salidas de los interruptores hacia la resistencia limitadora.";
        } else if (!ledConectado) {
            mensajeEstado = "Conecta la resistencia limitadora hacia el Ánodo (+) del LED.";
        } else {
            mensajeEstado = "Conecta el cátodo (-) del LED hacia el polo negativo (-) de la fuente.";
        }
    }
}
