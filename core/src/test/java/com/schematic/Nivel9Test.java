package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel9;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel9Test {

    private Nivel9 nivel;
    private FuenteAlimentacion fuente;
    private Temporizador555 chip;
    private Switch boton;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel9) GestorNiveles.crearNivel(9);
        boton = (Switch) nivel.crearYColocarComponente("SWITCH", 330f, 700f);
        chip = (Temporizador555) nivel.crearYColocarComponente("555", 600f, 400f);
        Capacitor capacitor = (Capacitor) nivel.crearYColocarComponente("CAPACITOR", 1000f, 160f);
        Resistencia resistencia = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 1000f, 640f);
        led = (LED) nivel.crearYColocarComponente("LED", 1300f, 450f);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
        }
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), chip.getTerminalVcc()));
        nivel.agregarCable(new Cable(chip.getTerminalTierra(), fuente.getTerminalNegativo()));
        nivel.agregarCable(new Cable(chip.getTerminalCapacitor(), capacitor.getTerminalPositivo()));
        nivel.agregarCable(new Cable(capacitor.getTerminalNegativo(), fuente.getTerminalNegativo()));
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), boton.getTerminalEntrada()));
        nivel.agregarCable(new Cable(boton.getTerminalSalida(), chip.getTerminalDisparo()));
        nivel.agregarCable(new Cable(chip.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testSinTocarElBotonLaLuzEstaApagada() {
        nivel.actualizar(1.0f);
        assertEquals("NEUTRO", led.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("Presiona el botón"));
    }

    @Test
    public void testUnToqueLaPrendeYSeApagaSola() {
        boton.setCerrado(true);
        nivel.actualizar(0.1f);
        boton.setCerrado(false);
        nivel.actualizar(1.0f);
        assertEquals("EXITO", led.getEstadoActual(), "Sigue prendida aunque se soltó el botón.");
        nivel.actualizar(1.0f);
        assertFalse(nivel.estaGanado());
        nivel.actualizar(1.0f);
        assertEquals("NEUTRO", led.getEstadoActual());
        assertTrue(nivel.estaGanado());
    }

    @Test
    public void testEnAstableNoFuncionaElPasillo() {
        chip.cambiarModo();
        boton.setCerrado(true);
        nivel.actualizar(0.5f);
        assertTrue(nivel.getMensajeEstado().contains("toque"));
        assertEquals(0, chip.getPulsos());
    }

    @Test
    public void testDejarElBotonPresionadoNoRepiteElPulso() {
        boton.setCerrado(true);
        for (int i = 0; i < 8; i++) nivel.actualizar(0.5f);
        assertEquals(1, chip.getPulsos());
        assertEquals("NEUTRO", led.getEstadoActual());
    }

    @Test
    public void testElBotonSinConectarATrigNoDispara() {
        Nivel9 otro = (Nivel9) GestorNiveles.crearNivel(9);
        otro.actualizar(0.1f);
        assertTrue(otro.getMensajeEstado().contains("Arrastra a la mesa"));
    }
}
