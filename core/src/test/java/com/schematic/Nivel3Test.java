package com.schematic;

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
    private Switch switch1;
    private Switch switch2;
    private Resistencia resistencia;
    private LED led;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel3) GestorNiveles.crearNivel(3);
        assertNotNull(nivel, "GestorNiveles debe instanciar Nivel3 para el nivel 3.");

        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
            else if (c instanceof Switch) {
                if ("SW_1".equals(c.getIdentificador())) switch1 = (Switch) c;
                else if ("SW_2".equals(c.getIdentificador())) switch2 = (Switch) c;
            }
            else if (c instanceof Resistencia) resistencia = (Resistencia) c;
            else if (c instanceof LED) led = (LED) c;
        }
    }

    private void conectarCircuitoSerieCompleto() {
        // Conectar: Fuente(+) -> Switch 1 -> Switch 2 -> Resistencia -> LED(Ánodo->Cátodo) -> Fuente(-)
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), switch2.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testInicializacionComponentesEInventario() {
        assertEquals(3, nivel.getNumeroNivel());
        assertEquals("NIVEL 3: DOBLE INTERRUPTOR (LÓGICA AND)", nivel.getTituloReto());
        assertEquals(5, nivel.getComponentes().size(), "Debe haber 5 componentes colocados en el tablero.");
        assertNotNull(fuente);
        assertNotNull(switch1);
        assertNotNull(switch2);
        assertNotNull(resistencia);
        assertNotNull(led);

        // Inventario disponible debe ser 0 porque están en el tablero
        Nivel.CasillaInventario casSw = nivel.buscarCasilla("SWITCH");
        assertNotNull(casSw);
        assertEquals(2, casSw.getCantidadTotal());
        assertEquals(0, casSw.getCantidadDisponible());

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
        // Conectar sin resistencia: Fuente(+) -> Switch1 -> Switch2 -> LED -> Fuente(-)
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), switch2.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switch1.setCerrado(true);
        switch2.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(led.estaQuemado());
        assertEquals("ERROR", led.getEstadoActual());
        assertEquals("ERROR", fuente.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("EL LED SE HA QUEMADO"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testPolaridadInvertidaLED() {
        // Conectar Fuente(+) -> Switch1 -> Switch2 -> Resistencia -> LED(Cátodo)
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), switch2.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalCatodo()));

        switch1.setCerrado(true);
        switch2.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("Polaridad invertida"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testFaltaSegundoInterruptor() {
        // Conectar saltando el switch 2: Fuente(+) -> Switch1 -> Resistencia -> LED -> Fuente(-)
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switch1.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("Falta intercalar el segundo interruptor"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testLogicaANDAmbosInterruptoresDebenEstarCerrados() {
        conectarCircuitoSerieCompleto();

        // Caso 1: Ambos interruptores abiertos
        switch1.setCerrado(false);
        switch2.setCerrado(false);
        nivel.actualizar(0.1f);
        assertFalse(nivel.estaGanado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.getMensajeEstado().contains("Ambos interruptores están abiertos"));

        // Caso 2: Switch 1 cerrado, Switch 2 abierto
        switch1.setCerrado(true);
        switch2.setCerrado(false);
        nivel.actualizar(0.1f);
        assertFalse(nivel.estaGanado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.getMensajeEstado().contains("Interruptor B está abierto"));

        // Caso 3: Switch 1 abierto, Switch 2 cerrado
        switch1.setCerrado(false);
        switch2.setCerrado(true);
        nivel.actualizar(0.1f);
        assertFalse(nivel.estaGanado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.getMensajeEstado().contains("Interruptor A está abierto"));

        // Caso 4: Ambos interruptores cerrados -> Fluye corriente
        switch1.setCerrado(true);
        switch2.setCerrado(true);
        nivel.actualizar(1.0f);
        assertEquals("EXITO", switch1.getEstadoActual());
        assertEquals("EXITO", switch2.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado(), "Aún no han pasado los 5 segundos requeridos.");
    }

    @Test
    public void testCondicionVictoriaCincoSegundosContinuos() {
        conectarCircuitoSerieCompleto();
        switch1.setCerrado(true);
        switch2.setCerrado(true);

        // 3 segundos
        nivel.actualizar(3.0f);
        assertEquals(3.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // 1.9 segundos más (total 4.9s)
        nivel.actualizar(1.9f);
        assertEquals(4.9f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // 0.2 segundos más (total 5.1s >= 5.0s)
        nivel.actualizar(0.2f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado(), "Debe ganar tras 5.0 segundos continuos.");
        assertTrue(nivel.estaCompletado());
        assertTrue(nivel.getMensajeEstado().contains("¡Nivel superado!"));
    }

    @Test
    public void testInterrupcionReseteaTemporizador() {
        conectarCircuitoSerieCompleto();
        switch1.setCerrado(true);
        switch2.setCerrado(true);

        nivel.actualizar(4.0f);
        assertEquals(4.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        // Abrir un switch interrumpe el circuito
        switch1.setCerrado(false);
        nivel.actualizar(0.1f);

        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f, "Temporizador debe volver a 0 inmediatamente.");
        assertFalse(nivel.estaGanado());

        // Volver a cerrar requiere 5 segundos completos desde 0
        switch1.setCerrado(true);
        nivel.actualizar(4.9f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(0.1f);
        assertTrue(nivel.estaGanado());
    }

    @Test
    public void testReiniciarNivelLimpiaTemporizadoresYCables() {
        conectarCircuitoSerieCompleto();
        switch1.setCerrado(true);
        switch2.setCerrado(true);
        nivel.actualizar(5.0f);
        assertTrue(nivel.estaGanado());

        nivel.reiniciarNivelActual();

        assertFalse(nivel.estaGanado());
        assertFalse(nivel.estaCompletado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertEquals(0, nivel.getCables().size());
        assertEquals(30.0f, nivel.getTiempoRestante(), 0.001f);
    }
}

