package com.schematic.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.schematic.constants.Constantes;
import com.schematic.controller.ControladorNivel;
import com.schematic.model.*;
import java.util.HashMap;
import java.util.Map;

public class VistaNivel extends ScreenAdapter {

    private Minijuego minijuego;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camara;
    private Map<String, Texture> mapaTexturas;
    private ControladorNivel controlador;

    public static final float ANCHO_PANTALLA = Constantes.ANCHO_VENTANA;
    public static final float ALTO_PANTALLA = Constantes.ALTO_VENTANA;
    public static final float TAMANO_CELDA = ControladorNivel.TAMANO_CELDA;
    public static final float ALTO_BARRA_SUPERIOR = 80f;

    public VistaNivel(Minijuego minijuego) {
        this.minijuego = minijuego;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.4f);
        this.mapaTexturas = new HashMap<>();

        this.camara = new OrthographicCamera();
        this.camara.setToOrtho(false, ANCHO_PANTALLA, ALTO_PANTALLA);

        this.controlador = new ControladorNivel(minijuego, camara);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(controlador);
    }

    @Override
    public void render(float delta) {
        if (minijuego != null) {
            minijuego.actualizar(delta);
        }

        Gdx.gl.glClearColor(0.10f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camara.update();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarGrid();
        dibujarComponentes();
        dibujarBarraSuperior();
        shapeRenderer.end();

        batch.begin();
        dibujarTextos();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void dibujarGrid() {
        shapeRenderer.setColor(0.18f, 0.22f, 0.28f, 0.6f);
        for (float x = 0; x <= ANCHO_PANTALLA; x += TAMANO_CELDA) {
            shapeRenderer.rectLine(x, 0, x, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR, 1.2f);
        }
        for (float y = 0; y <= ALTO_PANTALLA - ALTO_BARRA_SUPERIOR; y += TAMANO_CELDA) {
            shapeRenderer.rectLine(0, y, ANCHO_PANTALLA, y, 1.2f);
        }
    }

    private void dibujarComponentes() {
        if (minijuego == null) return;

        for (Componente comp : minijuego.getComponentes()) {
            float x = comp.getPosicionX();
            float y = comp.getPosicionY();
            float w = ControladorNivel.ANCHO_COMPONENTE;
            float h = ControladorNivel.ALTO_COMPONENTE;

            if ("EXITO".equals(comp.getEstadoActual())) {
                shapeRenderer.setColor(0.18f, 0.48f, 0.28f, 1f);
            } else if ("ERROR".equals(comp.getEstadoActual())) {
                shapeRenderer.setColor(0.65f, 0.20f, 0.20f, 1f);
            } else {
                shapeRenderer.setColor(0.24f, 0.28f, 0.36f, 1f);
            }
            shapeRenderer.rect(x, y, w, h);

            shapeRenderer.setColor(0.45f, 0.55f, 0.70f, 1f);
            shapeRenderer.rectLine(x, y, x + w, y, 2.5f);
            shapeRenderer.rectLine(x + w, y, x + w, y + h, 2.5f);
            shapeRenderer.rectLine(x + w, y + h, x, y + h, 2.5f);
            shapeRenderer.rectLine(x, y + h, x, y, 2.5f);
        }
    }

    private void dibujarBarraSuperior() {
        shapeRenderer.setColor(0.14f, 0.16f, 0.22f, 1f);
        shapeRenderer.rect(0, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR, ANCHO_PANTALLA, ALTO_BARRA_SUPERIOR);

        shapeRenderer.setColor(0.32f, 0.38f, 0.48f, 1f);
        shapeRenderer.rectLine(0, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR, ANCHO_PANTALLA, ALTO_BARRA_SUPERIOR, 3f);

        if (minijuego != null) {
            float tiempoReferencia = 45f;
            float porcentaje = Math.max(0f, Math.min(1f, minijuego.getTiempoRestante() / tiempoReferencia));

            float bx = 1350f;
            float by = ALTO_PANTALLA - 56f;
            float bw = 500f;
            float bh = 32f;

            shapeRenderer.setColor(0.22f, 0.25f, 0.32f, 1f);
            shapeRenderer.rect(bx, by, bw, bh);

            if (porcentaje > 0.5f) {
                shapeRenderer.setColor(0.22f, 0.85f, 0.35f, 1f);
            } else if (porcentaje > 0.25f) {
                shapeRenderer.setColor(0.95f, 0.80f, 0.20f, 1f);
            } else {
                shapeRenderer.setColor(0.90f, 0.25f, 0.20f, 1f);
            }
            shapeRenderer.rect(bx, by, bw * porcentaje, bh);

            shapeRenderer.setColor(0.55f, 0.65f, 0.75f, 1f);
            shapeRenderer.rectLine(bx, by, bx + bw, by, 2f);
            shapeRenderer.rectLine(bx + bw, by, bx + bw, by + bh, 2f);
            shapeRenderer.rectLine(bx + bw, by + bh, bx, by + bh, 2f);
            shapeRenderer.rectLine(bx, by + bh, bx, by, 2f);
        }
    }

    private void dibujarTextos() {
        font.setColor(Color.WHITE);

        if (minijuego != null) {
            font.draw(batch, "SCHEMATIC GAME - CIRCUITO DE PRUEBA", 30f, ALTO_PANTALLA - 30f);
            font.draw(batch, String.format("TIEMPO: %.1f s", minijuego.getTiempoRestante()), 1360f, ALTO_PANTALLA - 32f);

            if (minijuego.isCompletado()) {
                font.setColor(Color.RED);
                font.draw(batch, "¡TIEMPO FINALIZADO!", 750f, ALTO_PANTALLA - 30f);
            }

            font.setColor(Color.WHITE);
            for (Componente comp : minijuego.getComponentes()) {
                String texto = comp.getIdentificador();
                if (comp instanceof Switch) {
                    texto += (((Switch) comp).isCerrado() ? " [ON]" : " [OFF]");
                }
                font.draw(batch, texto, comp.getPosicionX() + 8f, comp.getPosicionY() + 38f);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (camara != null) {
            camara.setToOrtho(false, width, height);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (mapaTexturas != null) {
            for (Texture tex : mapaTexturas.values()) {
                if (tex != null) tex.dispose();
            }
            mapaTexturas.clear();
        }
    }

    public Minijuego getMinijuego() {
        return minijuego;
    }

    public void setMinijuego(Minijuego minijuego) {
        this.minijuego = minijuego;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public OrthographicCamera getCamara() {
        return camara;
    }

    public Map<String, Texture> getMapaTexturas() {
        return mapaTexturas;
    }
}

/*
Que hay ahorita:
Dibuja en pantalla grande de 1920x1080 la cuadricula, los componentes con sus nombres y la barra de tiempo arriba. Conecta el raton con el controlador.

Que falta:
Dibujar la cinta lateral de piezas, los cables doblados a 90 grados y los graficos finales de cada componente.

Recomendaciones:
Usa formas simples con ShapeRenderer mientras pruebas mecanicas antes de preocuparte por dibujos o texturas pesadas.
*/
