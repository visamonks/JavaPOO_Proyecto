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
        nivel.crearYColocarComponente("SWITCH", 440f, 575f);
        nivel.crearYColocarComponente("RESISTENCIA_220", 810f, 544f);
        nivel.crearYColocarComponente("LED", 1230f, 440f);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) interruptor = (Switch) c;
            else if (c instanceof Resistencia) resistencia = (Resistencia) c;
            else if (c instanceof LED) led = (LED) c;
        }
    }

    private void conectarCircuito() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), interruptor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(interruptor.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testNivelEmpiezaSoloConLaFuente() {
        Nivel2 nuevo = new Nivel2();
        assertEquals(1, nuevo.getComponentes().size(), "Solo la fuente debe estar en la mesa al iniciar.");
        assertTrue(nuevo.getComponentes().get(0) instanceof FuenteAlimentacion);
        assertEquals(1, nuevo.buscarCasilla("SWITCH").getCantidadDisponible());
        assertTrue(nuevo.getMensajeEstado().contains("Arrastra a la mesa"));
    }

    @Test
    public void testSwitchApagadoDetieneCorriente() {
        conectarCircuito();
        interruptor.setCerrado(false);

        nivel.actualizar(0.1f);

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

        interruptor.setCerrado(true);
        nivel.actualizar(1.0f);

        for (Cable c : nivel.getCables()) {
            assertTrue(c.isTieneEnergia(), "Los cables deben tener energía con el circuito cerrado.");
        }
        assertEquals("EXITO", interruptor.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado(), "Aún no debe ganar porque no han pasado 5 segundos.");

        interruptor.conmutar();
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

        nivel.actualizar(2.0f);
        assertEquals(2.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(2.9f);
        assertEquals(4.9f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado(), "No debe ganar con 4.9 segundos continuos.");

        nivel.actualizar(0.2f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado(), "Debe ganar tras 5.0 segundos continuos.");
        assertTrue(nivel.estaCompletado());
    }

    @Test
    public void testInterrupcionAntesDeCincoSegundosCancelaVictoria() {
        conectarCircuito();
        interruptor.setCerrado(true);

        nivel.actualizar(4.5f);
        assertEquals(4.5f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        interruptor.setCerrado(false);
        nivel.actualizar(0.1f);

        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f, "Temporizador debe reiniciarse a 0.");
        assertFalse(nivel.estaGanado());

        interruptor.setCerrado(true);
        nivel.actualizar(3.0f);
        assertEquals(3.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(2.0f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado(), "Ahora sí gana con 5 segundos continuos.");
    }
}
