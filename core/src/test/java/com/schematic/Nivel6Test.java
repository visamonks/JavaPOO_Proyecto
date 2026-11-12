package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel6;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel6Test {

    private Nivel6 nivel;
    private FuenteAlimentacion fuente;
    private final Switch[] switches = new Switch[3];
    private final LED[] leds = new LED[3];

    @BeforeEach
    public void setUp() {
        nivel = (Nivel6) GestorNiveles.crearNivel(6);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
        }
        String[] tipos = {"LED_VERDE", "LED_AMARILLO", "LED_ROJO"};
        for (int i = 0; i < 3; i++) {
            float y = 720f - i * 260f;
            switches[i] = (Switch) nivel.crearYColocarComponente("SWITCH", 420f, y);
            Resistencia r = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 760f, y - 30f);
            leds[i] = (LED) nivel.crearYColocarComponente(tipos[i], 1150f, y - 70f);
            nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switches[i].getTerminalEntrada()));
            nivel.agregarCable(new Cable(switches[i].getTerminalSalida(), leds[i].getTerminalAnodo()));
            nivel.agregarCable(new Cable(leds[i].getTerminalCatodo(), r.getTerminalEntrada()));
            nivel.agregarCable(new Cable(r.getTerminalSalida(), fuente.getTerminalNegativo()));
        }
    }

    private void soloPrender(int indice) {
        for (int i = 0; i < 3; i++) switches[i].setCerrado(i == indice);
    }

    @Test
    public void testLosLedsTienenSuColor() {
        assertEquals("VERDE", leds[0].getColor());
        assertEquals("AMARILLO", leds[1].getColor());
        assertEquals("ROJO", leds[2].getColor());
    }

    @Test
    public void testElLedRegresaASuCasillaDeColor() {
        nivel.devolverAlInventario(leds[2]);
        assertEquals(1, nivel.buscarCasilla("LED_ROJO").getCantidadDisponible());
        assertEquals(0, nivel.buscarCasilla("LED_VERDE").getCantidadDisponible());
    }

    @Test
    public void testSecuenciaCorrectaGana() {
        for (int i = 0; i < 3; i++) {
            soloPrender(i);
            nivel.actualizar(Nivel6.TIEMPO_POR_LUZ);
            assertEquals(i + 1, nivel.getPasoSecuencia());
        }
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.getMensajeEstado().contains("superado"));
    }

    @Test
    public void testEmpezarConElRojoNoAvanza() {
        soloPrender(2);
        nivel.actualizar(3f);
        assertEquals(0, nivel.getPasoSecuencia());
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testDosLucesALaVezNoCuentan() {
        switches[0].setCerrado(true);
        switches[1].setCerrado(true);
        nivel.actualizar(3f);
        assertEquals(0, nivel.getPasoSecuencia());
        assertTrue(nivel.getMensajeEstado().contains("Solo una luz"));
    }

    @Test
    public void testLuzEquivocadaReiniciaLaCuentaDeEsaLuz() {
        soloPrender(0);
        nivel.actualizar(1.0f);
        soloPrender(2);
        nivel.actualizar(0.1f);
        soloPrender(0);
        nivel.actualizar(1.0f);
        assertEquals(0, nivel.getPasoSecuencia(), "La cuenta del verde empezó de nuevo.");
        nivel.actualizar(0.6f);
        assertEquals(1, nivel.getPasoSecuencia());
    }

    @Test
    public void testFaltaUnaRama() {
        Nivel6 otro = (Nivel6) GestorNiveles.crearNivel(6);
        otro.actualizar(0.1f);
        assertTrue(otro.getMensajeEstado().contains("Arrastra a la mesa"));
    }
}
