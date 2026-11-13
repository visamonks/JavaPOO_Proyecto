package com.schematic.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BarraAtajos {

    private static final float ESPACIO_TECLA = 10f;
    private static final float ESPACIO_GRUPO = 38f;

    private final GlyphLayout layout = new GlyphLayout();
    private final Color colorTecla;
    private final Color colorTexto;

    public BarraAtajos(Color colorTecla, Color colorTexto) {
        this.colorTecla = new Color(colorTecla);
        this.colorTexto = new Color(colorTexto);
    }

    public float medirAncho(BitmapFont font, String... pares) {
        float ancho = 0f;
        for (int i = 0; i < pares.length; i += 2) {
            layout.setText(font, pares[i]);
            ancho += layout.width + ESPACIO_TECLA;
            layout.setText(font, pares[i + 1]);
            ancho += layout.width;
            if (i + 2 < pares.length) ancho += ESPACIO_GRUPO;
        }
        return ancho;
    }

    public void dibujarCentrado(SpriteBatch batch, BitmapFont font, float centroX, float y, String... pares) {
        dibujar(batch, font, centroX - medirAncho(font, pares) / 2f, y, pares);
    }

    public void dibujar(SpriteBatch batch, BitmapFont font, float x, float y, String... pares) {
        float cursor = x;
        for (int i = 0; i < pares.length; i += 2) {
            font.setColor(colorTecla);
            layout.setText(font, pares[i]);
            font.draw(batch, layout, cursor, y);
            cursor += layout.width + ESPACIO_TECLA;

            font.setColor(colorTexto);
            layout.setText(font, pares[i + 1]);
            font.draw(batch, layout, cursor, y);
            cursor += layout.width + ESPACIO_GRUPO;
        }
    }
}
