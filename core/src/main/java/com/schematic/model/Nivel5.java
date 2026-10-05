package com.schematic.model;

public class Nivel5 extends Nivel {

    public Nivel5() {
        super(5, "NIVEL 5: CIRCUITO COMBINADO", 35.0f);
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
        this.tiempoLimite = 35.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.mensajeEstado = "Objetivo: Resuelve el circuito esquemático combinado completo.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        this.componentes.add(fuente);

        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ω", 1));
        this.inventario.add(new CasillaInventario("SWITCH", "Interruptor", 1));
        this.inventario.add(new CasillaInventario("AND", "Compuerta AND", 1));
    }

    @Override
    public boolean evaluarCircuitoCompleto() {
        for (Cable c : cables) {
            c.propagarSenal();
        }
        for (Componente comp : componentes) {
            comp.evaluarEstado();
        }

        for (Componente comp : componentes) {
            if (comp instanceof LED) {
                if ("EXITO".equals(comp.getEstadoActual())) {
                    this.ganado = true;
                    this.completado = true;
                    this.mensajeEstado = "¡FELICITACIONES! Has superado el circuito final.";
                    return true;
                }
            }
        }
        return false;
    }
}

