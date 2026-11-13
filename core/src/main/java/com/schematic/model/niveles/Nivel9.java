package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel9 extends Nivel {

    public static final float TIEMPO_LIMITE = 180.0f;

    private boolean paso1Listo;
    private boolean paso2Listo;
    private boolean paso3Listo;

    public Nivel9() {
        super(9, "NIVEL 9: LA LUZ DEL PASILLO (555 MONOESTABLE)", TIEMPO_LIMITE);
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
        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 1));
        this.inventario.add(new CasillaInventario("SWITCH", "Botón", 1));
        this.inventario.add(new CasillaInventario("CAPACITOR", "Capacitor", 1));
        this.inventario.add(new CasillaInventario("555", "Chip 555", 1));
        this.mensajeEstado = mensajeColocarPiezas("Objetivo: una luz que se apague sola.");
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "LA LUZ DEL PASILLO",
                "En modo MONOESTABLE el 555 espera dormido. Con un toque en su pin TRIG despierta, prende su salida unos segundos y vuelve a dormir.",
                "La luz del pasillo de un edificio: presionas el botón una vez, se queda prendida un rato y luego se apaga sola para ahorrar energía.",
                TutorialNivel.Esquema.RAMAS,
                new String[]{"El 555 necesita lo mismo que en la ambulancia para funcionar.", "Algo tiene que darle el toque de arranque.", "Un solo toque basta: el chip cuenta el tiempo solo.", "¿En qué modo se queda el chip esperando un toque?"},
                "Presiona el botón una vez y deja que la luz del pasillo se apague sola después de 3 segundos."
        ).conRamas(new String[]{"SW:BOTON", "555M:MONO", "LED", "R"}, new String[]{"", "|", "CAP"})
         .conIcono("555M")
         .conConsejo("Aunque sueltes el botón, la luz sigue prendida: el 555 cuenta el tiempo solo.");
    }

    @Override
    public float getTiempoRequeridoVictoria() {
        return Temporizador555.DURACION_PULSO;
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
        Switch boton = buscarSwitches().isEmpty() ? null : buscarSwitches().get(0);
        if (chip == null || capacitor == null || boton == null || buscarCablesConTerminal(positivo).isEmpty()) {
            mensajeEstado = mensajeColocarPiezas("Objetivo: una luz que se apague sola.");
            return false;
        }

        Cable cableVcc = cableEntre(positivo, chip.getTerminalVcc());
        Cable cableGnd = cableEntre(chip.getTerminalTierra(), negativo);
        Cable cableC = cableEntre(chip.getTerminalCapacitor(), capacitor.getTerminalPositivo());
        Cable capATierra = cableEntre(capacitor.getTerminalNegativo(), negativo);
        paso1Listo = cableVcc != null && cableGnd != null && cableC != null && capATierra != null;

        Cable haciaBoton = cableEntre(positivo, boton.getTerminalEntrada());
        Cable botonATrig = cableEntre(boton.getTerminalSalida(), chip.getTerminalDisparo());
        paso2Listo = haciaBoton != null && botonATrig != null;

        Recorrido salida = recorrerHastaTierra(chip.getTerminalSalida(), fuente);
        if (!revisarErroresDeSalida(salida, chip.isSalidaAlta(), fuente)) return false;
        paso3Listo = salida.llegoATierra && salida.pasoPorLed && salida.pasoPorResistencia;

        boolean listo = paso1Listo && paso2Listo && paso3Listo && !chip.isAstable();
        boolean disparo = paso2Listo && boton.isCerrado();
        chip.avanzar(deltaActual, listo, disparo);

        if (cableVcc == null || cableGnd == null) {
            mensajeEstado = "El 555 no tiene energía o su regreso está abierto.";
            return false;
        }
        cableVcc.setTieneEnergia(true);
        cableGnd.setTieneEnergia(true);
        if (revisarCapacitorAlReves(capacitor, chip.getTerminalCapacitor(), fuente, true)) return false;
        if (cableC == null || capATierra == null) {
            mensajeEstado = "El 555 no tiene con qué medir el tiempo.";
            return false;
        }
        cableC.setTieneEnergia(true);
        capATierra.setTieneEnergia(true);
        if (!paso2Listo) {
            mensajeEstado = "Nada puede darle el toque de arranque al 555.";
            return false;
        }
        haciaBoton.setTieneEnergia(true);
        if (boton.isCerrado()) botonATrig.setTieneEnergia(true);
        boton.getTerminalEntrada().setValorLogico(true);
        boton.evaluarEstado();
        if (!revisarSalidaCompleta(salida)) {
            mensajeEstado = "Salida OUT: " + mensajeEstado;
            return false;
        }
        if (chip.isAstable()) {
            mensajeEstado = "En este modo el 555 no espera ningún toque.";
            return false;
        }

        capacitor.setCarga(chip.isPulsoActivo() ? chip.getProgresoCiclo() : 0f);
        capacitor.evaluarEstado();
        chip.evaluarEstado();
        if (chip.isSalidaAlta()) encenderTramoDeSalida(salida, fuente);

        if (ganado) {
            mensajeEstado = "¡Nivel superado! La luz se apagó sola.";
        } else if (chip.isPulsoActivo()) {
            mensajeEstado = String.format("Luz encendida: se apagará sola en %.1f s", Temporizador555.DURACION_PULSO - chip.getTiempo());
        } else if (boton.isCerrado()) {
            mensajeEstado = "Suelta el botón y vuelve a presionarlo para dar un toque.";
        } else {
            mensajeEstado = "Todo listo. Presiona el botón una vez.";
        }
        return chip.isSalidaAlta();
    }

    @Override
    protected void actualizarVictoria(boolean circuitoActivo, float deltaTiempo) {
        if (ganado) return;
        Temporizador555 chip = buscar555();
        if (chip == null) return;
        tiempoCircuitoActivo = chip.isPulsoActivo() ? chip.getTiempo() : 0f;
        if (chip.getPulsos() >= 1) {
            ganado = true;
            completado = true;
            tiempoCircuitoActivo = Temporizador555.DURACION_PULSO;
            mensajeEstado = "¡Nivel superado! La luz se apagó sola.";
        }
    }
}
