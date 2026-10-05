package com.schematic.model;

public class Nivel4 extends Nivel {

    public Nivel4() {
        super(4, "NIVEL 4: COMPUERTA LÓGICA OR", 30.0f);
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
        this.tiempoLimite = 30.0f;
        this.tiempoRestante = this.tiempoLimite;
        this.mensajeEstado = "Objetivo: Conecta la compuerta OR para permitir el paso de señal.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        this.componentes.add(fuente);

        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ω", 1));
        this.inventario.add(new CasillaInventario("OR", "Compuerta OR", 1));
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
                    this.mensajeEstado = "¡NIVEL 4 COMPLETADO! Señal OR activada.";
                    return true;
                }
            }
        }
        return false;
    }
}

