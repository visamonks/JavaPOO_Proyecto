package com.schematic.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.schematic.constants.Constantes;
import com.schematic.model.Minijuego;

public class VistaMenu extends ScreenAdapter {

    private Game juego;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontTitulo;
    private BitmapFont fontBotones;
    private BitmapFont fontTexto;
    private OrthographicCamera camara;
    private GlyphLayout glyphLayout;

    private static final int ESTADO_MENU = 0;
    private static final int ESTADO_TRABAJANDO = 1;
    private int estadoActual = ESTADO_MENU;
    private String seccionActual = "";

    private static final float ANCHO = Constantes.ANCHO_VENTANA;
    private static final float ALTO = Constantes.ALTO_VENTANA;

    private static final String[] OPCIONES = {"CONTINUAR", "GUÍA", "OPCIONES", "SALIR"};
    private static final float BOTON_ANCHO = 400f;
    private static final float BOTON_ALTO = 60f;
    private static final float BOTON_X = (ANCHO - BOTON_ANCHO) / 2f;
    private static final float[] BOTON_Y = {520f, 420f, 320f, 220f};

    private int opcionHover = -1;

    public VistaMenu() {
        this((Game) Gdx.app.getApplicationListener());
    }

    public VistaMenu(Game juego) {
        this.juego = juego;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();

        this.fontTitulo = new BitmapFont();
        this.fontTitulo.getData().setScale(3.5f);

        this.fontBotones = new BitmapFont();
        this.fontBotones.getData().setScale(1.5f);

        this.fontTexto = new BitmapFont();
        this.fontTexto.getData().setScale(1.2f);

        this.camara = new OrthographicCamera();
        this.camara.setToOrtho(false, ANCHO, ALTO);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }

                Vector3 click = camara.unproject(new Vector3(screenX, screenY, 0));

                if (estadoActual == ESTADO_MENU) {
                    for (int i = 0; i < OPCIONES.length; i++) {
                        if (estaDentro(click.x, click.y, BOTON_X, BOTON_Y[i], BOTON_ANCHO, BOTON_ALTO)) {
                            seleccionarOpcion(i);
                            return true;
                        }
                    }
                } else if (estadoActual == ESTADO_TRABAJANDO) {
                    float volverX = (ANCHO - 300f) / 2f;
                    float volverY = 320f;
                    if (estaDentro(click.x, click.y, volverX, volverY, 300f, 50f)) {
                        estadoActual = ESTADO_MENU;
                        return true;
                    }

                    if ("CONTINUAR".equals(seccionActual)) {
                        float probarX = (ANCHO - 400f) / 2f;
                        float probarY = 400f;
                        if (estaDentro(click.x, click.y, probarX, probarY, 400f, 50f)) {
                            iniciarJuego();
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (estadoActual == ESTADO_MENU) {
                    if (keycode == Input.Keys.ESCAPE) {
                        Gdx.app.exit();
                        return true;
                    }
                } else if (estadoActual == ESTADO_TRABAJANDO) {
                    if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                        estadoActual = ESTADO_MENU;
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void seleccionarOpcion(int indice) {
        if (indice == 0) {
            seccionActual = "CONTINUAR";
            estadoActual = ESTADO_TRABAJANDO;
        } else if (indice == 1) {
            seccionActual = "GUÍA";
            estadoActual = ESTADO_TRABAJANDO;
        } else if (indice == 2) {
            seccionActual = "OPCIONES";
            estadoActual = ESTADO_TRABAJANDO;
        } else if (indice == 3) {
            Gdx.app.exit();
        }
    }

    private void iniciarJuego() {
        if (juego != null) {
            Minijuego minijuego = new Minijuego(45.0f);
            juego.setScreen(new VistaNivel(minijuego));
        }
    }

    private boolean estaDentro(float x, float y, float bx, float by, float bw, float bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    @Override
    public void render(float delta) {
        camara.update();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Vector3 mouse = camara.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        actualizarHover(mouse.x, mouse.y);

        Gdx.gl.glClearColor(0.04f, 0.06f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (estadoActual == ESTADO_MENU) {
            dibujarBotonesMenu();
        } else {
            dibujarVentanaTrabajando();
        }
        shapeRenderer.end();

        batch.begin();
        if (estadoActual == ESTADO_MENU) {
            dibujarTextosMenu();
        } else {
            dibujarTextosTrabajando();
        }
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void actualizarHover(float mx, float my) {
        opcionHover = -1;
        if (estadoActual == ESTADO_MENU) {
            for (int i = 0; i < OPCIONES.length; i++) {
                if (estaDentro(mx, my, BOTON_X, BOTON_Y[i], BOTON_ANCHO, BOTON_ALTO)) {
                    opcionHover = i;
                    break;
                }
            }
        }
    }

    private void dibujarBotonesMenu() {
        for (int i = 0; i < OPCIONES.length; i++) {
            boolean hover = (i == opcionHover);

            if (hover) {
                shapeRenderer.setColor(0.12f, 0.18f, 0.25f, 1f);
            } else {
                shapeRenderer.setColor(0.08f, 0.10f, 0.14f, 1f);
            }
            shapeRenderer.rect(BOTON_X, BOTON_Y[i], BOTON_ANCHO, BOTON_ALTO);

            if (hover) {
                shapeRenderer.setColor(0f, 0.88f, 1f, 1f);
            } else {
                shapeRenderer.setColor(0.3f, 0.4f, 0.5f, 1f);
            }
            shapeRenderer.rectLine(BOTON_X, BOTON_Y[i], BOTON_X + BOTON_ANCHO, BOTON_Y[i], 2f);
            shapeRenderer.rectLine(BOTON_X + BOTON_ANCHO, BOTON_Y[i], BOTON_X + BOTON_ANCHO, BOTON_Y[i] + BOTON_ALTO, 2f);
            shapeRenderer.rectLine(BOTON_X + BOTON_ANCHO, BOTON_Y[i] + BOTON_ALTO, BOTON_X, BOTON_Y[i] + BOTON_ALTO, 2f);
            shapeRenderer.rectLine(BOTON_X, BOTON_Y[i] + BOTON_ALTO, BOTON_X, BOTON_Y[i], 2f);
        }
    }

    private void dibujarTextosMenu() {
        String titulo = "SHORT CIRCUIT!";
        glyphLayout.setText(fontTitulo, titulo);
        float tx = (ANCHO - glyphLayout.width) / 2f;
        float ty = 850f;

        fontTitulo.setColor(0f, 0.88f, 1f, 1f);
        fontTitulo.draw(batch, titulo, tx, ty);

        for (int i = 0; i < OPCIONES.length; i++) {
            String texto = OPCIONES[i];
            glyphLayout.setText(fontBotones, texto);
            float bx = BOTON_X + (BOTON_ANCHO - glyphLayout.width) / 2f;
            float by = BOTON_Y[i] + 40f;

            if (i == opcionHover) {
                fontBotones.setColor(1f, 1f, 1f, 1f);
            } else {
                fontBotones.setColor(0.7f, 0.8f, 0.9f, 1f);
            }
            fontBotones.draw(batch, texto, bx, by);
        }
    }

    private void dibujarVentanaTrabajando() {
        float cx = (ANCHO - 800f) / 2f;
        float cy = 250f;
        float cw = 800f;
        float ch = 500f;

        shapeRenderer.setColor(0.07f, 0.09f, 0.13f, 1f);
        shapeRenderer.rect(cx, cy, cw, ch);

        shapeRenderer.setColor(0f, 0.88f, 1f, 1f);
        shapeRenderer.rectLine(cx, cy, cx + cw, cy, 3f);
        shapeRenderer.rectLine(cx + cw, cy, cx + cw, cy + ch, 3f);
        shapeRenderer.rectLine(cx + cw, cy + ch, cx, cy + ch, 3f);
        shapeRenderer.rectLine(cx, cy + ch, cx, cy, 3f);

        if ("CONTINUAR".equals(seccionActual)) {
            float px = (ANCHO - 400f) / 2f;
            float py = 400f;
            shapeRenderer.setColor(0.12f, 0.20f, 0.28f, 1f);
            shapeRenderer.rect(px, py, 400f, 50f);
            shapeRenderer.setColor(0f, 0.88f, 1f, 1f);
            shapeRenderer.rectLine(px, py, px + 400f, py, 2f);
            shapeRenderer.rectLine(px + 400f, py, px + 400f, py + 50f, 2f);
            shapeRenderer.rectLine(px + 400f, py + 50f, px, py + 50f, 2f);
            shapeRenderer.rectLine(px, py + 50f, px, py, 2f);
        }

        float vx = (ANCHO - 300f) / 2f;
        float vy = 320f;
        shapeRenderer.setColor(0.12f, 0.16f, 0.22f, 1f);
        shapeRenderer.rect(vx, vy, 300f, 50f);
        shapeRenderer.setColor(0.5f, 0.6f, 0.7f, 1f);
        shapeRenderer.rectLine(vx, vy, vx + 300f, vy, 2f);
        shapeRenderer.rectLine(vx + 300f, vy, vx + 300f, vy + 50f, 2f);
        shapeRenderer.rectLine(vx + 300f, vy + 50f, vx, vy + 50f, 2f);
        shapeRenderer.rectLine(vx, vy + 50f, vx, vy, 2f);
    }

    private void dibujarTextosTrabajando() {
        String titulo = "TRABAJANDO EN ESTO";
        glyphLayout.setText(fontTitulo, titulo);
        float tx = (ANCHO - glyphLayout.width) / 2f;
        fontTitulo.setColor(0f, 0.88f, 1f, 1f);
        fontTitulo.draw(batch, titulo, tx, 680f);

        String info = "Seccion: " + seccionActual;
        glyphLayout.setText(fontTexto, info);
        fontTexto.setColor(Color.WHITE);
        fontTexto.draw(batch, info, (ANCHO - glyphLayout.width) / 2f, 590f);

        if ("CONTINUAR".equals(seccionActual)) {
            String probar = "PROBAR CIRCUITO";
            glyphLayout.setText(fontBotones, probar);
            fontBotones.setColor(0f, 0.88f, 1f, 1f);
            fontBotones.draw(batch, probar, (ANCHO - glyphLayout.width) / 2f, 435f);
        }

        String volver = "VOLVER";
        glyphLayout.setText(fontBotones, volver);
        fontBotones.setColor(Color.LIGHT_GRAY);
        fontBotones.draw(batch, volver, (ANCHO - glyphLayout.width) / 2f, 355f);
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
        if (fontTitulo != null) {
            fontTitulo.dispose();
        }
        if (fontBotones != null) {
            fontBotones.dispose();
        }
        if (fontTexto != null) {
            fontTexto.dispose();
        }
    }

    public Game getJuego() {
        return juego;
    }

    public void setJuego(Game juego) {
        this.juego = juego;
    }
}
