package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel8;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel8Test {

    private Nivel8 nivel;
    private FuenteAlimentacion fuente;
    private Temporizador555 chip;
    private Capacitor capacitor;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel8) GestorNiveles.crearNivel(8);
        chip = (Temporizador555) nivel.crearYColocarComponente("555", 380f, 420f);
        capacitor = (Capacitor) nivel.crearYColocarComponente("CAPACITOR", 820f, 160f);
        resistencia = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 820f, 640f);
        led = (LED) nivel.crearYColocarComponente("LED_ROJO", 1250f, 500f);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
        }
    }

    private void conectarTodo(boolean conCapacitor) {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), chip.getTerminalVcc()));
        nivel.agregarCable(new Cable(chip.getTerminalTierra(), fuente.getTerminalNegativo()));
        if (conCapacitor) {
            nivel.agregarCable(new Cable(chip.getTerminalCapacitor(), capacitor.getTerminalPositivo()));
            nivel.agregarCable(new Cable(capacitor.getTerminalNegativo(), fuente.getTerminalNegativo()));
        }
        nivel.agregarCable(new Cable(chip.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testCapacitorAlRevesEnElPinCExplota() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), chip.getTerminalVcc()));
        nivel.agregarCable(new Cable(chip.getTerminalTierra(), fuente.getTerminalNegativo()));
        nivel.agregarCable(new Cable(chip.getTerminalCapacitor(), capacitor.getTerminalNegativo()));
        nivel.agregarCable(new Cable(capacitor.getTerminalPositivo(), fuente.getTerminalNegativo()));
        nivel.actualizar(0.1f);
        assertTrue(capacitor.isExplotado());
        assertTrue(nivel.getMensajeEstado().contains("EXPLOT"));
    }

    @Test
    public void testElChipEmpiezaDormidoEnMonoestable() {
        assertFalse(chip.isAstable());
        conectarTodo(true);
        nivel.actualizar(0.5f);
        assertTrue(nivel.getMensajeEstado().contains("MONOESTABLE"));
        assertEquals(0, chip.getPulsos());
    }

    @Test
    public void testEnAstableElLedParpadeaSolo() {
        conectarTodo(true);
        chip.cambiarModo();
        nivel.actualizar(0.5f);
        assertEquals("EXITO", led.getEstadoActual());
        nivel.actualizar(0.5f);
        assertEquals("NEUTRO", led.getEstadoActual());
        nivel.actualizar(0.5f);
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals(2, chip.getPulsos());
    }

    @Test
    public void testGanaConCincoParpadeos() {
        conectarTodo(true);
        chip.cambiarModo();
        for (int i = 0; i < 8; i++) nivel.actualizar(0.5f);
        assertFalse(nivel.estaGanado());
        nivel.actualizar(0.5f);
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.getMensajeEstado().contains("superado"));
    }

    @Test
    public void testSinCapacitorNoMideElTiempo() {
        conectarTodo(false);
        chip.cambiarModo();
        for (int i = 0; i < 4; i++) nivel.actualizar(0.5f);
        assertEquals(0, chip.getPulsos());
        assertTrue(nivel.getMensajeEstado().contains("medir el tiempo"));
    }

    @Test
    public void testSinLedNoCuentanLosParpadeos() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), chip.getTerminalVcc()));
        nivel.agregarCable(new Cable(chip.getTerminalTierra(), fuente.getTerminalNegativo()));
        nivel.agregarCable(new Cable(chip.getTerminalCapacitor(), capacitor.getTerminalPositivo()));
        nivel.agregarCable(new Cable(capacitor.getTerminalNegativo(), fuente.getTerminalNegativo()));
        chip.cambiarModo();
        for (int i = 0; i < 4; i++) nivel.actualizar(0.5f);
        assertEquals(0, chip.getPulsos());
    }

    @Test
    public void testCambiarDeModoCambiaLaCara() {
        chip.cambiarModo();
        assertTrue(chip.isAstable());
        chip.cambiarModo();
        assertFalse(chip.isAstable());
    }
}
