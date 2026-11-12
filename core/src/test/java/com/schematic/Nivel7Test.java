package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel7;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel7Test {

    private Nivel7 nivel;
    private FuenteAlimentacion fuente;
    private Switch sw;
    private Capacitor capacitor;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel7) GestorNiveles.crearNivel(7);
        sw = (Switch) nivel.crearYColocarComponente("SWITCH", 420f, 600f);
        capacitor = (Capacitor) nivel.crearYColocarComponente("CAPACITOR", 720f, 250f);
        resistencia = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 820f, 640f);
        led = (LED) nivel.crearYColocarComponente("LED", 1250f, 500f);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
        }
    }

    private void conectarTodo() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), capacitor.getTerminalPositivo()));
        nivel.agregarCable(new Cable(capacitor.getTerminalNegativo(), fuente.getTerminalNegativo()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testElCapacitorSeCargaConElSwitchPrendido() {
        conectarTodo();
        sw.setCerrado(true);
        nivel.actualizar(1.0f);
        assertEquals(0.5f, capacitor.getCarga(), 0.01f);
        nivel.actualizar(1.0f);
        assertEquals(1.0f, capacitor.getCarga(), 0.01f);
        assertEquals("EXITO", led.getEstadoActual());
    }

    @Test
    public void testSinFuenteElCapacitorMantieneElLedYSeApagaDespacio() {
        conectarTodo();
        sw.setCerrado(true);
        nivel.actualizar(2.0f);
        sw.setCerrado(false);
        nivel.actualizar(1.0f);
        assertEquals("EXITO", led.getEstadoActual(), "El capacitor debe mantener el LED.");
        assertTrue(led.getBrillo() < 1f, "El LED debe verse más débil.");
        assertTrue(capacitor.getCarga() < 1f);
    }

    @Test
    public void testGanaConTresSegundosSoloConElCapacitor() {
        conectarTodo();
        sw.setCerrado(true);
        nivel.actualizar(2.0f);
        sw.setCerrado(false);
        nivel.actualizar(1.0f);
        nivel.actualizar(1.0f);
        assertFalse(nivel.estaGanado());
        nivel.actualizar(1.0f);
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.getMensajeEstado().contains("superado"));
    }

    @Test
    public void testConPocaCargaNoAlcanzaParaGanar() {
        conectarTodo();
        sw.setCerrado(true);
        nivel.actualizar(0.5f);
        sw.setCerrado(false);
        for (int i = 0; i < 6; i++) nivel.actualizar(0.5f);
        assertFalse(nivel.estaGanado());
        assertEquals("NEUTRO", led.getEstadoActual());
    }

    @Test
    public void testCapacitorAlReves() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), capacitor.getTerminalNegativo()));
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().contains("al revés"));
        assertFalse(capacitor.isExplotado());
    }

    @Test
    public void testCapacitorAlRevesExplotaConCorriente() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), capacitor.getTerminalNegativo()));
        nivel.agregarCable(new Cable(capacitor.getTerminalPositivo(), fuente.getTerminalNegativo()));
        sw.setCerrado(true);
        nivel.actualizar(0.1f);
        assertTrue(capacitor.isExplotado());
        assertEquals("ERROR", capacitor.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("EXPLOT"));

        sw.setCerrado(false);
        nivel.actualizar(0.1f);
        assertTrue(capacitor.isExplotado());
        assertFalse(capacitor.tieneCarga());
    }

    @Test
    public void testLedSinResistenciaSeQuema() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), capacitor.getTerminalPositivo()));
        nivel.agregarCable(new Cable(capacitor.getTerminalNegativo(), fuente.getTerminalNegativo()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
        sw.setCerrado(true);
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().contains("QUEMADO"));
    }
}
