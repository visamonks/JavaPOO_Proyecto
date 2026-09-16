package com.schematic.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.schematic.constants.Constantes;
import com.schematic.controller.ControladorNivel;
import com.schematic.model.*;
import java.util.HashMap;
import java.util.Map;

public class VistaNivel extends ScreenAdapter {

    public static final float ANCHO_PANTALLA = Constantes.ANCHO_VENTANA;
    public static final float ALTO_PANTALLA = Constantes.ALTO_VENTANA;
    public static final float ALTO_BARRA_SUPERIOR = 50f;

    private Minijuego minijuego;
    private Game juego;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camara;
    private ControladorNivel controlador;
    private Map<String, Texture> mapaTexturas;
    private float tiempoAnimacion;

    public VistaNivel(Minijuego minijuego) {
        this(minijuego, null);
    }

    public VistaNivel(Minijuego minijuego, Game juego) {
        this.minijuego = minijuego;
        this.juego = juego;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        this.mapaTexturas = new HashMap<>();
        this.tiempoAnimacion = 0f;

        this.camara = new OrthographicCamera();
        this.camara.setToOrtho(false, ANCHO_PANTALLA, ALTO_PANTALLA);

        this.controlador = new ControladorNivel(minijuego, juego, camara);

        cargarTexturas();
    }

    private void cargarTexturas() {
        cargarTexturaSegura("resistencia_normal", "sprites/resistencia_normal/frame_00000.png");
        cargarTexturaSegura("resistencia_fail", "sprites/resistencia_fail/frame_00000.png");
        cargarTexturaSegura("led_normal", "sprites/led_normal/frame_00000.png");
        cargarTexturaSegura("led_prendido", "sprites/led_prendiendo/frame_00007.png");
        cargarTexturaSegura("led_quemado", "sprites/led_muriendo/frame_00000.png");
    }

    private void cargarTexturaSegura(String clave, String ruta) {
        try {
            if (Gdx.files.internal(ruta).exists()) {
                mapaTexturas.put(clave, new Texture(Gdx.files.internal(ruta)));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(controlador);
    }

    @Override
    public void render(float delta) {
        tiempoAnimacion += delta;

        if (minijuego != null) {
            minijuego.actualizar(delta);
        }

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camara.update();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarMesaTrabajo();
        dibujarCables();
        dibujarTerminales();
        dibujarBarraSuperior();
        shapeRenderer.end();

        batch.begin();
        dibujarSpritesComponentes();
        dibujarTextos();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void dibujarMesaTrabajo() {
        shapeRenderer.setColor(0.12f, 0.15f, 0.20f, 1f);
        shapeRenderer.rect(0, 0, ANCHO_PANTALLA, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR);

        shapeRenderer.setColor(0.16f, 0.20f, 0.26f, 0.4f);
        for (float x = 0; x <= ANCHO_PANTALLA; x += 40f) {
            shapeRenderer.rectLine(x, 0, x, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR, 1f);
        }
        for (float y = 0; y <= ALTO_PANTALLA - ALTO_BARRA_SUPERIOR; y += 40f) {
            shapeRenderer.rectLine(0, y, ANCHO_PANTALLA, y, 1f);
        }
    }

    private void dibujarCables() {
        if (minijuego == null) return;

        for (Cable cable : minijuego.getCables()) {
            if (cable.getTerminalOrigen() != null && cable.getTerminalDestino() != null) {
                Vector2 p1 = cable.getTerminalOrigen().getPosicionAbsoluta();
                Vector2 p2 = cable.getTerminalDestino().getPosicionAbsoluta();

                if (cable.isTieneEnergia()) {
                    shapeRenderer.setColor(0.1f, 0.9f, 0.4f, 0.4f);
                    shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, 8f);
                    shapeRenderer.setColor(0.2f, 1.0f, 0.5f, 1f);
                    shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, 4f);

                    float distancia = p1.dst(p2);
                    if (distancia > 10f) {
                        float paso = (tiempoAnimacion * 90f) % 40f;
                        for (float d = paso; d < distancia; d += 40f) {
                            float alpha = d / distancia;
                            float ex = MathUtils.lerp(p1.x, p2.x, alpha);
                            float ey = MathUtils.lerp(p1.y, p2.y, alpha);
                            shapeRenderer.setColor(1f, 1f, 1f, 0.9f);
                            shapeRenderer.circle(ex, ey, 3.5f);
                        }
                    }
                } else {
                    shapeRenderer.setColor(0.35f, 0.42f, 0.52f, 1f);
                    shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, 3.5f);
                }
            }
        }

        if (controlador != null && controlador.getTerminalInicioCable() != null) {
            Vector2 p1 = controlador.getTerminalInicioCable().getPosicionAbsoluta();
            float mx = controlador.getMouseActualX();
            float my = controlador.getMouseActualY();

            shapeRenderer.setColor(1.0f, 0.85f, 0.2f, 0.5f);
            shapeRenderer.rectLine(p1.x, p1.y, mx, my, 6f);
            shapeRenderer.setColor(1.0f, 0.95f, 0.3f, 1f);
            shapeRenderer.rectLine(p1.x, p1.y, mx, my, 3f);
        }
    }

    private void dibujarTerminales() {
        if (minijuego == null) return;

        for (Componente comp : minijuego.getComponentes()) {
            for (Terminal t : comp.getTerminales()) {
                Vector2 pos = t.getPosicionAbsoluta();

                if (t.getIdentificador().contains("POSITIVO")) {
                    shapeRenderer.setColor(0.90f, 0.20f, 0.20f, 1f);
                } else if (t.getIdentificador().contains("NEGATIVO")) {
                    shapeRenderer.setColor(0.20f, 0.50f, 0.95f, 1f);
                } else {
                    shapeRenderer.setColor(0.95f, 0.75f, 0.20f, 1f);
                }

                shapeRenderer.circle(pos.x, pos.y, 11f);

                if (t.estaConectado()) {
                    shapeRenderer.setColor(1f, 1f, 1f, 1f);
                    shapeRenderer.circle(pos.x, pos.y, 5f);
                } else {
                    shapeRenderer.setColor(0.12f, 0.15f, 0.20f, 1f);
                    shapeRenderer.circle(pos.x, pos.y, 5f);
                }
            }
        }
    }

    private void dibujarSpritesComponentes() {
        if (minijuego == null) return;

        for (Componente comp : minijuego.getComponentes()) {
            float x = comp.getPosicionX();
            float y = comp.getPosicionY();
            float w = comp.getAncho();
            float h = comp.getAlto();

            if (comp instanceof FuenteAlimentacion) {
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                dibujarCajaFuente((FuenteAlimentacion) comp);
                shapeRenderer.end();
                batch.begin();
            } else if (comp instanceof Resistencia) {
                Resistencia res = (Resistencia) comp;
                Texture tex = res.isQuemada() ? mapaTexturas.get("resistencia_fail") : mapaTexturas.get("resistencia_normal");
                if (tex != null) {
                    batch.draw(tex, x, y, w, h);
                } else {
                    batch.end();
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(0.70f, 0.60f, 0.45f, 1f);
                    shapeRenderer.rect(x + 50f, y + 60f, w - 100f, h - 120f);
                    shapeRenderer.end();
                    batch.begin();
                }
            } else if (comp instanceof LED) {
                LED led = (LED) comp;
                Texture tex;
                if (led.isQuemado()) {
                    tex = mapaTexturas.get("led_quemado");
                } else if ("EXITO".equals(led.getEstadoActual())) {
                    tex = mapaTexturas.get("led_prendido");
                } else {
                    tex = mapaTexturas.get("led_normal");
                }

                if (tex != null) {
                    batch.draw(tex, x, y, w, h);
                } else {
                    batch.end();
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    if ("EXITO".equals(led.getEstadoActual())) {
                        shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 1f);
                    } else if (led.isQuemado()) {
                        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
                    } else {
                        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1f);
                    }
                    shapeRenderer.circle(x + w * 0.5f, y + h * 0.5f, 40f);
                    shapeRenderer.end();
                    batch.begin();
                }
            }
        }
    }

    private void dibujarCajaFuente(FuenteAlimentacion fuente) {
        float x = fuente.getPosicionX();
        float y = fuente.getPosicionY();
        float w = fuente.getAncho();
        float h = fuente.getAlto();

        shapeRenderer.setColor(0.18f, 0.22f, 0.28f, 1f);
        shapeRenderer.rect(x, y, w, h);

        shapeRenderer.setColor(0.28f, 0.36f, 0.48f, 1f);
        shapeRenderer.rectLine(x, y, x + w, y, 3f);
        shapeRenderer.rectLine(x + w, y, x + w, y + h, 3f);
        shapeRenderer.rectLine(x + w, y + h, x, y + h, 3f);
        shapeRenderer.rectLine(x, y + h, x, y, 3f);

        shapeRenderer.setColor(0.08f, 0.10f, 0.14f, 1f);
        shapeRenderer.rect(x + 18f, y + h - 100f, w - 36f, 75f);

        shapeRenderer.setColor(0.2f, 0.85f, 0.3f, 1f);
        shapeRenderer.circle(x + 36f, y + 45f, 8f);
    }

    private void dibujarBarraSuperior() {
        shapeRenderer.setColor(0.13f, 0.16f, 0.22f, 1f);
        shapeRenderer.rect(0, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR, ANCHO_PANTALLA, ALTO_BARRA_SUPERIOR);

        shapeRenderer.setColor(0.25f, 0.32f, 0.42f, 1f);
        shapeRenderer.rectLine(0, ALTO_PANTALLA - ALTO_BARRA_SUPERIOR, ANCHO_PANTALLA, ALTO_BARRA_SUPERIOR, 2.5f);
    }

    private void dibujarTextos() {
        if (minijuego != null) {
            font.setColor(Color.WHITE);
            String texto = "Conecta la fuente, la resistencia y el LED para encender el circuito.";
            if (minijuego.isCompletado() && minijuego.estaGanado()) {
                texto = "Circuito completado.";
            } else if (minijuego.getMensajeEstado().contains("QUEMADO")) {
                texto = "El LED se quemó. Conéctalo a la resistencia.";
            } else if (minijuego.getMensajeEstado().contains("CORTOCIRCUITO")) {
                texto = "Cortocircuito entre terminales.";
            }
            font.draw(batch, texto, 32f, ALTO_PANTALLA - 17f);
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
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
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

    public ControladorNivel getControlador() {
        return controlador;
    }
}
