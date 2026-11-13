package com.schematic.model.niveles;

import com.schematic.model.componentes.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nivel6 extends Nivel {

    public static final float TIEMPO_LIMITE = 150.0f;
    public static final float TIEMPO_POR_LUZ = 1.5f;
    public static final String[] SECUENCIA = {"VERDE", "AMARILLO", "ROJO"};

    private int pasoSecuencia;
    private float tiempoEnLuz;
    private boolean ramasConectadas;
    private String luzActual;
    private boolean variasLuces;

    public Nivel6() {
        super(6, "NIVEL 6: EL SEMÁFORO", TIEMPO_LIMITE);
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
        this.tiempoRestante = this.tiempoLimite;
        this.pasoSecuencia = 0;
        this.tiempoEnLuz = 0f;
        this.ramasConectadas = false;
        this.luzActual = null;
        this.variasLuces = false;

        this.componentes.add(new FuenteAlimentacion("FUENTE_PODER", 80f, 280f));
        this.inventario.add(new CasillaInventario("LED_VERDE", "LED Verde", 1));
        this.inventario.add(new CasillaInventario("LED_AMARILLO", "LED Amarillo", 1));
        this.inventario.add(new CasillaInventario("LED_ROJO", "LED Rojo", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 3));
        this.inventario.add(new CasillaInventario("SWITCH", "Interruptor", 3));
        this.mensajeEstado = mensajeColocarPiezas("Objetivo: tres luces que se prendan cada una por su lado.");
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "EL SEMÁFORO",
                "Tres LEDs en paralelo, cada uno con su interruptor y su resistencia. Tú decides cuál se prende y en qué orden.",
                "Un semáforo pasa de verde a amarillo y luego a rojo, y nunca prende dos luces al mismo tiempo.",
                TutorialNivel.Esquema.RAMAS,
                new String[]{"Una luz que se controla sola necesita su propio switch.", "Tres luces, tres caminos.", "Cada LED necesita su propia protección.", "Un semáforo nunca prende dos luces a la vez."},
                "Sé el controlador: VERDE, luego AMARILLO y luego ROJO. 1.5 s cada una y solo una luz a la vez."
        ).conRamas(new String[]{"SW", "LED_VERDE", "R"}, new String[]{"SW", "LED_AMARILLO", "R"}, new String[]{"SW", "LED_ROJO", "R"})
         .conIcono("SEMAFORO")
         .conConsejo("Si prendes la luz equivocada, la luz que toca vuelve a empezar su cuenta.");
    }

    @Override
    public String getTextoProgreso() {
        return "Luz " + Math.min(pasoSecuencia + 1, SECUENCIA.length) + " de " + SECUENCIA.length;
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        ramasConectadas = false;
        luzActual = null;
        variasLuces = false;
        apagarTodo();

        FuenteAlimentacion fuente = buscarFuente();
        if (fuente == null) return false;
        Terminal positivo = fuente.getTerminalPositivo();
        positivo.setValorLogico(true);
        if (hayCortoEnFuente(fuente)) return false;

        if (buscarCablesConTerminal(positivo).isEmpty()) {
            mensajeEstado = mensajeColocarPiezas("Objetivo: tres luces que se prendan cada una por su lado.");
            return false;
        }

        int ramasListas = 0;
        Set<String> colores = new HashSet<>();
        List<String> encendidas = new ArrayList<>();
        Recorrido incompleta = null;

        for (Switch sw : buscarSwitches()) {
            Cable entrada = cableEntre(positivo, sw.getTerminalEntrada());
            if (entrada == null) continue;
            entrada.setTieneEnergia(true);
            sw.getTerminalEntrada().setValorLogico(true);
            sw.evaluarEstado();

            Recorrido rama = recorrerHastaTierra(sw.getTerminalSalida(), fuente);
            if (!revisarErroresDeSalida(rama, sw.isCerrado(), fuente)) return false;

            if (rama.llegoATierra && rama.pasoPorLed && rama.pasoPorResistencia) {
                ramasListas++;
                colores.add(rama.led.getColor());
                if (sw.isCerrado()) {
                    encenderTramoDeSalida(rama, fuente);
                    encendidas.add(rama.led.getColor());
                }
            } else {
                incompleta = rama;
            }
        }

        ramasConectadas = ramasListas >= 3 && colores.size() == 3;
        if (!ramasConectadas) {
            if (incompleta != null) {
                revisarSalidaCompleta(incompleta);
                mensajeEstado = "Rama incompleta: " + mensajeEstado;
            } else {
                mensajeEstado = mensajeColocarPiezas("Luces que ya se controlan solas: " + ramasListas + " / 3.");
            }
            return false;
        }

        variasLuces = encendidas.size() > 1;
        luzActual = (encendidas.size() == 1) ? encendidas.get(0) : null;

        if (ganado) {
            mensajeEstado = "¡Nivel superado! El semáforo funciona.";
        } else if (variasLuces) {
            mensajeEstado = "¡Solo una luz a la vez! Apaga las demás.";
        } else {
            mensajeEstado = String.format("Enciende SOLO la luz %s  (%.1f / %.1f s)", SECUENCIA[pasoSecuencia], tiempoEnLuz, TIEMPO_POR_LUZ);
        }
        return luzActual != null;
    }

    @Override
    protected void actualizarVictoria(boolean circuitoActivo, float deltaTiempo) {
        if (ganado) return;
        if (luzActual != null && luzActual.equals(SECUENCIA[pasoSecuencia])) {
            tiempoEnLuz += deltaTiempo;
            if (tiempoEnLuz >= TIEMPO_POR_LUZ) {
                pasoSecuencia++;
                tiempoEnLuz = 0f;
                if (pasoSecuencia >= SECUENCIA.length) {
                    ganado = true;
                    completado = true;
                    mensajeEstado = "¡Nivel superado! El semáforo funciona.";
                }
            }
        } else if (luzActual != null || variasLuces) {
            tiempoEnLuz = 0f;
        }
        tiempoCircuitoActivo = (pasoSecuencia + tiempoEnLuz / TIEMPO_POR_LUZ) * getTiempoRequeridoVictoria() / SECUENCIA.length;
    }

    public int getPasoSecuencia() { return pasoSecuencia; }
}
