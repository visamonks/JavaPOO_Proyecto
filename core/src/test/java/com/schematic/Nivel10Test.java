package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel10;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel10Test {

    private Nivel10 nivel;
    private FuenteAlimentacion fuente;
    private Switch sw;
    private Potenciometro perilla;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel10) GestorNiveles.crearNivel(10);
        sw = (Switch) nivel.crearYColocarComponente("SWITCH", 420f, 600f);
        perilla = (Potenciometro) nivel.crearYColocarComponente("POTENCIOMETRO", 720f, 400f);
        resistencia = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 1000f, 700f);
        led = (LED) nivel.crearYColocarComponente("LED", 1300f, 400f);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
        }
    }

    private void conectarTodo() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), perilla.getTerminalEntrada()));
        nivel.agregarCable(new Cable(perilla.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testLaPerillaEmpiezaAlMaximoYSeMueveDeDiezEnDiez() {
        assertEquals(100, perilla.getPorcentaje());
        perilla.bajar();
        assertEquals(90, perilla.getPorcentaje());
        for (int i = 0; i < 20; i++) perilla.bajar();
        assertEquals(0, perilla.getPorcentaje());
        perilla.subir();
        assertEquals(10, perilla.getPorcentaje());
    }

    @Test
    public void testLaPerillaCambiaElBrilloDelLed() {
        conectarTodo();
        sw.setCerrado(true);
        perilla.setValor(0.3f);
        nivel.actualizar(0.1f);
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals(0.3f, led.getBrillo(), 0.01f);
    }

    @Test
    public void testAlMaximoNoCuentaElTiempo() {
        conectarTodo();
        sw.setCerrado(true);
        nivel.actualizar(4f);
        assertFalse(nivel.estaGanado());
        assertTrue(nivel.getMensajeEstado().contains("bájala"));
    }

    @Test
    public void testGanaConLuzMediaTresSegundos() {
        conectarTodo();
        sw.setCerrado(true);
        perilla.setValor(0.5f);
        nivel.actualizar(2.9f);
        assertFalse(nivel.estaGanado());
        nivel.actualizar(0.2f);
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.getMensajeEstado().contains("superado"));
    }

    @Test
    public void testSinPotenciometroEnElCaminoNoSePuedeRegular() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
        sw.setCerrado(true);
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().contains("potenciómetro"));
    }

    @Test
    public void testSinResistenciaElLedSeQuemaAunqueHayaPerilla() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), sw.getTerminalEntrada()));
        nivel.agregarCable(new Cable(sw.getTerminalSalida(), perilla.getTerminalEntrada()));
        nivel.agregarCable(new Cable(perilla.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
        sw.setCerrado(true);
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().contains("QUEMADO"));
    }
}
