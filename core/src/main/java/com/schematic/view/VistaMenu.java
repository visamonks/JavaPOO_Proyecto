package com.schematic.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.schematic.constants.Constantes;
import com.schematic.model.*;

public class VistaMenu extends ScreenAdapter {

    private Game juego;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontTitulo;
    private BitmapFont fontBotones;
    private OrthographicCamera camara;
    private Viewport viewport;
    private GlyphLayout glyphLayout;

    private static final int ESTADO_MENU = 0;
    private static final int ESTADO_TRABAJANDO = 1;
    private int estadoActual = ESTADO_MENU;
    private String seccionActual = "";

    private static final float ANCHO = Constantes.ANCHO_VENTANA;
    private static final float ALTO = Constantes.ALTO_VENTANA;

    private static final String[] OPCIONES = {"CONTINUAR", "GUÍA", "OPCIONES", "SALIR"};
    private static final float BOTON_ANCHO = 440f;
    private static final float BOTON_ALTO = 68f;
    private static final float BOTON_X = (ANCHO - BOTON_ANCHO) / 2f;
    private static final float[] BOTON_Y = {530f, 425f, 320f, 215f};

    private int opcionHover = -1;
    private boolean hoverProbar = false;
    private boolean hoverVolver = false;

    public VistaMenu() {
        this((Game) Gdx.app.getApplicationListener());
    }

    public VistaMenu(Game juego) {
        this.juego = juego;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();

        this.camara = new OrthographicCamera();
        this.viewport = new FitViewport(ANCHO, ALTO, camara);
        this.viewport.apply();

        this.fontTitulo = new BitmapFont();
        this.fontTitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontTitulo.getData().setScale(3.2f);

        this.fontBotones = new BitmapFont();
        this.fontBotones.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontBotones.getData().setScale(1.6f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }

                Vector3 click = viewport.unproject(new Vector3(screenX, screenY, 0));

                if (estadoActual == ESTADO_MENU) {
                    for (int i = 0; i < OPCIONES.length; i++) {
                        if (estaDentro(click.x, click.y, BOTON_X, BOTON_Y[i], BOTON_ANCHO, BOTON_ALTO)) {
                            seleccionarOpcion(i);
                            return true;
                        }
                    }
                } else if (estadoActual == ESTADO_TRABAJANDO) {
                    float volverX = (ANCHO - 360f) / 2f;
                    float volverY = 320f;
                    if (estaDentro(click.x, click.y, volverX, volverY, 360f, 60f)) {
                        estadoActual = ESTADO_MENU;
                        return true;
                    }

                    if ("CONTINUAR".equals(seccionActual)) {
                        float probarX = (ANCHO - 460f) / 2f;
                        float probarY = 415f;
                        if (estaDentro(click.x, click.y, probarX, probarY, 460f, 64f)) {
                            iniciarNivelJuego();
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
                    if (keycode == Input.Keys.ENTER && "CONTINUAR".equals(seccionActual)) {
                        iniciarNivelJuego();
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

    private void iniciarNivelJuego() {
        if (juego != null) {
            Minijuego minijuego = new Minijuego(45.0f);
            minijuego.agregarComponente(new Switch("SW_1", 240f, 480f, false));
            minijuego.agregarComponente(new CompuertaAND("AND_1", 600f, 480f, false, false));
            minijuego.agregarComponente(new Resistencia("R_1", 960f, 480f, 220, 220));
            minijuego.agregarComponente(new LED("LED_1", 1320f, 480f, true));
            juego.setScreen(new VistaNivel(minijuego));
        }
    }

    private boolean estaDentro(float x, float y, float bx, float by, float bw, float bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Vector3 mouse = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
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
        hoverProbar = false;
        hoverVolver = false;

        if (estadoActual == ESTADO_MENU) {
            for (int i = 0; i < OPCIONES.length; i++) {
                if (estaDentro(mx, my, BOTON_X, BOTON_Y[i], BOTON_ANCHO, BOTON_ALTO)) {
                    opcionHover = i;
                    break;
                }
            }
        } else if (estadoActual == ESTADO_TRABAJANDO) {
            float volverX = (ANCHO - 360f) / 2f;
            float volverY = 320f;
            hoverVolver = estaDentro(mx, my, volverX, volverY, 360f, 60f);

            if ("CONTINUAR".equals(seccionActual)) {
                float probarX = (ANCHO - 460f) / 2f;
                float probarY = 415f;
                hoverProbar = estaDentro(mx, my, probarX, probarY, 460f, 64f);
            }
        }
    }

    private void dibujarBotonesMenu() {
        for (int i = 0; i < OPCIONES.length; i++) {
            boolean hover = (i == opcionHover);
            float x = BOTON_X + (hover ? 14f : 0f);
            float y = BOTON_Y[i];

            shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
            shapeRenderer.rect(x + 8f, y - 8f, BOTON_ANCHO, BOTON_ALTO);

            if (hover) {
                shapeRenderer.setColor(0.10f, 0.16f, 0.24f, 0.98f);
            } else {
                shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.95f);
            }
            shapeRenderer.rect(x, y, BOTON_ANCHO, BOTON_ALTO);

            if (hover) {
                shapeRenderer.setColor(0f, 0.90f, 1f, 1f);
            } else {
                shapeRenderer.setColor(0.25f, 0.40f, 0.52f, 1f);
            }
            shapeRenderer.rectLine(x, y, x + BOTON_ANCHO, y, 3f);
            shapeRenderer.rectLine(x + BOTON_ANCHO, y, x + BOTON_ANCHO, y + BOTON_ALTO, 3f);
            shapeRenderer.rectLine(x + BOTON_ANCHO, y + BOTON_ALTO, x, y + BOTON_ALTO, 3f);
            shapeRenderer.rectLine(x, y + BOTON_ALTO, x, y, 3f);

            shapeRenderer.setColor(0f, 0.90f, 1f, 1f);
            float esq = 14f;
            shapeRenderer.rectLine(x, y, x + esq, y, 4.5f);
            shapeRenderer.rectLine(x, y, x, y + esq, 4.5f);
            shapeRenderer.rectLine(x + BOTON_ANCHO, y + BOTON_ALTO, x + BOTON_ANCHO - esq, y + BOTON_ALTO, 4.5f);
            shapeRenderer.rectLine(x + BOTON_ANCHO, y + BOTON_ALTO, x + BOTON_ANCHO, y + BOTON_ALTO - esq, 4.5f);
        }
    }

    private void dibujarTextosMenu() {
        String titulo = "SHORT CIRCUIT!";
        glyphLayout.setText(fontTitulo, titulo);
        float tx = (ANCHO - glyphLayout.width) / 2f;
        float ty = 850f;

        fontTitulo.setColor(0f, 0f, 0f, 0.7f);
        fontTitulo.draw(batch, titulo, tx + 4f, ty - 4f);

        fontTitulo.setColor(0f, 0.90f, 1f, 1f);
        fontTitulo.draw(batch, titulo, tx, ty);

        for (int i = 0; i < OPCIONES.length; i++) {
            boolean hover = (i == opcionHover);
            String texto = OPCIONES[i];
            glyphLayout.setText(fontBotones, texto);
            float bx = BOTON_X + (hover ? 14f : 0f) + (BOTON_ANCHO - glyphLayout.width) / 2f;
            float by = BOTON_Y[i] + 46f;

            fontBotones.setColor(0f, 0f, 0f, 0.8f);
            fontBotones.draw(batch, texto, bx + 2f, by - 2f);

            if (hover) {
                fontBotones.setColor(Color.WHITE);
            } else {
                fontBotones.setColor(0.70f, 0.85f, 0.95f, 1f);
            }
            fontBotones.draw(batch, texto, bx, by);
        }
    }

    private void dibujarVentanaTrabajando() {
        float cx = (ANCHO - 840f) / 2f;
        float cy = 240f;
        float cw = 840f;
        float ch = 520f;

        shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
        shapeRenderer.rect(cx + 12f, cy - 12f, cw, ch);

        shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.96f);
        shapeRenderer.rect(cx, cy, cw, ch);

        shapeRenderer.setColor(0f, 0.88f, 1f, 1f);
        shapeRenderer.rectLine(cx, cy, cx + cw, cy, 3.5f);
        shapeRenderer.rectLine(cx + cw, cy, cx + cw, cy + ch, 3.5f);
        shapeRenderer.rectLine(cx + cw, cy + ch, cx, cy + ch, 3.5f);
        shapeRenderer.rectLine(cx, cy + ch, cx, cy, 3.5f);

        float esq = 22f;
        shapeRenderer.rectLine(cx, cy, cx + esq, cy, 5.5f);
        shapeRenderer.rectLine(cx, cy, cx, cy + esq, 5.5f);
        shapeRenderer.rectLine(cx + cw, cy, cx + cw - esq, cy, 5.5f);
        shapeRenderer.rectLine(cx + cw, cy, cx + cw, cy + esq, 5.5f);
        shapeRenderer.rectLine(cx, cy + ch, cx + esq, cy + ch, 5.5f);
        shapeRenderer.rectLine(cx, cy + ch, cx, cy + ch - esq, 5.5f);
        shapeRenderer.rectLine(cx + cw, cy + ch, cx + cw - esq, cy + ch, 5.5f);
        shapeRenderer.rectLine(cx + cw, cy + ch, cx + cw, cy + ch - esq, 5.5f);

        if ("CONTINUAR".equals(seccionActual)) {
            float px = (ANCHO - 460f) / 2f + (hoverProbar ? 8f : 0f);
            float py = 415f;
            float pw = 460f;
            float ph = 64f;

            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(px + 6f, py - 6f, pw, ph);

            shapeRenderer.setColor(hoverProbar ? 0.12f : 0.08f, hoverProbar ? 0.22f : 0.12f, hoverProbar ? 0.32f : 0.18f, 0.95f);
            shapeRenderer.rect(px, py, pw, ph);

            shapeRenderer.setColor(hoverProbar ? Color.WHITE : new Color(0f, 0.88f, 1f, 1f));
            shapeRenderer.rectLine(px, py, px + pw, py, 2.5f);
            shapeRenderer.rectLine(px + pw, py, px + pw, py + ph, 2.5f);
            shapeRenderer.rectLine(px + pw, py + ph, px, py + ph, 2.5f);
            shapeRenderer.rectLine(px, py + ph, px, py, 2.5f);
        }

        float vx = (ANCHO - 360f) / 2f + (hoverVolver ? 8f : 0f);
        float vy = 320f;
        float vw = 360f;
        float vh = 60f;

        shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
        shapeRenderer.rect(vx + 6f, vy - 6f, vw, vh);

        shapeRenderer.setColor(hoverVolver ? 0.12f : 0.08f, hoverVolver ? 0.16f : 0.10f, hoverVolver ? 0.22f : 0.14f, 0.95f);
        shapeRenderer.rect(vx, vy, vw, vh);

        shapeRenderer.setColor(hoverVolver ? Color.WHITE : new Color(0.40f, 0.55f, 0.70f, 1f));
        shapeRenderer.rectLine(vx, vy, vx + vw, vy, 2.5f);
        shapeRenderer.rectLine(vx + vw, vy, vx + vw, vy + vh, 2.5f);
        shapeRenderer.rectLine(vx + vw, vy + vh, vx, vy + vh, 2.5f);
        shapeRenderer.rectLine(vx, vy + vh, vx, vy, 2.5f);
    }

    private void dibujarTextosTrabajando() {
        String titulo = "TRABAJANDO EN ESTO";
        glyphLayout.setText(fontTitulo, titulo);
        float tx = (ANCHO - glyphLayout.width) / 2f;

        fontTitulo.setColor(0f, 0f, 0f, 0.7f);
        fontTitulo.draw(batch, titulo, tx + 3f, 672f);

        fontTitulo.setColor(0f, 0.88f, 1f, 1f);
        fontTitulo.draw(batch, titulo, tx, 675f);

        if ("CONTINUAR".equals(seccionActual)) {
            String probar = "PROBAR CIRCUITO (NIVEL 1)";
            glyphLayout.setText(fontBotones, probar);
            float px = (ANCHO - 460f) / 2f + (hoverProbar ? 8f : 0f);
            float bx = px + (460f - glyphLayout.width) / 2f;
            float by = 415f + 43f;

            fontBotones.setColor(0f, 0f, 0f, 0.8f);
            fontBotones.draw(batch, probar, bx + 2f, by - 2f);

            fontBotones.setColor(hoverProbar ? Color.WHITE : new Color(0f, 0.88f, 1f, 1f));
            fontBotones.draw(batch, probar, bx, by);
        }

        String volver = "VOLVER";
        glyphLayout.setText(fontBotones, volver);
        float vx = (ANCHO - 360f) / 2f + (hoverVolver ? 8f : 0f);
        float bx = vx + (360f - glyphLayout.width) / 2f;
        float by = 320f + 41f;

        fontBotones.setColor(0f, 0f, 0f, 0.8f);
        fontBotones.draw(batch, volver, bx + 2f, by - 2f);

        fontBotones.setColor(hoverVolver ? Color.WHITE : new Color(0.65f, 0.78f, 0.90f, 1f));
        fontBotones.draw(batch, volver, bx, by);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
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
    }

    public Game getJuego() {
        return juego;
    }

    public void setJuego(Game juego) {
        this.juego = juego;
    }
}
