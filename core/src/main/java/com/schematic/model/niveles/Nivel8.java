package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel8 extends Nivel {

    public static final float TIEMPO_LIMITE = 75.0f;
    public static final int PARPADEOS_PARA_GANAR = 5;

    private boolean paso1Listo;
    private boolean paso2Listo;
    private boolean paso3Listo;

    public Nivel8() {
        super(8, "NIVEL 8: LA AMBULANCIA (555 ASTABLE)", TIEMPO_LIMITE);
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

        this.componentes.add(new FuenteAlimentacion("FUENTE_PODER", 80f, 280f));
        this.inventario.add(new CasillaInventario("LED_ROJO", "LED Rojo", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1));
        this.inventario.add(new CasillaInventario("CAPACITOR", "Capacitor", 1));
        this.inventario.add(new CasillaInventario("555", "Chip 555", 1));
        this.mensajeEstado = mensajeColocarPiezas("Conecta la Fuente (+) al pin VCC del 555.");
    }

    @Override
    public String[] obtenerPasosGuia() {
        return new String[]{"1. VCC y GND", "2. Capacitor", "3. LED + 220", "4. Astable"};
    }

    @Override
    public boolean[] obtenerEstadoPasosGuia() {
        Temporizador555 chip = buscar555();
        return new boolean[]{paso1Listo, paso2Listo, paso3Listo, chip != null && chip.isAstable() && paso3Listo};
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "LA AMBULANCIA",
                "El chip 555 es un temporizador. En modo ASTABLE se prende y se apaga solo, una y otra vez, como un corazón que late.",
                "Las luces de una ambulancia o la direccional del carro parpadean solas: adentro hay un temporizador como el 555.",
                TutorialNivel.Esquema.RAMAS,
                new String[]{"Fuente (+) a VCC y GND a (-)", "Pin C al capacitor (+)", "OUT al (+) del LED y su (-) a 220 Ohm", "Capacitor (-) y 220 Ohm a (-)"},
                "Haz que la luz de la ambulancia parpadee 5 veces."
        ).conRamas(new String[]{"555A:ASTABLE", "LED_ROJO", "R"}, new String[]{"|", "CAP"})
         .conIcono("555A")
         .conConsejo("Haz clic en el 555 para cambiar su modo. Si tiene gorro de dormir está en MONOESTABLE.");
    }

    @Override
    public float getTiempoRequeridoVictoria() {
        return PARPADEOS_PARA_GANAR;
    }

    @Override
    public String getTextoProgreso() {
        return Math.round(tiempoCircuitoActivo) + " / " + PARPADEOS_PARA_GANAR + " parpadeos";
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        paso1Listo = false;
        paso2Listo = false;
        paso3Listo = false;
        apagarTodo();

        FuenteAlimentacion fuente = buscarFuente();
        if (fuente == null) return false;
        Terminal positivo = fuente.getTerminalPositivo();
        Terminal negativo = fuente.getTerminalNegativo();
        positivo.setValorLogico(true);
        if (hayCortoEnFuente(fuente)) return false;

        Temporizador555 chip = buscar555();
        Capacitor capacitor = buscarCapacitor();
        if (chip == null || capacitor == null || buscarCablesConTerminal(positivo).isEmpty()) {
            mensajeEstado = mensajeColocarPiezas("Conecta la Fuente (+) al pin VCC del 555.");
            return false;
        }

        Cable cableVcc = cableEntre(positivo, chip.getTerminalVcc());
        Cable cableGnd = cableEntre(chip.getTerminalTierra(), negativo);
        paso1Listo = cableVcc != null && cableGnd != null;

        Cable cableC = cableEntre(chip.getTerminalCapacitor(), capacitor.getTerminalPositivo());
        Cable capATierra = cableEntre(capacitor.getTerminalNegativo(), negativo);
        paso2Listo = cableC != null && capATierra != null;

        Recorrido salida = recorrerHastaTierra(chip.getTerminalSalida(), fuente);
        if (!revisarErroresDeSalida(salida, chip.isSalidaAlta(), fuente)) return false;
        paso3Listo = salida.llegoATierra && salida.pasoPorLed && salida.pasoPorResistencia;

        boolean listo = paso1Listo && paso2Listo && paso3Listo && chip.isAstable();
        chip.avanzar(deltaActual, listo, false);

        if (cableVcc == null) {
            mensajeEstado = "Conecta la Fuente (+) al pin VCC del 555 (arriba a la izquierda).";
            return false;
        }
        cableVcc.setTieneEnergia(true);
        if (cableGnd == null) {
            mensajeEstado = "Conecta el pin GND del 555 (abajo a la izquierda) a la Fuente (-).";
            return false;
        }
        cableGnd.setTieneEnergia(true);
        if (revisarCapacitorAlReves(capacitor, chip.getTerminalCapacitor(), fuente, true)) return false;
        if (!paso2Listo) {
            mensajeEstado = "El 555 necesita un capacitor para medir el tiempo: el pin C va a su pata (+) y su pata (-) a la Fuente (-).";
            return false;
        }
        cableC.setTieneEnergia(true);
        capATierra.setTieneEnergia(true);
        if (!revisarSalidaCompleta(salida)) {
            mensajeEstado = "Salida OUT: " + mensajeEstado;
            return false;
        }
        if (!chip.isAstable()) {
            mensajeEstado = "El 555 está dormido en MONOESTABLE. Haz clic en él para pasarlo a ASTABLE.";
            return false;
        }

        float progreso = chip.getProgresoCiclo();
        capacitor.setCarga(chip.isSalidaAlta() ? progreso : 1f - progreso);
        capacitor.evaluarEstado();
        chip.evaluarEstado();
        if (chip.isSalidaAlta()) encenderTramoDeSalida(salida, fuente);

        mensajeEstado = ganado
                ? "¡Nivel superado! La ambulancia ya tiene sus luces."
                : "¡Parpadeando solo! " + chip.getPulsos() + " / " + PARPADEOS_PARA_GANAR;
        return chip.isSalidaAlta();
    }

    @Override
    protected void actualizarVictoria(boolean circuitoActivo, float deltaTiempo) {
        if (ganado) return;
        Temporizador555 chip = buscar555();
        int pulsos = (chip != null) ? chip.getPulsos() : 0;
        tiempoCircuitoActivo = Math.min(pulsos, PARPADEOS_PARA_GANAR);
        if (pulsos >= PARPADEOS_PARA_GANAR) {
            ganado = true;
            completado = true;
            mensajeEstado = "¡Nivel superado! La ambulancia ya tiene sus luces.";
        }
    }
}
