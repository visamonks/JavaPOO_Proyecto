package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel7 extends Nivel {

    public static final float TIEMPO_LIMITE = 70.0f;
    public static final float TIEMPO_SOLO_CAPACITOR = 3.0f;

    private float tiempoSoloCapacitor;
    private boolean prendidoPorCapacitor;
    private boolean paso1Listo;
    private boolean paso2Listo;
    private boolean paso3Listo;

    public Nivel7() {
        super(7, "NIVEL 7: EL CAPACITOR", TIEMPO_LIMITE);
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
        this.tiempoSoloCapacitor = 0f;
        this.prendidoPorCapacitor = false;
        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;

        this.componentes.add(new FuenteAlimentacion("FUENTE_PODER", 80f, 280f));
        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1));
        this.inventario.add(new CasillaInventario("SWITCH", "Interruptor", 1));
        this.inventario.add(new CasillaInventario("CAPACITOR", "Capacitor", 1));
        this.mensajeEstado = mensajeColocarPiezas("Conecta la Fuente (+) al switch.");
    }

    @Override
    public String[] obtenerPasosGuia() {
        return new String[]{"1. Switch", "2. Capacitor", "3. LED + 220", "4. Sin fuente"};
    }

    @Override
    public boolean[] obtenerEstadoPasosGuia() {
        return new boolean[]{paso1Listo, paso2Listo, paso3Listo, ganado};
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "EL CAPACITOR",
                "Un capacitor guarda energía como un tanque pequeño. Cuando se corta la fuente, la suelta poco a poco.",
                "Cuando desconectas el cargador de la laptop, la lucecita tarda un momento en apagarse: un capacitor la mantiene.",
                TutorialNivel.Esquema.RAMAS,
                new String[]{"Fuente (+) al switch", "Switch a la pata (+) del capacitor", "Switch al (+) del LED y su (-) a 220 Ohm", "Capacitor (-) y 220 Ohm a la Fuente (-)"},
                "Carga el capacitor, APAGA el switch y deja que el capacitor mantenga el LED prendido 3 segundos."
        ).conRamas(new String[]{"SW", "", "LED", "R"}, new String[]{"", "|", "CAP"})
         .conIcono("CAP")
         .conConsejo("Ojo con la polaridad: si el capacitor queda al revés y le llega corriente, explota.");
    }

    @Override
    public float getTiempoRequeridoVictoria() {
        return TIEMPO_SOLO_CAPACITOR;
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        paso1Listo = false;
        paso2Listo = false;
        paso3Listo = false;
        prendidoPorCapacitor = false;
        apagarTodo();

        FuenteAlimentacion fuente = buscarFuente();
        if (fuente == null) return false;
        Terminal positivo = fuente.getTerminalPositivo();
        positivo.setValorLogico(true);
        if (hayCortoEnFuente(fuente)) return false;

        Switch sw = buscarSwitches().isEmpty() ? null : buscarSwitches().get(0);
        Capacitor capacitor = buscarCapacitor();
        if (sw == null || capacitor == null || buscarCablesConTerminal(positivo).isEmpty()) {
            if (capacitor != null && !ganado) capacitor.descargar(deltaActual);
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

        Terminal salidaSwitch = sw.getTerminalSalida();
        if (revisarCapacitorAlReves(capacitor, salidaSwitch, fuente, sw.isCerrado())) return false;
        Cable haciaCapacitor = cableEntre(salidaSwitch, capacitor.getTerminalPositivo());
        Cable capacitorATierra = cableEntre(capacitor.getTerminalNegativo(), fuente.getTerminalNegativo());
        paso2Listo = haciaCapacitor != null && capacitorATierra != null;

        Recorrido salida = recorrerHastaTierra(salidaSwitch, fuente);
        boolean hayCorriente = sw.isCerrado() || (paso2Listo && capacitor.tieneCarga());
        if (!revisarErroresDeSalida(salida, hayCorriente, fuente)) return false;
        paso3Listo = salida.llegoATierra && salida.pasoPorLed && salida.pasoPorResistencia;

        if (!paso2Listo) {
            mensajeEstado = "Conecta la salida del switch a la pata (+) del capacitor y su pata (-) a la Fuente (-).";
            return false;
        }
        if (!revisarSalidaCompleta(salida)) return false;

        if (sw.isCerrado()) {
            capacitor.cargar(deltaActual);
            haciaCapacitor.setTieneEnergia(true);
            capacitorATierra.setTieneEnergia(true);
            encenderTramoDeSalida(salida, fuente);
            int porcentaje = Math.round(capacitor.getCarga() * 100f);
            mensajeEstado = (porcentaje < 100)
                    ? "Cargando el capacitor: " + porcentaje + "%"
                    : "Capacitor lleno. Ahora APAGA el switch y mira el LED.";
            capacitor.evaluarEstado();
            return true;
        }

        if (capacitor.tieneCarga()) {
            capacitor.descargar(deltaActual);
            haciaCapacitor.setTieneEnergia(true);
            capacitorATierra.setTieneEnergia(true);
            encenderTramoDeSalida(salida, fuente);
            salida.led.setBrillo(0.25f + 0.75f * capacitor.getCarga());
            capacitor.evaluarEstado();
            prendidoPorCapacitor = true;
            mensajeEstado = ganado
                    ? "¡Nivel superado! El capacitor guardó la energía."
                    : String.format("¡El capacitor mantiene el LED!  %.1f / %.1f s", tiempoSoloCapacitor, TIEMPO_SOLO_CAPACITOR);
            return true;
        }

        mensajeEstado = ganado ? "¡Nivel superado! El capacitor guardó la energía." : "Enciende el switch para cargar el capacitor.";
        return false;
    }

    @Override
    protected void actualizarVictoria(boolean circuitoActivo, float deltaTiempo) {
        if (ganado) return;
        if (prendidoPorCapacitor) {
            tiempoSoloCapacitor += deltaTiempo;
        } else {
            tiempoSoloCapacitor = 0f;
        }
        tiempoCircuitoActivo = Math.min(tiempoSoloCapacitor, TIEMPO_SOLO_CAPACITOR);
        if (tiempoSoloCapacitor >= TIEMPO_SOLO_CAPACITOR) {
            ganado = true;
            completado = true;
            mensajeEstado = "¡Nivel superado! El capacitor guardó la energía.";
        }
    }

    private Capacitor buscarCapacitor() {
        for (Componente c : componentes) {
            if (c instanceof Capacitor) return (Capacitor) c;
        }
        return null;
    }
}
