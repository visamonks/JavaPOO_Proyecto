package com.schematic;

import com.schematic.constants.Constantes;
import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel;
import com.schematic.model.niveles.Nivel4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel4Test {

    private Nivel4 nivel;
    private FuenteAlimentacion fuente;
    private Switch switchDivisor;
    private CompuertaOR compuertaOr;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel4) GestorNiveles.crearNivel(4);
        assertNotNull(nivel, "GestorNiveles debe instanciar Nivel4 para el nivel 4.");

        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) {
                fuente = (FuenteAlimentacion) c;
            } else if (c instanceof Switch) {
                switchDivisor = (Switch) c;
            } else if (c instanceof CompuertaOR) {
                compuertaOr = (CompuertaOR) c;
            } else if (c instanceof Resistencia) {
                resistencia = (Resistencia) c;
            } else if (c instanceof LED) {
                led = (LED) c;
            }
        }
    }

    private void conectarCircuitoCompuertaORCompleto() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaOr.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaOr.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaOr.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testInicializacionComponentesTiempoEInventario() {
        assertEquals(4, nivel.getNumeroNivel());
        assertEquals("NIVEL 4: COMPUERTA LÓGICA OR", nivel.getTituloReto());
        assertEquals(50.0f, nivel.getTiempoLimite(), 0.001f);
        assertEquals(50.0f, nivel.getTiempoRestante(), 0.001f);
        assertEquals(5, nivel.getComponentes().size(), "Debe haber 5 componentes colocados en el tablero.");

        assertNotNull(fuente);
        assertNotNull(switchDivisor);
        assertTrue(switchDivisor.isDoble(), "El Switch del nivel 4 debe ser un Switch Divisor doble (A/B).");
        assertNotNull(compuertaOr);
        assertNotNull(resistencia);
        assertNotNull(led);

        Nivel.CasillaInventario casSw = nivel.buscarCasilla("SWITCH");
        assertNotNull(casSw);
        assertEquals(1, casSw.getCantidadTotal());
        assertEquals(0, casSw.getCantidadDisponible());

        Nivel.CasillaInventario casOr = nivel.buscarCasilla("OR");
        assertNotNull(casOr);
        assertEquals(1, casOr.getCantidadTotal());
        assertEquals(0, casOr.getCantidadDisponible());

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
    public void testCircuitoSinResistenciaQuemaLED() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaOr.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaOr.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaOr.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switchDivisor.setCerradoA(true);
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
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaOr.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaOr.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaOr.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalCatodo()));
        nivel.agregarCable(new Cable(led.getTerminalAnodo(), fuente.getTerminalNegativo()));

        switchDivisor.setCerradoA(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("Polaridad invertida"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testFaltaSegundaEntradaEnCompuertaOR() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaOr.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(compuertaOr.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switchDivisor.setCerradoA(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("IN B"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testLogicaORAmbasPalancasAbiertas() {
        conectarCircuitoCompuertaORCompleto();

        switchDivisor.setCerradoA(false);
        switchDivisor.setCerradoB(false);
        nivel.actualizar(0.1f);

        assertFalse(compuertaOr.isSalida());
        assertFalse(nivel.estaGanado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertEquals("NEUTRO", compuertaOr.getEstadoActual());
        assertEquals("NEUTRO", led.getEstadoActual());
    }

    @Test
    public void testLogicaORPalancaACerradaEnciendeLED() {
        conectarCircuitoCompuertaORCompleto();

        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(false);
        nivel.actualizar(1.0f);

        assertTrue(compuertaOr.isSalida());
        assertEquals("EXITO", switchDivisor.getEstadoActual());
        assertEquals("EXITO", compuertaOr.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", fuente.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testLogicaORPalancaBCerradaEnciendeLED() {
        conectarCircuitoCompuertaORCompleto();

        switchDivisor.setCerradoA(false);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(1.0f);

        assertTrue(compuertaOr.isSalida());
        assertEquals("EXITO", switchDivisor.getEstadoActual());
        assertEquals("EXITO", compuertaOr.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", fuente.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testLogicaORAmbasPalancasCerradasEnciendeLED() {
        conectarCircuitoCompuertaORCompleto();

        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(true);
        nivel.actualizar(1.0f);

        assertTrue(compuertaOr.isSalida());
        assertEquals("EXITO", compuertaOr.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
    }

    @Test
    public void testCondicionVictoriaCincoSegundosContinuos() {
        conectarCircuitoCompuertaORCompleto();
        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(false);

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
    public void testTransferenciaFluidaDeRamaSinRomperCircuito() {
        conectarCircuitoCompuertaORCompleto();

        switchDivisor.setCerradoA(true);
        switchDivisor.setCerradoB(false);
        nivel.actualizar(2.0f);
        assertEquals(2.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        switchDivisor.setCerradoB(true);
        nivel.actualizar(1.5f);
        assertEquals(3.5f, nivel.getTiempoCircuitoActivo(), 0.001f);

        switchDivisor.setCerradoA(false);
        nivel.actualizar(1.5f);

        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.estaCompletado());
    }

    @Test
    public void testDesconexionDeCableInterrumpeTemporizador() {
        conectarCircuitoCompuertaORCompleto();
        switchDivisor.setCerradoA(true);

        nivel.actualizar(4.0f);
        assertEquals(4.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        Cable cableTierra = nivel.buscarCableConTerminal(fuente.getTerminalNegativo());
        assertNotNull(cableTierra);
        nivel.eliminarCable(cableTierra);

        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testResistenciaEnLadoNegativoFuncionaCorrectamente() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switchDivisor.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaA(), compuertaOr.getTerminalEntradaA()));
        nivel.agregarCable(new Cable(switchDivisor.getTerminalSalidaB(), compuertaOr.getTerminalEntradaB()));
        nivel.agregarCable(new Cable(compuertaOr.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), fuente.getTerminalNegativo()));

        switchDivisor.setCerradoB(true);
        nivel.actualizar(1.0f);

        assertFalse(led.estaQuemado());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
    }

    @Test
    public void testDevolverYColocarCompuertaORDesdeInventario() {
        nivel.devolverAlInventario(compuertaOr);
        Nivel.CasillaInventario casOr = nivel.buscarCasilla("OR");
        assertEquals(1, casOr.getCantidadDisponible());

        Componente nueva = nivel.crearYColocarComponente("OR", 680f, 430f);
        assertNotNull(nueva);
        assertTrue(nueva instanceof CompuertaOR);
        assertEquals(0, casOr.getCantidadDisponible());
    }

    @Test
    public void testAgotamientoDeTiempo50SegundosFallaNivel() {
        conectarCircuitoCompuertaORCompleto();
        switchDivisor.setCerradoA(false);
        switchDivisor.setCerradoB(false);

        nivel.actualizar(51.0f);

        assertEquals(0.0f, nivel.getTiempoRestante(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testReiniciarNivelRestaura50SegundosYLimpiaCables() {
        conectarCircuitoCompuertaORCompleto();
        switchDivisor.setCerradoA(true);
        nivel.actualizar(5.0f);
        assertTrue(nivel.estaGanado());

        nivel.reiniciarNivelActual();

        assertFalse(nivel.estaGanado());
        assertFalse(nivel.estaCompletado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertEquals(0, nivel.getCables().size());
        assertEquals(Constantes.TIEMPO_LIMITE_NIVEL_4, nivel.getTiempoRestante(), 0.001f);
    }
}
