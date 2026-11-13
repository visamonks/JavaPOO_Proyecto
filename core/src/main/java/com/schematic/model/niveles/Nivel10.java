package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel10 extends Nivel {

    public static final float TIEMPO_LIMITE = 80.0f;
    public static final int MINIMO = 40;
    public static final int MAXIMO = 60;
    public static final float TIEMPO_EN_RANGO = 3.0f;

    private boolean paso1Listo;
    private boolean paso2Listo;
    private boolean paso3Listo;
    private boolean enRango;

    public Nivel10() {
        super(10, "NIVEL 10: EL DIMMER", TIEMPO_LIMITE);
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
        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;
        this.enRango = false;

        this.componentes.add(new FuenteAlimentacion("FUENTE_PODER", 80f, 280f));
        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1));
        this.inventario.add(new CasillaInventario("SWITCH", "Interruptor", 1));
        this.inventario.add(new CasillaInventario("POTENCIOMETRO", "Potenciómetro", 1));
        this.mensajeEstado = mensajeColocarPiezas("Conecta la Fuente (+) al switch.");
    }

    @Override
    public String[] obtenerPasosGuia() {
        return new String[]{"1. Switch", "2. Perilla", "3. LED + 220", "4. Luz media"};
    }

    @Override
    public boolean[] obtenerEstadoPasosGuia() {
        return new boolean[]{paso1Listo, paso2Listo, paso3Listo, enRango};
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "EL DIMMER",
                "El potenciómetro es una resistencia que puedes ajustar girando su perilla: más resistencia, menos corriente y el LED brilla menos.",
                "El regulador de luz de un cuarto o la perilla del volumen de una bocina: giras y cambia poco a poco.",
                TutorialNivel.Esquema.RAMAS,
                new String[]{"Fuente (+) al switch", "Switch al potenciómetro", "Potenciómetro al (+) del LED", "LED (-) a 220 Ohm y a la Fuente (-)"},
                "Deja la luz entre 40% y 60% (luz de cine) y mantenla así 3 segundos."
        ).conRamas(new String[]{"SW", "POT", "LED", "R"})
         .conIcono("POT")
         .conConsejo("Haz clic en el potenciómetro para abrir su barra y arrastra la perilla. También sirve la rueda del ratón.");
    }

    @Override
    public float getTiempoRequeridoVictoria() {
        return TIEMPO_EN_RANGO;
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        paso1Listo = false;
        paso2Listo = false;
        paso3Listo = false;
        enRango = false;
        apagarTodo();

        FuenteAlimentacion fuente = buscarFuente();
        if (fuente == null) return false;
        Terminal positivo = fuente.getTerminalPositivo();
        positivo.setValorLogico(true);
        if (hayCortoEnFuente(fuente)) return false;

        Switch sw = buscarSwitches().isEmpty() ? null : buscarSwitches().get(0);
        Potenciometro perilla = buscarPotenciometro();
        if (sw == null || perilla == null || buscarCablesConTerminal(positivo).isEmpty()) {
            mensajeEstado = mensajeColocarPiezas("Conecta la Fuente (+) al switch.");
            return false;
        }

        Cable entrada = cableEntre(positivo, sw.getTerminalEntrada());
        if (entrada == null) {
            mensajeEstado = "Conecta la Fuente (+) al pin izquierdo del switch.";
            return false;
        }
        paso1Listo = true;
        entrada.setTieneEnergia(true);
        sw.getTerminalEntrada().setValorLogico(true);
        sw.evaluarEstado();

        Recorrido salida = recorrerHastaTierra(sw.getTerminalSalida(), fuente);
        if (!revisarErroresDeSalida(salida, sw.isCerrado(), fuente)) return false;
        paso2Listo = salida.potenciometro != null;
        paso3Listo = salida.llegoATierra && salida.pasoPorLed && salida.pasoPorResistencia;

        if (!revisarSalidaCompleta(salida)) return false;
        if (!paso2Listo) {
            mensajeEstado = "Pon el potenciómetro en el camino para poder regular la luz.";
            return false;
        }
        if (!sw.isCerrado()) {
            mensajeEstado = "Enciende el switch para prender la luz.";
            return false;
        }

        encenderTramoDeSalida(salida, fuente);
        int intensidad = perilla.getPorcentaje();
        salida.led.setBrillo(Math.max(0.08f, perilla.getValor()));
        enRango = intensidad >= MINIMO && intensidad <= MAXIMO;

        if (ganado) {
            mensajeEstado = "¡Nivel superado! Luz perfecta para ver una película.";
        } else if (intensidad > MAXIMO) {
            mensajeEstado = "Intensidad " + intensidad + "%: bájala hasta " + MINIMO + "%-" + MAXIMO + "% (mitad izquierda de la perilla).";
        } else if (intensidad < MINIMO) {
            mensajeEstado = "Intensidad " + intensidad + "%: súbela hasta " + MINIMO + "%-" + MAXIMO + "% (mitad derecha de la perilla).";
        } else {
            mensajeEstado = String.format("¡Luz de cine! %d%%   %.1f / %.0f s", intensidad, tiempoCircuitoActivo, TIEMPO_EN_RANGO);
        }
        return enRango;
    }

    private Potenciometro buscarPotenciometro() {
        for (Componente c : componentes) {
            if (c instanceof Potenciometro) return (Potenciometro) c;
        }
        return null;
    }
}
