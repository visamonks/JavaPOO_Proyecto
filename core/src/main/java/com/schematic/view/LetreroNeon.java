package com.schematic.view;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.Map;

public class LetreroNeon {

    private static final float ANCHO_LETRA = 4f;
    private static final float ESPACIO_LETRAS = 2.3f;
    private static final float ANCHO_ESPACIO = 3.4f;

    private final Map<Character, float[][]> letras = new HashMap<>();

    public LetreroNeon() {
        letras.put('S', new float[][]{{4, 6, 1, 6, 0, 5, 0, 4, 1, 3, 3, 3, 4, 2, 4, 1, 3, 0, 0, 0}});
        letras.put('H', new float[][]{{0, 0, 0, 6}, {4, 0, 4, 6}, {0, 3, 4, 3}});
        letras.put('O', new float[][]{{1, 0, 3, 0, 4, 1, 4, 5, 3, 6, 1, 6, 0, 5, 0, 1, 1, 0}});
        letras.put('R', new float[][]{{0, 0, 0, 6, 3, 6, 4, 5, 4, 4, 3, 3, 0, 3}, {2, 3, 4, 0}});
        letras.put('T', new float[][]{{0, 6, 4, 6}, {2, 6, 2, 0}});
        letras.put('C', new float[][]{{4, 6, 1, 6, 0, 5, 0, 1, 1, 0, 4, 0}});
        letras.put('I', new float[][]{{2, 0, 2, 6}, {1, 6, 3, 6}, {1, 0, 3, 0}});
        letras.put('U', new float[][]{{0, 6, 0, 1, 1, 0, 3, 0, 4, 1, 4, 6}});
        letras.put('!', new float[][]{{2, 6, 2, 2}, {2, 0, 2, 0}});
    }

    public float medirAncho(String texto, float unidad) {
        float ancho = 0f;
        for (int i = 0; i < texto.length(); i++) {
            ancho += avanceDe(texto.charAt(i));
        }
        return (ancho - ESPACIO_LETRAS) * unidad;
    }

    public float medirAlto(float unidad) {
        return 6f * unidad;
    }

    public void dibujar(ShapeRenderer sr, String texto, float x, float y, float unidad, float grosor, boolean conPuntas) {
        float cursor = x;
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            float[][] trazos = letras.get(letra);
            if (trazos != null) {
                float corrimiento = (letra == '!') ? -1.5f * unidad : 0f;
                for (float[] trazo : trazos) {
                    dibujarTrazo(sr, trazo, cursor + corrimiento, y, unidad, grosor, conPuntas);
                }
            }
            cursor += avanceDe(letra) * unidad;
        }
    }

    private float avanceDe(char letra) {
        if (letra == ' ') return ANCHO_ESPACIO;
        if (letra == '!') return 1f + ESPACIO_LETRAS;
        return ANCHO_LETRA + ESPACIO_LETRAS;
    }

    private void dibujarTrazo(ShapeRenderer sr, float[] p, float x, float y, float unidad, float grosor, boolean conPuntas) {
        int puntos = p.length / 2;
        for (int k = 0; k < puntos; k++) {
            float px = x + p[k * 2] * unidad;
            float py = y + p[k * 2 + 1] * unidad;
            sr.circle(px, py, grosor / 2f, 16);
            if (k > 0) {
                float ax = x + p[(k - 1) * 2] * unidad;
                float ay = y + p[(k - 1) * 2 + 1] * unidad;
                sr.rectLine(ax, ay, px, py, grosor);
            }
        }
        if (conPuntas && puntos > 1) {
            boolean cerrado = p[0] == p[p.length - 2] && p[1] == p[p.length - 1];
            if (!cerrado) {
                sr.circle(x + p[0] * unidad, y + p[1] * unidad, grosor * 0.95f, 20);
                sr.circle(x + p[p.length - 2] * unidad, y + p[p.length - 1] * unidad, grosor * 0.95f, 20);
            }
        }
    }
}
