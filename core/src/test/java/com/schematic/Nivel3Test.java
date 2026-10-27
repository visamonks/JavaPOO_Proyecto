package com.schematic;

import com.schematic.constants.Constantes;
import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel;
import com.schematic.model.niveles.Nivel3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel3Test {

    private Nivel3 nivel;
    private FuenteAlimentacion fuente;
    private Switch switchDivisor;
    private CompuertaAND compuertaAnd;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel3) GestorNiveles.crearNivel(3);
        assertNotNull(nivel, "GestorNiveles debe instanciar Nivel3 para el nivel 3.");

        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) switchDivisor = (Switch) c;
            else if (c instanceof CompuertaAND) compuertaAnd = (CompuertaAND) c;
            else if (c instanceof Resistencia) resistencia = (Resistencia) c;
            else if (c instanceof LED) led = (LED) c;
        }
    }

    private void conectarCircuitoCompuertaANDCompleto() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaAnd.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaAnd.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaAnd.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testInicializacionComponentesTiempoEInventario() {
        assertEquals(3, nivel.getNumeroNivel());
        assertEquals("NIVEL 3: COMPUERTA LÓGICA AND", nivel.getTituloReto());
        assertEquals(50.0f, nivel.getTiempoLimite(), 0.001f);
        assertEquals(50.0f, nivel.getTiempoRestante(), 0.001f);
        assertEquals(5, nivel.getComponentes().size(), "Debe haber 5 componentes colocados en el tablero.");

        assertNotNull(fuente);
        assertNotNull(switchDivisor);
        assertTrue(switchDivisor.isDoble(), "El Switch del nivel 3 debe ser un Switch Divisor doble (A/B).");
        assertNotNull(compuertaAnd);
        assertNotNull(resistencia);
        assertNotNull(led);

        Nivel.CasillaInventario casSw = nivel.buscarCasilla("SWITCH");
        assertNotNull(casSw);
        assertEquals(1, casSw.getCantidadTotal());
        assertEquals(0, casSw.getCantidadDisponible());

        Nivel.CasillaInventario casAnd = nivel.buscarCasilla("AND");
        assertNotNull(casAnd);
        assertEquals(1, casAnd.getCantidadTotal());
        assertEquals(0, casAnd.getCantidadDisponible());

        Nivel.CasillaInventario casRes = nivel.buscarCasilla("RESISTENCIA_220");
        assertNotNull(casRes);
        assertEquals(1, casRes.getCantidadTotal());
        assertEquals(0, casRes.getCantidadDisponible());

        Nivel.CasillaInventario casLed = nivel.buscarCasilla("LED");
        assertNotNull(casLed);
        assertEquals(1, casLed.getCantidadTotal());
        assertEquals(0, casLed.getCantidadDisponible());
    }

    @Test
    public void testCortocircuitoDirecto() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), fuente.getTerminalNegativo()));
        nivel.actualizar(0.1f);

        assertEquals("ERROR", fuente.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("CORTOCIRCUITO DIRECTO"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testCircuitoSinResistenciaQuemaLEDAlActivarAND() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaAnd.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaAnd.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaAnd.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(0.1f);

        assertTrue(led.estaQuemado());
        assertEquals("ERROR", led.getEstadoActual());
        assertEquals("ERROR", fuente.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("EL LED SE HA QUEMADO"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testPolaridadInvertidaLED() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaAnd.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaAnd.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaAnd.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalCatodo()));

        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("Polaridad invertida"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testFaltaSegundaEntradaEnCompuertaAND() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaAnd.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(compuertaAnd.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switchDivisor.setCerradoA(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("IN B"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testTablaDeVerdadCompuertaAND() {
        conectarCircuitoCompuertaANDCompleto();

        // 0 · 0 = 0
        switchDivisor.setCerradoA(false);
        switchDivisor.setCerradoB(false);
        nivel.actualizar(0.1f);
        assertFalse(compuertaAnd.isSalida());
        assertEquals("NEUTRO", led.getEstadoActual());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        // 1 · 0 = 0
        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(false);
        nivel.actualizar(0.1f);
        assertFalse(compuertaAnd.isSalida());
        assertEquals("NEUTRO", led.getEstadoActual());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        // 0 · 1 = 0
        switchDivisor.setCerradoA(false);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(0.1f);
        assertFalse(compuertaAnd.isSalida());
        assertEquals("NEUTRO", led.getEstadoActual());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        // 1 · 1 = 1
        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(1.0f);
        assertTrue(compuertaAnd.isSalida());
        assertEquals("EXITO", compuertaAnd.getEstadoActual());
        assertEquals("EXITO", switchDivisor.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testCondicionVictoriaCincoSegundosContinuos() {
        conectarCircuitoCompuertaANDCompleto();
        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);

        nivel.actualizar(3.0f);
        assertEquals(3.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(1.9f);
        assertEquals(4.9f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(0.2f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.estaCompletado());
        assertTrue(nivel.getMensajeEstado().contains("¡Nivel superado!"));
    }

    @Test
    public void testInterrupcionReseteaTemporizador() {
        conectarCircuitoCompuertaANDCompleto();
        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);

        nivel.actualizar(4.0f);
        assertEquals(4.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        switchDivisor.setCerradoB(false);
        nivel.actualizar(0.1f);

        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        switchDivisor.setCerradoB(true);
        nivel.actualizar(4.9f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(0.1f);
        assertTrue(nivel.estaGanado());
    }

    @Test
    public void testReiniciarNivelRestaura50SegundosYLimpiaCables() {
        conectarCircuitoCompuertaANDCompleto();
        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(5.0f);
        assertTrue(nivel.estaGanado());

        nivel.reiniciarNivelActual();

        assertFalse(nivel.estaGanado());
        assertFalse(nivel.estaCompletado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertEquals(0, nivel.getCables().size());
        assertEquals(Constantes.TIEMPO_LIMITE_NIVEL_3, nivel.getTiempoRestante(), 0.001f);
    }
}
