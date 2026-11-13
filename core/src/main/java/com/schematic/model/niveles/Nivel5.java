package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel5 extends Nivel {

    public static final float TIEMPO_LIMITE = 120.0f;
    public static final float TIEMPO_POR_LADO = 2.5f;

    private float tiempoIzquierda;
    private float tiempoDerecha;
    private boolean izquierdaPrendida;
    private boolean derechaPrendida;
    private boolean paso1Listo;
    private boolean paso2Listo;
    private boolean paso3Listo;

    public Nivel5() {
        super(5, "NIVEL 5: LAS DIRECCIONALES", TIEMPO_LIMITE);
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
        this.tiempoIzquierda = 0f;
        this.tiempoDerecha = 0f;
        this.izquierdaPrendida = false;
        this.derechaPrendida = false;
        this.paso1Listo = false;
        this.paso2Listo = false;
        this.paso3Listo = false;

        this.componentes.add(new FuenteAlimentacion("FUENTE_PODER", 80f, 280f));
        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 2));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ohm", 2));
        this.inventario.add(new CasillaInventario("SWITCH_DIVISOR", "Palanca", 1));
        this.mensajeEstado = mensajeColocarPiezas("Objetivo: una palanca y dos direccionales.");
    }

    @Override
    public TutorialNivel obtenerTutorial() {
        return new TutorialNivel(
                "LAS DIRECCIONALES",
                "Un circuito puede tener ramas en paralelo: cada LED tiene su propio camino y su propia resistencia, así se prenden por separado.",
                "Las direccionales del carro: con una sola palanca prendes la luz de la izquierda o la de la derecha.",
                TutorialNivel.Esquema.RAMAS,
                new String[]{"Cada lado de la palanca manda la corriente por un camino distinto.", "Dos luces que trabajan por separado necesitan caminos separados.", "Cada LED necesita su propia protección.", "Todos los caminos terminan en el mismo lugar."},
                "Usa cada direccional 2.5 segundos: primero una y luego la otra."
        ).conRamas(new String[]{"SW:A", "LED:IZQ", "R"}, new String[]{"SW:B", "LED:DER", "R"})
         .conIcono("LED_DOS")
         .conConsejo("Mitad de arriba de la palanca = izquierda (A). Mitad de abajo = derecha (B).");
    }

    @Override
    public float getTiempoRequeridoVictoria() {
        return TIEMPO_POR_LADO * 2f;
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        paso1Listo = false;
        paso2Listo = false;
        paso3Listo = false;
        izquierdaPrendida = false;
        derechaPrendida = false;
        apagarTodo();

        FuenteAlimentacion fuente = buscarFuente();
        if (fuente == null) return false;
        Terminal positivo = fuente.getTerminalPositivo();
        positivo.setValorLogico(true);
        if (hayCortoEnFuente(fuente)) return false;

        Switch palanca = null;
        for (Switch sw : buscarSwitches()) {
            if (sw.isDoble()) palanca = sw;
        }
        if (palanca == null || buscarCablesConTerminal(positivo).isEmpty()) {
            mensajeEstado = mensajeColocarPiezas("Objetivo: una palanca y dos direccionales.");
            return false;
        }

        Cable entrada = cableEntre(positivo, palanca.getTerminalEntrada());
        if (entrada == null) {
            mensajeEstado = "La palanca no recibe energía por su entrada.";
            return false;
        }
        paso1Listo = true;
        entrada.setTieneEnergia(true);
        palanca.getTerminalEntrada().setValorLogico(true);
        palanca.evaluarEstado();

        Recorrido izquierda = recorrerHastaTierra(palanca.getTerminalSalidaA(), fuente);
        Recorrido derecha = recorrerHastaTierra(palanca.getTerminalSalidaB(), fuente);
        if (!revisarErroresDeSalida(izquierda, palanca.isCerradoA(), fuente)) return false;
        if (!revisarErroresDeSalida(derecha, palanca.isCerradoB(), fuente)) return false;

        paso2Listo = izquierda.llegoATierra && izquierda.pasoPorLed && izquierda.pasoPorResistencia;
        paso3Listo = derecha.llegoATierra && derecha.pasoPorLed && derecha.pasoPorResistencia;

        if (paso2Listo && paso3Listo && izquierda.led == derecha.led) {
            mensajeEstado = "Cada direccional necesita su propio LED.";
            return false;
        }
        if (!paso2Listo) {
            revisarSalidaCompleta(izquierda);
            mensajeEstado = "Izquierda (A): " + mensajeEstado;
            return false;
        }
        if (!paso3Listo) {
            revisarSalidaCompleta(derecha);
            mensajeEstado = "Derecha (B): " + mensajeEstado;
            return false;
        }

        izquierdaPrendida = palanca.isCerradoA();
        derechaPrendida = palanca.isCerradoB();
        if (izquierdaPrendida) encenderTramoDeSalida(izquierda, fuente);
        if (derechaPrendida) encenderTramoDeSalida(derecha, fuente);

        if (ganado) {
            mensajeEstado = "¡Nivel superado! Las dos direccionales funcionan.";
        } else if (!izquierdaPrendida && !derechaPrendida) {
            mensajeEstado = "Todo listo: prueba la palanca hacia los dos lados.";
        } else {
            mensajeEstado = String.format("Izquierda %.1f de %.1f s, derecha %.1f de %.1f s",
                    tiempoIzquierda, TIEMPO_POR_LADO, tiempoDerecha, TIEMPO_POR_LADO);
        }
        return izquierdaPrendida || derechaPrendida;
    }

    @Override
    protected void actualizarVictoria(boolean circuitoActivo, float deltaTiempo) {
        if (izquierdaPrendida) tiempoIzquierda = Math.min(TIEMPO_POR_LADO, tiempoIzquierda + deltaTiempo);
        if (derechaPrendida) tiempoDerecha = Math.min(TIEMPO_POR_LADO, tiempoDerecha + deltaTiempo);
        tiempoCircuitoActivo = tiempoIzquierda + tiempoDerecha;
        ganado = tiempoIzquierda >= TIEMPO_POR_LADO && tiempoDerecha >= TIEMPO_POR_LADO;
        completado = ganado;
        if (ganado) mensajeEstado = "¡Nivel superado! Las dos direccionales funcionan.";
    }

    public float getTiempoIzquierda() { return tiempoIzquierda; }
    public float getTiempoDerecha() { return tiempoDerecha; }
}
