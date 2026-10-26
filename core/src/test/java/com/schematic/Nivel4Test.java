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
    private Switch switch1;
    private Switch switch2;
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
                if (Constantes.ID_NIVEL4_SWITCH_A.equals(c.getIdentificador())) {
                    switch1 = (Switch) c;
                } else if (Constantes.ID_NIVEL4_SWITCH_B.equals(c.getIdentificador())) {
                    switch2 = (Switch) c;
                }
            } else if (c instanceof Resistencia) {
                resistencia = (Resistencia) c;
            } else if (c instanceof LED) {
                led = (LED) c;
            }
        }
    }

    /**
     * Conecta el circuito en paralelo completo (Lógica OR):
     * Rama A: Fuente(+) -> Switch 1 -> Resistencia -> LED -> Fuente(-)
     * Rama B: Fuente(+) -> Switch 2 -> Resistencia -> LED -> Fuente(-)
     */
    private void conectarCircuitoParaleloCompleto() {
        // Entradas en paralelo desde Fuente(+)
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch2.getTerminalEntrada()));

        // Salidas en paralelo convergen en la Resistencia
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), resistencia.getTerminalEntrada()));

        // Resistencia limitadora hacia el Ánodo del LED
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));

        // Cátodo del LED retorna a tierra polo negativo (-)
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));
    }

    @Test
    public void testInicializacionComponentesEInventario() {
        assertEquals(4, nivel.getNumeroNivel());
        assertEquals("NIVEL 4: DOBLE SWITCH EN PARALELO (LÓGICA OR)", nivel.getTituloReto());
        assertEquals(5, nivel.getComponentes().size(), "Debe haber 5 componentes colocados en el tablero.");
        assertNotNull(fuente);
        assertNotNull(switch1);
        assertNotNull(switch2);
        assertNotNull(resistencia);
        assertNotNull(led);

        // Inventario disponible debe ser 0 porque ya están colocados en el tablero
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
        // Conectar ramas paralelas directamente al LED sin pasar por resistencia
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch2.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switch1.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(led.estaQuemado());
        assertEquals("ERROR", led.getEstadoActual());
        assertEquals("ERROR", fuente.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("EL LED SE HA QUEMADO"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testPolaridadInvertidaLED() {
        // Conectar la salida de las ramas y resistencia hacia el Cátodo (-) del LED
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch2.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalCatodo()));
        nivel.agregarCable(new Cable(led.getTerminalAnodo(), fuente.getTerminalNegativo()));

        switch1.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("Polaridad invertida"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testFaltaSegundoInterruptorEnParalelo() {
        // Conectar únicamente la rama del Switch 1, dejando Switch 2 sin conectar
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switch1.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("segundo interruptor") || nivel.getMensajeEstado().contains("Lógica OR"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testInterruptoresConectadosEnSerieDaError() {
        // Conectar interruptores en serie (Lógica AND) en lugar de en paralelo
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), switch1.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch1.getTerminalSalida(), switch2.getTerminalEntrada()));
        nivel.agregarCable(new Cable(switch2.getTerminalSalida(), resistencia.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resistencia.getTerminalSalida(), led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), fuente.getTerminalNegativo()));

        switch1.setCerrado(true);
        switch2.setCerrado(true);
        nivel.actualizar(0.1f);

        assertTrue(nivel.getMensajeEstado().contains("serie") && nivel.getMensajeEstado().contains("Lógica OR"));
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testLogicaORAmbosInterruptoresAbiertos() {
        conectarCircuitoParaleloCompleto();

        switch1.setCerrado(false);
        switch2.setCerrado(false);
        nivel.actualizar(0.1f);

        assertFalse(nivel.estaGanado());
        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.getMensajeEstado().contains("Ambos interruptores están abiertos"));
        assertEquals("NEUTRO", switch1.getEstadoActual());
        assertEquals("NEUTRO", switch2.getEstadoActual());
        assertEquals("NEUTRO", led.getEstadoActual());
    }

    @Test
    public void testLogicaORSwitchACerradoEnciendeLED() {
        conectarCircuitoParaleloCompleto();

        // Switch A (SW_1) CERRADO, Switch B (SW_2) ABIERTO -> Lógica OR satisfecha (1 || 0 = 1)
        switch1.setCerrado(true);
        switch2.setCerrado(false);
        nivel.actualizar(1.0f);

        assertEquals("EXITO", switch1.getEstadoActual());
        assertEquals("NEUTRO", switch2.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", fuente.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado(), "Aún no han transcurrido los 5 segundos continuos de validación.");
    }

    @Test
    public void testLogicaORSwitchBCerradoEnciendeLED() {
        conectarCircuitoParaleloCompleto();

        // Switch A (SW_1) ABIERTO, Switch B (SW_2) CERRADO -> Lógica OR satisfecha (0 || 1 = 1)
        switch1.setCerrado(false);
        switch2.setCerrado(true);
        nivel.actualizar(1.0f);

        assertEquals("NEUTRO", switch1.getEstadoActual());
        assertEquals("EXITO", switch2.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", fuente.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testLogicaORAmbosInterruptoresCerrados() {
        conectarCircuitoParaleloCompleto();

        // Ambos CERRADOS -> Lógica OR satisfecha (1 || 1 = 1)
        switch1.setCerrado(true);
        switch2.setCerrado(true);
        nivel.actualizar(1.0f);

        assertEquals("EXITO", switch1.getEstadoActual());
        assertEquals("EXITO", switch2.getEstadoActual());
        assertEquals("EXITO", resistencia.getEstadoActual());
        assertEquals("EXITO", led.getEstadoActual());
        assertEquals("EXITO", fuente.getEstadoActual());
        assertEquals(1.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());
    }

    @Test
    public void testCondicionVictoriaCincoSegundosContinuos() {
        conectarCircuitoParaleloCompleto();
        switch1.setCerrado(true);
        switch2.setCerrado(false);

        // 3 segundos iniciales
        nivel.actualizar(3.0f);
        assertEquals(3.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // 1.9 segundos más (total 4.9s < 5.0s)
        nivel.actualizar(1.9f);
        assertEquals(4.9f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertFalse(nivel.estaGanado());

        // 0.2 segundos más (total 5.1s >= 5.0s)
        nivel.actualizar(0.2f);
        assertEquals(5.0f, nivel.getTiempoCircuitoActivo(), 0.001f);
        assertTrue(nivel.estaGanado(), "El nivel debe completarse tras 5.0 segundos continuos.");
        assertTrue(nivel.estaCompletado());
        assertTrue(nivel.getMensajeEstado().contains("¡Nivel superado!"));
    }

    @Test
    public void testInterrupcionReseteaTemporizador() {
        conectarCircuitoParaleloCompleto();
        switch1.setCerrado(true);
        switch2.setCerrado(false);

        nivel.actualizar(4.0f);
        assertEquals(4.0f, nivel.getTiempoCircuitoActivo(), 0.001f);

        // Abrir el switch activo interrumpe la corriente
        switch1.setCerrado(false);
        nivel.actualizar(0.1f);

        assertEquals(0.0f, nivel.getTiempoCircuitoActivo(), 0.001f, "Temporizador debe resetearse a 0.0s de inmediato.");
        assertFalse(nivel.estaGanado());

        // Cerrar Switch B reactiva el flujo, pero debe contar los 5s completos desde 0
        switch2.setCerrado(true);
        nivel.actualizar(4.9f);
        assertFalse(nivel.estaGanado());

        nivel.actualizar(0.1f);
        assertTrue(nivel.estaGanado());
    }

    @Test
    public void testReiniciarNivelLimpiaTemporizadoresYCables() {
        conectarCircuitoParaleloCompleto();
        switch1.setCerrado(true);
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

