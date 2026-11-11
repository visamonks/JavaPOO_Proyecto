package com.schematic;

import com.schematic.model.componentes.*;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel;
import com.schematic.model.niveles.Nivel5;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Nivel5Test {

    private Nivel5 nivel;
    private FuenteAlimentacion fuente;
    private Switch palanca;
    private Resistencia resIzquierda;
    private Resistencia resDerecha;
    private LED ledIzquierda;
    private LED ledDerecha;

    @BeforeEach
    public void setUp() {
        nivel = (Nivel5) GestorNiveles.crearNivel(5);
        palanca = (Switch) nivel.crearYColocarComponente("SWITCH_DIVISOR", 420f, 450f);
        resIzquierda = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 780f, 700f);
        resDerecha = (Resistencia) nivel.crearYColocarComponente("RESISTENCIA_220", 780f, 250f);
        ledIzquierda = (LED) nivel.crearYColocarComponente("LED", 1200f, 650f);
        ledDerecha = (LED) nivel.crearYColocarComponente("LED", 1200f, 200f);
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof FuenteAlimentacion) fuente = (FuenteAlimentacion) c;
        }
    }

    private void conectarRama(Terminal salida, Resistencia res, LED led) {
        nivel.agregarCable(new Cable(salida, led.getTerminalAnodo()));
        nivel.agregarCable(new Cable(led.getTerminalCatodo(), res.getTerminalEntrada()));
        nivel.agregarCable(new Cable(res.getTerminalSalida(), fuente.getTerminalNegativo()));
    }

    private void conectarTodo() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), palanca.getTerminalEntrada()));
        conectarRama(palanca.getTerminalSalidaA(), resIzquierda, ledIzquierda);
        conectarRama(palanca.getTerminalSalidaB(), resDerecha, ledDerecha);
    }

    @Test
    public void testNivelEmpiezaSoloConLaFuente() {
        Nivel nuevo = GestorNiveles.crearNivel(5);
        assertEquals(1, nuevo.getComponentes().size());
        assertEquals(2, nuevo.buscarCasilla("LED").getCantidadDisponible());
        assertEquals(2, nuevo.buscarCasilla("RESISTENCIA_220").getCantidadDisponible());
        assertNotNull(nuevo.buscarCasilla("SWITCH_DIVISOR"));
    }

    @Test
    public void testLasPiezasRepetidasTienenNombresDistintos() {
        assertNotEquals(ledIzquierda.getIdentificador(), ledDerecha.getIdentificador());
        assertNotEquals(resIzquierda.getIdentificador(), resDerecha.getIdentificador());
        assertTrue(palanca.isDoble());
    }

    @Test
    public void testCadaLadoPrendeSoloSuLed() {
        conectarTodo();
        palanca.setCerradoA(true);
        nivel.actualizar(0.5f);
        assertEquals("EXITO", ledIzquierda.getEstadoActual());
        assertEquals("NEUTRO", ledDerecha.getEstadoActual());

        palanca.setCerradoA(false);
        palanca.setCerradoB(true);
        nivel.actualizar(0.5f);
        assertEquals("NEUTRO", ledIzquierda.getEstadoActual());
        assertEquals("EXITO", ledDerecha.getEstadoActual());
    }

    @Test
    public void testSoloUnLadoNoAlcanzaParaGanar() {
        conectarTodo();
        palanca.setCerradoA(true);
        nivel.actualizar(10f);
        assertFalse(nivel.estaGanado());
        assertEquals(Nivel5.TIEMPO_POR_LADO, nivel.getTiempoIzquierda(), 0.001f);
    }

    @Test
    public void testGanaUsandoLasDosDireccionales() {
        conectarTodo();
        palanca.setCerradoA(true);
        nivel.actualizar(2.5f);
        palanca.setCerradoA(false);
        palanca.setCerradoB(true);
        nivel.actualizar(2.4f);
        assertFalse(nivel.estaGanado());
        nivel.actualizar(0.2f);
        assertTrue(nivel.estaGanado());
        assertTrue(nivel.getMensajeEstado().contains("superado"));
    }

    @Test
    public void testRamaSinResistenciaQuemaElLed() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), palanca.getTerminalEntrada()));
        nivel.agregarCable(new Cable(palanca.getTerminalSalidaA(), ledIzquierda.getTerminalAnodo()));
        nivel.agregarCable(new Cable(ledIzquierda.getTerminalCatodo(), fuente.getTerminalNegativo()));
        palanca.setCerradoA(true);
        nivel.actualizar(0.1f);
        assertEquals("ERROR", ledIzquierda.getEstadoActual());
        assertTrue(nivel.getMensajeEstado().contains("QUEMADO"));
    }

    @Test
    public void testFaltaLaRamaDerecha() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), palanca.getTerminalEntrada()));
        conectarRama(palanca.getTerminalSalidaA(), resIzquierda, ledIzquierda);
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().startsWith("Derecha (B)"));
    }

    @Test
    public void testPolaridadInvertidaEnUnaRama() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), palanca.getTerminalEntrada()));
        nivel.agregarCable(new Cable(palanca.getTerminalSalidaA(), resIzquierda.getTerminalEntrada()));
        nivel.agregarCable(new Cable(resIzquierda.getTerminalSalida(), ledIzquierda.getTerminalCatodo()));
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().contains("Polaridad invertida"));
    }

    @Test
    public void testCortocircuitoDirecto() {
        nivel.agregarCable(new Cable(fuente.getTerminalPositivo(), fuente.getTerminalNegativo()));
        nivel.actualizar(0.1f);
        assertTrue(nivel.getMensajeEstado().contains("CORTOCIRCUITO"));
    }
}
