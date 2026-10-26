package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.Nivel2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CircuitoTest {

    private Nivel2 nivel;
    private FuenteAlimentacion fuente;
    private Switch interruptor;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = new Nivel2();
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) interruptor = (Switch) c;
            else if (c instanceof Resistencia) resistencia = (Resistencia) c;
            else if (c instanceof LED) led = (LED) c;
        }
    }

    private void conectarCircuito() {
        // Conectar: Fuente(+) -> Switch -> Resistencia -> LED -> Fuente(-)
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), interruptor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(interruptor.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testSwitchApagadoDetieneCorriente() {
        conectarCircuito();
        interruptor.setCerrado(false);

        nivel.actualizar(0.1f);

        // Con switch abierto, no debe haber corriente en ningún cable
        for (Cable c : nivel.getCables()) {
            assertFalse(c.isTieneEnergia(), "Ningún cable debe tener corriente si el switch está apagado.");
        }
        assertEquals("NEUTRO", interruptor.getEstadoActual());
        assertEquals("NEUTRO", led.getEstadoActual());
        assertFalse(nivel.estaGanado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
    }

    @Test
    public void testFlujoCorrienteAlEncenderYDetenerAlApagar() {
        conectarCircuito();

        // 1. Encender el switch
        interruptor.setCerrado(true);
        nivel.actualizar(1.0f);

        for (Cable c : nivel.getCables()) {
            assertTrue(c.isTieneEnergia(), "Los cables deben tener energía con el circuito cerrado.");
        }
        assertEquals("EXITO", interruptor.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado(), "Aún no debe ganar porque no han pasado 5 segundos.");

        // 2. Apagar el switch
        interruptor.conmutar(); // Ahora cerrado es false
        nivel.actualizar(0.1f);

        for (Cable c : nivel.getCables()) {
            assertFalse(c.isTieneEnergia(), "La corriente debe detenerse inmediatamente al apagar el switch.");
        }
        assertEquals("NEUTRO", interruptor.getEstadoActual());
        assertEquals("NEUTRO", led.getEstadoActual());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f, "El temporizador debe reiniciarse a 0 al apagar el switch.");
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testQuitarSwitchDetieneCorrienteYReiniciaTemporizador() {
        conectarCircuito();
        interruptor.setCerrado(true);
        nivel.actualizar(2.5f);

        assertEquals(2.5f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // Quitar el switch del circuito
        nivel.devolverAlInventario(interruptor);
        nivel.actualizar(0.1f);

        for (Cable c : nivel.getCables()) {
            assertFalse(c.isTieneEnergia(), "No debe haber corriente tras quitar el switch.");
        }
        assertEquals("NEUTRO", led.getEstadoActual());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f, "El temporizador debe reiniciarse a 0.");
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testTemporizadorCincoSegundosContinuosParaGanar() {
        conectarCircuito();
        interruptor.setCerrado(true);

        // Avanzar 2 segundos
        nivel.actualizar(2.0f);
        assertEquals(2.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // Avanzar 2.9 segundos (total 4.9s)
        nivel.actualizar(2.9f);
        assertEquals(4.9f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado(), "No debe ganar con 4.9 segundos continuos.");

        // Avanzar 0.2 segundos (total >= 5.0s)
        nivel.actualizar(0.2f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado(), "Debe ganar tras 5.0 segundos continuos.");
        assertTrue(nivel.estaCompletado());
    }

    @Test
    public void testInterrupcionAntesDeCincoSegundosCancelaVictoria() {
        conectarCircuito();
        interruptor.setCerrado(true);

        // Mantener activo 4.5 segundos
        nivel.actualizar(4.5f);
        assertEquals(4.5f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // Interrumpir el circuito apagando el switch
        interruptor.setCerrado(false);
        nivel.actualizar(0.1f);

        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f, "Temporizador debe reiniciarse a 0.");
        assertFalse(nivel.estaGanado());

        // Volver a encender: debe requerir otros 5 segundos completos desde cero
        interruptor.setCerrado(true);
        nivel.actualizar(3.0f);
        assertEquals(3.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(2.0f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado(), "Ahora sí gana con 5 segundos continuos.");
    }
}

