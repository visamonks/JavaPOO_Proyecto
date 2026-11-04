package com.schematic;

import com.schematic.model.componentes.CompuertaAND;
import com.schematic.model.componentes.CompuertaLogica;
import com.schematic.model.componentes.CompuertaOR;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel;
import com.schematic.model.niveles.TutorialNivel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TutorialTest {

    @Test
    public void testTodosLosNivelesTienenTutorialCompleto() {
        for (int n = 1; n <= GestorNiveles.obtenerTotalNiveles(); n++) {
            TutorialNivel t = GestorNiveles.crearNivel(n).obtenerTutorial();
            assertNotNull(t, "El nivel " + n + " debe tener tutorial.");
            assertFalse(t.getTitulo().isEmpty(), "Falta el título del tutorial del nivel " + n);
            assertFalse(t.getConcepto().isEmpty(), "Falta el concepto del tutorial del nivel " + n);
            assertFalse(t.getVidaReal().isEmpty(), "Falta el ejemplo de vida real del nivel " + n);
            assertFalse(t.getMision().isEmpty(), "Falta la misión del nivel " + n);
            assertTrue(t.getPasos().length > 0, "El tutorial del nivel " + n + " debe tener pasos.");
            assertNotNull(t.getEsquema(), "El tutorial del nivel " + n + " debe tener esquema.");
        }
    }

    @Test
    public void testTextosSoloUsanCaracteresLatin1() {
        for (int n = 1; n <= GestorNiveles.obtenerTotalNiveles(); n++) {
            TutorialNivel t = GestorNiveles.crearNivel(n).obtenerTutorial();
            StringBuilder todo = new StringBuilder();
            todo.append(t.getTitulo()).append(t.getConcepto()).append(t.getVidaReal())
                .append(t.getMision()).append(t.getConsejo());
            for (String paso : t.getPasos()) todo.append(paso);
            for (char c : todo.toString().toCharArray()) {
                assertTrue(c <= 0xFF, "Nivel " + n + ": el caracter '" + c + "' no se puede dibujar.");
            }
        }
    }

    @Test
    public void testTablaDeVerdadCoincideConLaCompuertaReal() {
        revisarTabla(GestorNiveles.crearNivel(3), true);
        revisarTabla(GestorNiveles.crearNivel(4), false);
    }

    private void revisarTabla(Nivel nivel, boolean esAnd) {
        boolean[] tabla = nivel.obtenerTutorial().getTablaVerdad();
        assertNotNull(tabla, "El nivel " + nivel.getNumeroNivel() + " debe mostrar la tabla de verdad.");
        boolean[][] entradas = {{false, false}, {true, false}, {false, true}, {true, true}};
        for (int i = 0; i < 4; i++) {
            boolean a = entradas[i][0], b = entradas[i][1];
            CompuertaLogica compuerta = esAnd ? new CompuertaAND("T", 0, 0, a, b) : new CompuertaOR("T", 0, 0, a, b);
            assertEquals(compuerta.calcularSalida(), tabla[i], "Fila A=" + a + " B=" + b + " del nivel " + nivel.getNumeroNivel());
        }
    }
}
