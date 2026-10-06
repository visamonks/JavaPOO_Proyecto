package com.schematic.model.niveles;

import com.schematic.model.componentes.*;

public class Nivel3 extends Nivel {

    public Nivel3() {
        super(3, "NIVEL 3: COMPUERTA LÓGICA AND", 30.0f);
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
        this.mensajeEstado = "Objetivo: Conecta y activa ambas entradas de la compuerta AND.";

        FuenteAlimentacion fuente = new FuenteAlimentacion("FUENTE_PODER", 80f, 280f);
        this.componentes.add(fuente);

        this.inventario.add(new CasillaInventario("LED", "LED (Diodo)", 1));
        this.inventario.add(new CasillaInventario("RESISTENCIA_220", "220 Ω", 1));
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
                    this.mensajeEstado = "¡NIVEL 3 COMPLETADO! Compuerta AND validada correctamente.";
                    return true;
                }
            }
        }
        return false;
    }
}

