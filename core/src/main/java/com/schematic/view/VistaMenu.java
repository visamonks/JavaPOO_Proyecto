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
import com.badlogic.gdx.math.MathUtils;
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
    private BitmapFont fontDetalle;
    private OrthographicCamera camara;
    private Viewport viewport;
    private GlyphLayout glyphLayout;

    private static final int PANTALLA_MENU = 0;
    private static final int PANTALLA_TRABAJANDO = 1;
    private int estadoActual = PANTALLA_MENU;
    private String seccionActual = "";

    private float tiempoTotal = 0f;
    private float offsetGridPerspectiva = 0f;

    private static final float ANCHO = Constantes.ANCHO_VENTANA;
    private static final float ALTO = Constantes.ALTO_VENTANA;

    private static final String[] TEXTOS_BOTONES = {"CONTINUAR", "GUÍA", "OPCIONES", "SALIR"};
    private final float[] animHover = new float[4];
    private int indiceSeleccionadoTeclado = -1;
    private boolean mouseActivo = true;

    private static final float BOTON_ANCHO = 510f;
    private static final float BOTON_ALTO = 78f;
    private static final float BOTON_BASE_X = (ANCHO - BOTON_ANCHO) / 2f;
    private static final float[] BOTONES_Y = {530f, 415f, 300f, 185f};

    private final Color COLOR_FONDO_NEGRO = new Color(0.02f, 0.03f, 0.06f, 1f);
    private final Color COLOR_SLATE_OSCURO = new Color(0.06f, 0.09f, 0.14f, 1f);
    private final Color COLOR_GRIS_METAL = new Color(0.30f, 0.38f, 0.48f, 1f);
    private final Color COLOR_GRIS_CLARO = new Color(0.65f, 0.76f, 0.88f, 1f);
    private final Color COLOR_NEON_CELESTE = new Color(0.0f, 0.88f, 1.0f, 1f);
    private final Color COLOR_CELESTE_BRILLANTE = new Color(0.60f, 0.95f, 1.0f, 1f);
    private final Color COLOR_CAJA_OSCURA = new Color(0.05f, 0.07f, 0.11f, 0.96f);

    private float animVolver = 0f;
    private float animProbar = 0f;

    private boolean enTransicionNivel = false;
    private float tiempoTransicionNivel = 0f;
    private static final float DURACION_TRANSICION = 0.65f;

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
        this.fontTitulo.getData().setScale(7.2f);

        this.fontBotones = new BitmapFont();
        this.fontBotones.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontBotones.getData().setScale(1.9f);

        this.fontDetalle = new BitmapFont();
        this.fontDetalle.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontDetalle.getData().setScale(1.35f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT || enTransicionNivel) {
                    return false;
                }

                Vector3 mundo = viewport.unproject(new Vector3(screenX, screenY, 0));
                float mx = mundo.x;
                float my = mundo.y;

                if (estadoActual == PANTALLA_MENU) {
                    for (int i = 0; i < TEXTOS_BOTONES.length; i++) {
                        float flotacion = MathUtils.sin(tiempoTotal * 3.8f + i * 1.6f) * 6f;
                        float bx = BOTON_BASE_X + (animHover[i] * 38f);
                        float by = BOTONES_Y[i] + flotacion;
                        if (estaDentro(mx, my, bx, by, BOTON_ANCHO, BOTON_ALTO)) {
                            activarOpcion(i);
                            return true;
                        }
                    }
                } else if (estadoActual == PANTALLA_TRABAJANDO) {
                    float volverX = (ANCHO - 440f) / 2f + (animVolver * 24f);
                    float volverY = 320f;
                    if (estaDentro(mx, my, volverX, volverY, 440f, 68f)) {
                        estadoActual = PANTALLA_MENU;
                        return true;
                    }

                    if ("CONTINUAR".equals(seccionActual)) {
                        float probarX = (ANCHO - 500f) / 2f + (animProbar * 24f);
                        float probarY = 420f;
                        if (estaDentro(mx, my, probarX, probarY, 500f, 68f)) {
                            iniciarNivelJuego();
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (estadoActual == PANTALLA_MENU) {
                    if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                        mouseActivo = false;
                        indiceSeleccionadoTeclado--;
                        if (indiceSeleccionadoTeclado < 0) {
                            indiceSeleccionadoTeclado = TEXTOS_BOTONES.length - 1;
                        }
                        return true;
                    }
                    if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                        mouseActivo = false;
                        indiceSeleccionadoTeclado++;
                        if (indiceSeleccionadoTeclado >= TEXTOS_BOTONES.length) {
                            indiceSeleccionadoTeclado = 0;
                        }
                        return true;
                    }
                    if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                        if (indiceSeleccionadoTeclado >= 0 && indiceSeleccionadoTeclado < TEXTOS_BOTONES.length) {
                            activarOpcion(indiceSeleccionadoTeclado);
                            return true;
                        }
                    }
                    if (keycode == Input.Keys.ESCAPE) {
                        Gdx.app.exit();
                        return true;
                    }
                } else if (estadoActual == PANTALLA_TRABAJANDO) {
                    if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                        estadoActual = PANTALLA_MENU;
                        return true;
                    }
                    if (keycode == Input.Keys.ENTER && "CONTINUAR".equals(seccionActual)) {
                        iniciarNivelJuego();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseActivo = true;
                return false;
            }
        });
    }

    private void activarOpcion(int indice) {
        if (indice == 0) {
            seccionActual = "CONTINUAR";
            estadoActual = PANTALLA_TRABAJANDO;
        } else if (indice == 1) {
            seccionActual = "GUÍA";
            estadoActual = PANTALLA_TRABAJANDO;
        } else if (indice == 2) {
            seccionActual = "OPCIONES";
            estadoActual = PANTALLA_TRABAJANDO;
        } else if (indice == 3) {
            Gdx.app.exit();
        }
    }

    private void iniciarNivelJuego() {
        if (!enTransicionNivel) {
            enTransicionNivel = true;
            tiempoTransicionNivel = 0f;
        }
    }

    private boolean estaDentro(float px, float py, float bx, float by, float bw, float bh) {
        return px >= bx && px <= bx + bw && py >= by && py <= by + bh;
    }

    @Override
    public void render(float delta) {
        tiempoTotal += delta;
        offsetGridPerspectiva = (offsetGridPerspectiva + delta * 95f) % 70f;

        viewport.apply();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Vector3 raton = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float mx = raton.x;
        float my = raton.y;

        actualizarAnimaciones(delta, mx, my);

        Gdx.gl.glClearColor(COLOR_FONDO_NEGRO.r, COLOR_FONDO_NEGRO.g, COLOR_FONDO_NEGRO.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarFondoRedElectrica();
        dibujarOndaOsciloscopio();
        dibujarRejillaCyberEspacio();
        dibujarRayosElectricosFondo();

        if (estadoActual == PANTALLA_MENU) {
            dibujarCajaCartelPrincipal();
            dibujarBotonesMenu();
        } else {
            dibujarCajaTrabajando();
        }
        shapeRenderer.end();

        batch.begin();
        if (estadoActual == PANTALLA_MENU) {
            dibujarCartelPrincipalShortCircuit();
            dibujarTextosBotonesMenu();
        } else {
            dibujarTextosTrabajando();
        }
        batch.end();

        if (enTransicionNivel) {
            tiempoTransicionNivel += delta;
            float progreso = MathUtils.clamp(tiempoTransicionNivel / DURACION_TRANSICION, 0f, 1f);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COLOR_FONDO_NEGRO.r, COLOR_FONDO_NEGRO.g, COLOR_FONDO_NEGRO.b, progreso);
            shapeRenderer.rect(0, 0, ANCHO, ALTO);

            float barraY = ALTO * progreso;
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.95f);
            shapeRenderer.rectLine(0, barraY, ANCHO, barraY, 5f);
            shapeRenderer.setColor(1f, 1f, 1f, 0.98f);
            shapeRenderer.rectLine(0, barraY, ANCHO, barraY, 2f);
            shapeRenderer.end();

            batch.begin();
            String textoCarga = ">> INICIALIZANDO BANCO DE TRABAJO... <<";
            glyphLayout.setText(fontBotones, textoCarga);
            fontBotones.setColor(COLOR_NEON_CELESTE);
            fontBotones.draw(batch, textoCarga, (ANCHO - glyphLayout.width) / 2f, ALTO / 2f + 20f);
            batch.end();

            if (tiempoTransicionNivel >= DURACION_TRANSICION) {
                if (juego != null) {
                    Minijuego minijuego = new Minijuego(1, "NIVEL 1: ENCIENDE EL LED", 60.0f);
                    juego.setScreen(new VistaNivel(minijuego, juego));
                }
            }
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void actualizarAnimaciones(float delta, float mx, float my) {
        if (estadoActual == PANTALLA_MENU) {
            for (int i = 0; i < TEXTOS_BOTONES.length; i++) {
                boolean hover = false;
                if (mouseActivo) {
                    float flotacion = MathUtils.sin(tiempoTotal * 3.8f + i * 1.6f) * 6f;
                    float bx = BOTON_BASE_X + (animHover[i] * 38f);
                    float by = BOTONES_Y[i] + flotacion;
                    if (estaDentro(mx, my, bx, by, BOTON_ANCHO, BOTON_ALTO)) {
                        hover = true;
                        indiceSeleccionadoTeclado = i;
                    }
                } else {
                    hover = (i == indiceSeleccionadoTeclado);
                }

                float target = hover ? 1f : 0f;
                animHover[i] = MathUtils.lerp(animHover[i], target, delta * 18f);
            }
        } else {
            float volverX = (ANCHO - 440f) / 2f + (animVolver * 24f);
            float volverY = 320f;
            boolean hoverVolver = estaDentro(mx, my, volverX, volverY, 440f, 68f);
            animVolver = MathUtils.lerp(animVolver, hoverVolver ? 1f : 0f, delta * 18f);

            if ("CONTINUAR".equals(seccionActual)) {
                float probarX = (ANCHO - 500f) / 2f + (animProbar * 24f);
                float probarY = 420f;
                boolean hoverProbar = estaDentro(mx, my, probarX, probarY, 500f, 68f);
                animProbar = MathUtils.lerp(animProbar, hoverProbar ? 1f : 0f, delta * 18f);
            }
        }
    }

    private void dibujarFondoRedElectrica() {
        for (float y = 0; y < ALTO; y += 35f) {
            float t = y / ALTO;
            float r = MathUtils.lerp(0.015f, 0.05f, t);
            float g = MathUtils.lerp(0.025f, 0.07f, t);
            float b = MathUtils.lerp(0.05f, 0.13f, t);
            shapeRenderer.setColor(r, g, b, 1f);
            shapeRenderer.rect(0, y, ANCHO, 36f);
        }

        shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.18f);

        float[] pistasY = {90f, 180f, 290f, 410f, 530f, 650f, 750f, 870f, 980f, 1040f};
        for (int i = 0; i < pistasY.length; i++) {
            float ondaY = MathUtils.sin(tiempoTotal * 2.8f + i * 0.9f) * 7f;
            float py = pistasY[i] + ondaY;
            shapeRenderer.rectLine(0, py, ANCHO, py, 2.5f);

            float velocidadX = 260f + (i * 65f);
            float pulsoX1 = ((tiempoTotal * velocidadX) + i * 350f) % (ANCHO + 300f) - 150f;
            float pulsoX2 = (ANCHO + 150f) - (((tiempoTotal * (velocidadX * 0.8f)) + i * 280f) % (ANCHO + 300f));

            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.88f);
            shapeRenderer.rectLine(pulsoX1 - 55f, py, pulsoX1 + 55f, py, 4.5f);
            shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE.r, COLOR_CELESTE_BRILLANTE.g, COLOR_CELESTE_BRILLANTE.b, 0.95f);
            shapeRenderer.rectLine(pulsoX2 - 40f, py, pulsoX2 + 40f, py, 3.8f);

            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(pulsoX1, py, 5.2f);
            shapeRenderer.circle(pulsoX2, py, 4.2f);

            shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.18f);
        }

        float[] pistasX = {120f, 260f, 440f, 660f, 1260f, 1480f, 1660f, 1820f};
        for (int i = 0; i < pistasX.length; i++) {
            float ondaX = MathUtils.cos(tiempoTotal * 2.4f + i * 1.1f) * 6f;
            float px = pistasX[i] + ondaX;
            shapeRenderer.rectLine(px, 0, px, ALTO, 2.4f);

            float velocidadY = 240f + (i * 55f);
            float pulsoY = ((tiempoTotal * velocidadY) + i * 290f) % (ALTO + 200f) - 100f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.85f);
            shapeRenderer.rectLine(px, pulsoY - 45f, px, pulsoY + 45f, 4.2f);

            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(px, pulsoY, 4.5f);

            shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.18f);
        }

        for (int i = 0; i < pistasX.length; i++) {
            for (int j = 0; j < pistasY.length; j++) {
                float px = pistasX[i] + MathUtils.cos(tiempoTotal * 2.4f + i * 1.1f) * 6f;
                float py = pistasY[j] + MathUtils.sin(tiempoTotal * 2.8f + j * 0.9f) * 7f;

                float pulsoOnda = (tiempoTotal * 4.5f + i + j * 1.3f);
                float radioExpansion = (pulsoOnda % 3.0f) * 7.5f + 4f;
                float alphaExpansion = MathUtils.clamp(1f - (radioExpansion / 26f), 0f, 0.65f);

                shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alphaExpansion);
                shapeRenderer.circle(px, py, radioExpansion);

                shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE.r, COLOR_CELESTE_BRILLANTE.g, COLOR_CELESTE_BRILLANTE.b, 0.95f);
                shapeRenderer.circle(px, py, 4f);

                shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
                shapeRenderer.circle(px, py, 2.2f);
            }
        }
    }

    private void dibujarOndaOsciloscopio() {
        float centroY = 675f;
        float paso = 16f;
        float prevX = 0f;
        float prevY = centroY + MathUtils.sin(tiempoTotal * 7f) * 22f;

        for (float x = paso; x <= ANCHO; x += paso) {
            float onda1 = MathUtils.sin(x * 0.012f + tiempoTotal * 7.5f) * 26f;
            float onda2 = MathUtils.cos(x * 0.028f - tiempoTotal * 5f) * 14f;
            float y = centroY + onda1 + onda2;

            float alpha = 0.28f + MathUtils.sin(tiempoTotal * 4f + x * 0.01f) * 0.15f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alpha);
            shapeRenderer.rectLine(prevX, prevY, x, y, 3f);

            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.8f);
            shapeRenderer.rectLine(prevX, prevY, x, y, 1.2f);

            prevX = x;
            prevY = y;
        }
    }

    private void dibujarRejillaCyberEspacio() {
        float baseRejilla = 130f;
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.22f);

        for (float x = 0; x <= ANCHO; x += 100f) {
            float ondaX = MathUtils.sin(tiempoTotal * 3.6f + x * 0.015f) * 18f;
            shapeRenderer.rectLine(x + ondaX, 0, x - (x - ANCHO / 2f) * 0.45f, baseRejilla + 150f, 2.2f);
        }

        for (float y = 0; y < baseRejilla + 150f; y += 24f) {
            float animY = (y + offsetGridPerspectiva) % (baseRejilla + 150f);
            float deformacionOnda = MathUtils.sin(tiempoTotal * 4.5f + animY * 0.06f) * 7f;
            float alpha = (1f - (animY / (baseRejilla + 150f))) * 0.42f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alpha);
            shapeRenderer.rectLine(0, animY + deformacionOnda, ANCHO, animY + deformacionOnda, 2.2f);
        }
    }

    private void dibujarRayosElectricosFondo() {
        float cicloRayo = (tiempoTotal * 2.2f) % 3.0f;
        if (cicloRayo < 0.35f) {
            float x1 = 150f + MathUtils.sin(tiempoTotal * 5f) * 80f;
            float y1 = 700f;
            float x2 = 550f;
            float y2 = 450f;
            dibujarRayoProcedural(x1, y1, x2, y2, 6, 25f, COLOR_CELESTE_BRILLANTE, 3.5f);
        }

        if (cicloRayo > 1.4f && cicloRayo < 1.75f) {
            float x1 = ANCHO - 220f;
            float y1 = 780f;
            float x2 = ANCHO - 580f;
            float y2 = 480f;
            dibujarRayoProcedural(x1, y1, x2, y2, 6, 28f, COLOR_NEON_CELESTE, 3.5f);
        }
    }

    private void dibujarRayoProcedural(float x1, float y1, float x2, float y2, int pasos, float dispersion, Color col, float grosor) {
        float actX = x1;
        float actY = y1;
        for (int p = 1; p <= pasos; p++) {
            float t = (float) p / pasos;
            float sigX = MathUtils.lerp(x1, x2, t);
            float sigY = MathUtils.lerp(y1, y2, t);

            if (p < pasos) {
                sigX += MathUtils.sin(tiempoTotal * 40f + p * 3.7f) * dispersion;
                sigY += MathUtils.cos(tiempoTotal * 40f + p * 2.9f) * dispersion;
            }

            shapeRenderer.setColor(col.r, col.g, col.b, 0.95f);
            shapeRenderer.rectLine(actX, actY, sigX, sigY, grosor);

            shapeRenderer.setColor(1f, 1f, 1f, 0.98f);
            shapeRenderer.rectLine(actX, actY, sigX, sigY, grosor * 0.45f);

            actX = sigX;
            actY = sigY;
        }
    }

    private void dibujarCajaCartelPrincipal() {
        float oscY = MathUtils.sin(tiempoTotal * 4.2f) * 14f;
        float oscX = MathUtils.cos(tiempoTotal * 2.8f) * 9f;

        float cartelW = 1680f;
        float cartelH = 240f;
        float cartelX = (ANCHO - cartelW) / 2f + oscX;
        float cartelY = 760f + oscY;

        shapeRenderer.setColor(0f, 0f, 0f, 0.92f);
        shapeRenderer.rect(cartelX + 22f, cartelY - 22f, cartelW, cartelH);

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.35f);
        shapeRenderer.rect(cartelX + 12f, cartelY - 12f, cartelW, cartelH);

        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(cartelX, cartelY, cartelW, cartelH);

        for (float sx = cartelX; sx < cartelX + cartelW; sx += 48f) {
            shapeRenderer.setColor(COLOR_SLATE_OSCURO.r, COLOR_SLATE_OSCURO.g, COLOR_SLATE_OSCURO.b, 0.35f);
            shapeRenderer.rectLine(sx, cartelY, sx + 24f, cartelY + cartelH, 3f);
        }

        float pulsoBorde = MathUtils.sin(tiempoTotal * 6f) * 0.25f + 0.75f;
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, pulsoBorde);
        shapeRenderer.rectLine(cartelX, cartelY, cartelX + cartelW, cartelY, 5.5f);
        shapeRenderer.rectLine(cartelX + cartelW, cartelY, cartelX + cartelW, cartelY + cartelH, 5.5f);
        shapeRenderer.rectLine(cartelX + cartelW, cartelY + cartelH, cartelX, cartelY + cartelH, 5.5f);
        shapeRenderer.rectLine(cartelX, cartelY + cartelH, cartelX, cartelY, 5.5f);

        shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE);
        float brk = 38f;
        shapeRenderer.rectLine(cartelX, cartelY, cartelX + brk, cartelY, 8f);
        shapeRenderer.rectLine(cartelX, cartelY, cartelX, cartelY + brk, 8f);
        shapeRenderer.rectLine(cartelX + cartelW, cartelY, cartelX + cartelW - brk, cartelY, 8f);
        shapeRenderer.rectLine(cartelX + cartelW, cartelY, cartelX + cartelW, cartelY + brk, 8f);
        shapeRenderer.rectLine(cartelX, cartelY + cartelH, cartelX + brk, cartelY + cartelH, 8f);
        shapeRenderer.rectLine(cartelX, cartelY + cartelH, cartelX, cartelY + cartelH - brk, 8f);
        shapeRenderer.rectLine(cartelX + cartelW, cartelY + cartelH, cartelX + cartelW - brk, cartelY + cartelH, 8f);
        shapeRenderer.rectLine(cartelX + cartelW, cartelY + cartelH, cartelX + cartelW, cartelY + cartelH - brk, 8f);

        float flashArc = MathUtils.sin(tiempoTotal * 8f);
        if (flashArc > 0.6f) {
            dibujarRayoProcedural(cartelX + 30f, cartelY + cartelH / 2f, cartelX + 260f, cartelY + cartelH / 2f, 5, 18f, COLOR_CELESTE_BRILLANTE, 3.8f);
            dibujarRayoProcedural(cartelX + cartelW - 260f, cartelY + cartelH / 2f, cartelX + cartelW - 30f, cartelY + cartelH / 2f, 5, 18f, COLOR_CELESTE_BRILLANTE, 3.8f);
        }
    }

    private void dibujarCartelPrincipalShortCircuit() {
        float oscY = MathUtils.sin(tiempoTotal * 4.2f) * 14f;
        float oscX = MathUtils.cos(tiempoTotal * 2.8f) * 9f;

        float jitterX = 0f;
        float jitterY = 0f;
        if (MathUtils.sin(tiempoTotal * 17f) > 0.88f) {
            jitterX = MathUtils.random(-4.5f, 4.5f);
            jitterY = MathUtils.random(-3.5f, 3.5f);
        }

        float cartelW = 1680f;
        float cartelH = 240f;
        float cartelX = (ANCHO - cartelW) / 2f + oscX;
        float cartelY = 760f + oscY;

        String titulo = "SHORT CIRCUIT!";
        glyphLayout.setText(fontTitulo, titulo);
        float tituloX = (ANCHO - glyphLayout.width) / 2f + oscX + jitterX;
        float tituloY = cartelY + (cartelH + glyphLayout.height) / 2f + jitterY;

        for (int off = 28; off >= 4; off -= 3) {
            float f = (float) off / 28f;
            fontTitulo.setColor(
                    MathUtils.lerp(0.02f, 0.12f, f),
                    MathUtils.lerp(0.08f, 0.32f, f),
                    MathUtils.lerp(0.18f, 0.55f, f),
                    0.92f
            );
            fontTitulo.draw(batch, titulo, tituloX + off, tituloY - off);
        }

        fontTitulo.setColor(COLOR_NEON_CELESTE);
        fontTitulo.draw(batch, titulo, tituloX, tituloY);

        fontTitulo.setColor(COLOR_CELESTE_BRILLANTE);
        fontTitulo.draw(batch, titulo, tituloX - 2.5f, tituloY + 2.5f);

        float flash = MathUtils.sin(tiempoTotal * 7.5f);
        if (flash > 0.65f) {
            fontTitulo.setColor(1f, 1f, 1f, 0.98f);
            fontTitulo.draw(batch, titulo, tituloX - 3.5f, tituloY + 3.5f);
        }
    }

    private void dibujarBotonesMenu() {
        for (int i = 0; i < TEXTOS_BOTONES.length; i++) {
            float factorHover = animHover[i];
            float flotacion = MathUtils.sin(tiempoTotal * 3.8f + i * 1.6f) * 6f;
            float vibraX = (factorHover > 0.05f) ? MathUtils.sin(tiempoTotal * 35f + i) * (factorHover * 2.5f) : 0f;
            float x = BOTON_BASE_X + (factorHover * 38f) + vibraX;
            float y = BOTONES_Y[i] + flotacion;
            float w = BOTON_ANCHO;
            float h = BOTON_ALTO;

            float offsetSombra = 14f + (factorHover * 9f);
            shapeRenderer.setColor(0f, 0f, 0f, 0.88f);
            shapeRenderer.rect(x + offsetSombra, y - offsetSombra, w, h);

            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f + factorHover * 0.48f);
            shapeRenderer.rect(x + offsetSombra * 0.5f, y - offsetSombra * 0.5f, w, h);

            float rFondo = MathUtils.lerp(COLOR_CAJA_OSCURA.r, COLOR_SLATE_OSCURO.r, factorHover);
            float gFondo = MathUtils.lerp(COLOR_CAJA_OSCURA.g, 0.16f, factorHover);
            float bFondo = MathUtils.lerp(COLOR_CAJA_OSCURA.b, 0.26f, factorHover);
            shapeRenderer.setColor(rFondo, gFondo, bFondo, 0.98f);
            shapeRenderer.rect(x, y, w, h);

            Color colorBorde = (factorHover > 0.5f) ? COLOR_CELESTE_BRILLANTE : COLOR_NEON_CELESTE;
            shapeRenderer.setColor(colorBorde);
            float grosor = 3.8f + (factorHover * 2.2f);
            shapeRenderer.rectLine(x, y, x + w, y, grosor);
            shapeRenderer.rectLine(x + w, y, x + w, y + h, grosor);
            shapeRenderer.rectLine(x + w, y + h, x, y + h, grosor);
            shapeRenderer.rectLine(x, y + h, x, y, grosor);

            shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE);
            float esquina = 15f + factorHover * 8f;
            shapeRenderer.rectLine(x, y, x + esquina, y, grosor + 2f);
            shapeRenderer.rectLine(x, y, x, y + esquina, grosor + 2f);
            shapeRenderer.rectLine(x + w, y + h, x + w - esquina, y + h, grosor + 2f);
            shapeRenderer.rectLine(x + w, y + h, x + w, y + h - esquina, grosor + 2f);

            float perimetro = 2 * (w + h);
            float distRecorrida = ((tiempoTotal * 480f) + i * 180f) % perimetro;
            float pxLaser, pyLaser;
            if (distRecorrida < w) {
                pxLaser = x + distRecorrida;
                pyLaser = y;
            } else if (distRecorrida < w + h) {
                pxLaser = x + w;
                pyLaser = y + (distRecorrida - w);
            } else if (distRecorrida < 2 * w + h) {
                pxLaser = x + w - (distRecorrida - (w + h));
                pyLaser = y + h;
            } else {
                pxLaser = x;
                pyLaser = y + h - (distRecorrida - (2 * w + h));
            }

            shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
            shapeRenderer.circle(pxLaser, pyLaser, 4.2f);
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.75f);
            shapeRenderer.circle(pxLaser, pyLaser, 7.5f);

            if (factorHover > 0.05f) {
                float tamanoPuntero = 20f * factorHover;
                shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, factorHover * 0.95f);
                shapeRenderer.triangle(
                        x - 24f, y + h / 2f,
                        x - 46f, y + h / 2f + tamanoPuntero,
                        x - 46f, y + h / 2f - tamanoPuntero
                );
                shapeRenderer.setColor(1f, 1f, 1f, factorHover * 0.95f);
                shapeRenderer.triangle(
                        x - 28f, y + h / 2f,
                        x - 41f, y + h / 2f + tamanoPuntero * 0.5f,
                        x - 41f, y + h / 2f - tamanoPuntero * 0.5f
                );
            }
        }
    }

    private void dibujarTextosBotonesMenu() {
        for (int i = 0; i < TEXTOS_BOTONES.length; i++) {
            float factorHover = animHover[i];
            float flotacion = MathUtils.sin(tiempoTotal * 3.8f + i * 1.6f) * 6f;
            float vibraX = (factorHover > 0.05f) ? MathUtils.sin(tiempoTotal * 35f + i) * (factorHover * 2.5f) : 0f;
            float x = BOTON_BASE_X + (factorHover * 38f) + vibraX;
            float y = BOTONES_Y[i] + flotacion;
            float w = BOTON_ANCHO;

            String etiqueta = TEXTOS_BOTONES[i];
            if (factorHover > 0.4f) {
                etiqueta = "[►  " + etiqueta + "  ◄]";
            }

            glyphLayout.setText(fontBotones, etiqueta);
            float tx = x + (w - glyphLayout.width) / 2f;
            float ty = y + 51f;

            fontBotones.setColor(0f, 0f, 0f, 0.92f);
            fontBotones.draw(batch, etiqueta, tx + 3.5f, ty - 3.5f);

            if (factorHover > 0.45f) {
                fontBotones.setColor(COLOR_CELESTE_BRILLANTE);
            } else {
                fontBotones.setColor(COLOR_NEON_CELESTE);
            }
            fontBotones.draw(batch, etiqueta, tx, ty);
        }

        String controlHint = "[ CONTROLES: RATÓN O FLECHAS ARRIBA / ABAJO + ENTER ]";
        glyphLayout.setText(fontDetalle, controlHint);
        float hintX = (ANCHO - glyphLayout.width) / 2f;
        float pulsoHint = MathUtils.sin(tiempoTotal * 3.5f) * 0.25f + 0.75f;

        fontDetalle.setColor(0f, 0f, 0f, 0.92f);
        fontDetalle.draw(batch, controlHint, hintX + 2f, 118f);

        fontDetalle.setColor(COLOR_GRIS_CLARO.r, COLOR_GRIS_CLARO.g, COLOR_GRIS_CLARO.b, pulsoHint);
        fontDetalle.draw(batch, controlHint, hintX, 120f);
    }

    private void dibujarCajaTrabajando() {
        float cx = (ANCHO - 1000f) / 2f;
        float cy = 250f;
        float cw = 1000f;
        float ch = 570f;

        float pulsoBorde = MathUtils.sin(tiempoTotal * 5f) * 0.25f + 0.75f;

        shapeRenderer.setColor(0f, 0f, 0f, 0.92f);
        shapeRenderer.rect(cx + 20f, cy - 20f, cw, ch);

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f);
        shapeRenderer.rect(cx + 12f, cy - 12f, cw, ch);

        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(cx, cy, cw, ch);

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, pulsoBorde);
        shapeRenderer.rectLine(cx, cy, cx + cw, cy, 4.8f);
        shapeRenderer.rectLine(cx + cw, cy, cx + cw, cy + ch, 4.8f);
        shapeRenderer.rectLine(cx + cw, cy + ch, cx, cy + ch, 4.8f);
        shapeRenderer.rectLine(cx, cy + ch, cx, cy, 4.8f);

        shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.75f);
        shapeRenderer.rectLine(cx + 14f, cy + 14f, cx + cw - 14f, cy + 14f, 2.2f);
        shapeRenderer.rectLine(cx + cw - 14f, cy + 14f, cx + cw - 14f, cy + ch - 14f, 2.2f);
        shapeRenderer.rectLine(cx + cw - 14f, cy + ch - 14f, cx + 14f, cy + ch - 14f, 2.2f);
        shapeRenderer.rectLine(cx + 14f, cy + ch - 14f, cx + 14f, cy + 14f, 2.2f);

        if ("CONTINUAR".equals(seccionActual)) {
            float px = (ANCHO - 500f) / 2f + (animProbar * 24f);
            float py = 420f;
            dibujarBotonAuxiliar(px, py, 500f, 68f, animProbar, COLOR_CELESTE_BRILLANTE);
        }

        float vx = (ANCHO - 440f) / 2f + (animVolver * 24f);
        float vy = 320f;
        dibujarBotonAuxiliar(vx, vy, 440f, 68f, animVolver, COLOR_NEON_CELESTE);
    }

    private void dibujarBotonAuxiliar(float x, float y, float w, float h, float factorHover, Color acento) {
        float sombra = 12f + factorHover * 6f;
        shapeRenderer.setColor(0f, 0f, 0f, 0.88f);
        shapeRenderer.rect(x + sombra, y - sombra, w, h);

        shapeRenderer.setColor(acento.r, acento.g, acento.b, 0.28f + factorHover * 0.45f);
        shapeRenderer.rect(x + sombra * 0.5f, y - sombra * 0.5f, w, h);

        float r = MathUtils.lerp(COLOR_CAJA_OSCURA.r, COLOR_SLATE_OSCURO.r, factorHover);
        float g = MathUtils.lerp(COLOR_CAJA_OSCURA.g, 0.16f, factorHover);
        float b = MathUtils.lerp(COLOR_CAJA_OSCURA.b, 0.26f, factorHover);
        shapeRenderer.setColor(r, g, b, 0.98f);
        shapeRenderer.rect(x, y, w, h);

        shapeRenderer.setColor(factorHover > 0.5f ? Color.WHITE : acento);
        shapeRenderer.rectLine(x, y, x + w, y, 3.8f);
        shapeRenderer.rectLine(x + w, y, x + w, y + h, 3.8f);
        shapeRenderer.rectLine(x + w, y + h, x, y + h, 3.8f);
        shapeRenderer.rectLine(x, y + h, x, y, 3.8f);
    }

    private void dibujarTextosTrabajando() {
        String cartel = "¡TRABAJANDO EN ESTO!";
        glyphLayout.setText(fontBotones, cartel);
        float cx = (ANCHO - glyphLayout.width) / 2f;

        for (int off = 14; off >= 3; off -= 3) {
            fontBotones.setColor(0.04f, 0.18f, 0.28f, 0.92f);
            fontBotones.draw(batch, cartel, cx + off, 670f - off);
        }
        fontBotones.setColor(COLOR_NEON_CELESTE);
        fontBotones.draw(batch, cartel, cx, 670f);

        String mensaje = "Esta característica se encuentra actualmente en desarrollo para la siguiente entrega.";
        glyphLayout.setText(fontDetalle, mensaje);
        fontDetalle.setColor(Color.WHITE);
        fontDetalle.draw(batch, mensaje, (ANCHO - glyphLayout.width) / 2f, 570f);

        String atajo = "(Presiona ESC o haz clic abajo para regresar)";
        glyphLayout.setText(fontDetalle, atajo);
        fontDetalle.setColor(COLOR_GRIS_CLARO);
        fontDetalle.draw(batch, atajo, (ANCHO - glyphLayout.width) / 2f, 520f);

        if ("CONTINUAR".equals(seccionActual)) {
            float px = (ANCHO - 500f) / 2f + (animProbar * 24f);
            float py = 420f;
            String textoProbar = animProbar > 0.4f ? "[►  PROBAR CIRCUITO (NIVEL 1)  ◄]" : "PROBAR CIRCUITO (NIVEL 1)";
            glyphLayout.setText(fontDetalle, textoProbar);
            fontDetalle.setColor(animProbar > 0.5f ? Color.WHITE : COLOR_CELESTE_BRILLANTE);
            fontDetalle.draw(batch, textoProbar, px + (500f - glyphLayout.width) / 2f, py + 43f);
        }

        float vx = (ANCHO - 440f) / 2f + (animVolver * 24f);
        float vy = 320f;
        String textoVolver = animVolver > 0.4f ? "[►  VOLVER AL MENÚ  ◄]" : "VOLVER AL MENÚ";
        glyphLayout.setText(fontDetalle, textoVolver);
        fontDetalle.setColor(animVolver > 0.5f ? Color.WHITE : COLOR_NEON_CELESTE);
        fontDetalle.draw(batch, textoVolver, vx + (440f - glyphLayout.width) / 2f, vy + 43f);
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
        if (fontDetalle != null) {
            fontDetalle.dispose();
        }
    }

    public Game getJuego() {
        return juego;
    }

    public void setJuego(Game juego) {
        this.juego = juego;
    }
}
