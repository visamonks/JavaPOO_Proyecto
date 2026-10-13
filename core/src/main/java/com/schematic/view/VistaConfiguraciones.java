package com.schematic.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.schematic.JuegoPrincipal;
import com.schematic.constants.Constantes;
import com.schematic.model.progreso.GestorPersistenciaJSON;
import com.schematic.model.progreso.ProgresoJugador;

import java.util.ArrayList;
import java.util.List;

public class VistaConfiguraciones extends ScreenAdapter {

    private final Game juego;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout layout;

    private final OrthographicCamera camara;
    private final Viewport viewport;

    private final BitmapFont fontTitulo;
    private final BitmapFont fontSubtitulo;
    private final BitmapFont fontSeccion;
    private final BitmapFont fontDetalle;
    private final BitmapFont fontPequena;
    private final BitmapFont fontBotones;

    private final Color COLOR_FONDO = new Color(0.02f, 0.03f, 0.06f, 1f);
    private final Color COLOR_GRID = new Color(0.05f, 0.12f, 0.18f, 0.40f);
    private final Color COLOR_CARD_FONDO = new Color(0.04f, 0.07f, 0.12f, 0.95f);
    private final Color COLOR_CARD_BORDE = new Color(0.12f, 0.22f, 0.35f, 1f);
    private final Color COLOR_NEON_CYAN = new Color(0.00f, 0.90f, 1.00f, 1f);
    private final Color COLOR_CELESTE_BRILLANTE = new Color(0.65f, 0.95f, 1.00f, 1f);
    private final Color COLOR_AMBAR = new Color(1.00f, 0.78f, 0.20f, 1f);
    private final Color COLOR_VERDE = new Color(0.20f, 0.95f, 0.45f, 1f);
    private final Color COLOR_ROJO = new Color(0.95f, 0.28f, 0.28f, 1f);
    private final Color COLOR_TEXTO_MUTED = new Color(0.52f, 0.64f, 0.78f, 1f);
    private final Color COLOR_TEXTO_BLANCO = new Color(0.96f, 0.98f, 1.00f, 1f);

    private static final float ANCHO_VIRTUAL = Constantes.ANCHO_VENTANA;
    private static final float ALTO_VIRTUAL = Constantes.ALTO_VENTANA;

    private static final float CARD_ANCHO = 1040f;
    private static final float CARD_X = (ANCHO_VIRTUAL - CARD_ANCHO) / 2f;

    private static final float CARD1_Y = 620f;
    private static final float CARD1_ALTO = 210f;

    private static final float CARD2_Y = 430f;
    private static final float CARD2_ALTO = 160f;

    private static final float CARD3_Y = 240f;
    private static final float CARD3_ALTO = 160f;

    private static final float SLIDER_ANCHO = 780f;
    private static final float SLIDER_ALTO = 20f;
    private static final float SLIDER_X = CARD_X + 130f;
    private static final float SLIDER_Y = CARD1_Y + 54f;

    private float volumen = 0.8f;
    private boolean arrastrandoSlider = false;

    private boolean estadoConfirmacionBorrado = false;
    private float tiempoConfirmacionBorrado = 0f;
    private float tiempoMensajeExito = 0f;

    private float tiempoTotal = 0f;
    private float offsetGrid = 0f;
    private float tiempoChispasAmbiente = 0f;

    private float animVolver = 0f;
    private float animPantalla = 0f;
    private float animBorrar = 0f;
    private float animCancelar = 0f;

    private float electronX;
    private float electronY;
    private float targetElectronX;
    private float targetElectronY;
    private float saltoElectron = 0f;
    private float giroElectron = 0f;
    private float tiempoParpadeo = 0f;
    private boolean parpadeando = false;

    private static class ChispaParticula {
        float x, y;
        float vx, vy;
        float vida, vidaMax;
        Color color;

        ChispaParticula(float x, float y, float vx, float vy, float vidaMax, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.vida = vidaMax;
            this.vidaMax = vidaMax;
            this.color = color;
        }
    }
    private final List<ChispaParticula> chispas = new ArrayList<>();

    public VistaConfiguraciones() {
        this((Game) Gdx.app.getApplicationListener());
    }

    public VistaConfiguraciones(Game juego) {
        this.juego = juego;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.layout = new GlyphLayout();

        this.camara = new OrthographicCamera();
        this.viewport = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
        this.viewport.apply();
        this.camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);
        this.camara.update();

        this.fontTitulo = crearFuente(3.6f);
        this.fontSubtitulo = crearFuente(1.25f);
        this.fontSeccion = crearFuente(1.45f);
        this.fontDetalle = crearFuente(0.98f);
        this.fontPequena = crearFuente(0.85f);
        this.fontBotones = crearFuente(1.30f);

        cargarPreferencias();

        this.electronX = SLIDER_X + (volumen * SLIDER_ANCHO);
        this.electronY = SLIDER_Y + 54f;
        this.targetElectronX = this.electronX;
        this.targetElectronY = this.electronY;
    }

    private BitmapFont crearFuente(float escala) {
        BitmapFont f = new BitmapFont();
        f.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        f.getData().setScale(escala);
        return f;
    }

    private void cargarPreferencias() {
        try {
            Preferences prefs = Gdx.app.getPreferences("schematic_config");
            this.volumen = prefs.getFloat("volumen", 0.8f);
        } catch (Exception e) {
            this.volumen = 0.8f;
        }
    }

    private void guardarPreferencias() {
        try {
            Preferences prefs = Gdx.app.getPreferences("schematic_config");
            prefs.putFloat("volumen", volumen);
            prefs.flush();
        } catch (Exception ignored) {
        }
    }

    public static float getVolumenGuardado() {
        try {
            Preferences prefs = Gdx.app.getPreferences("schematic_config");
            return prefs.getFloat("volumen", 0.8f);
        } catch (Exception e) {
            return 0.8f;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) return false;

                Vector3 m = viewport.unproject(new Vector3(screenX, screenY, 0));
                float mx = m.x;
                float my = m.y;

                float distElectron = Vector2.dst(mx, my, electronX, electronY);
                if (distElectron <= 60f) {
                    acariciarElectron();
                    return true;
                }

                if (estaDentro(mx, my, SLIDER_X - 30f, SLIDER_Y - 25f, SLIDER_ANCHO + 60f, SLIDER_ALTO + 50f)) {
                    arrastrandoSlider = true;
                    actualizarVolumenPorPosicion(mx);
                    return true;
                }

                float btnPantallaW = 340f;
                float btnPantallaH = 54f;
                float btnPantallaX = CARD_X + CARD_ANCHO - btnPantallaW - 40f;
                float btnPantallaY = CARD2_Y + 53f;
                if (estaDentro(mx, my, btnPantallaX, btnPantallaY, btnPantallaW, btnPantallaH)) {
                    alternarPantallaCompleta();
                    return true;
                }

                if (!estadoConfirmacionBorrado) {
                    float btnBorrarW = 340f;
                    float btnBorrarH = 54f;
                    float btnBorrarX = CARD_X + CARD_ANCHO - btnBorrarW - 40f;
                    float btnBorrarY = CARD3_Y + 53f;
                    if (estaDentro(mx, my, btnBorrarX, btnBorrarY, btnBorrarW, btnBorrarH)) {
                        solicitarConfirmacionBorrado();
                        return true;
                    }
                } else {
                    float btnConfW = 200f;
                    float btnCancW = 130f;
                    float btnConfX = CARD_X + CARD_ANCHO - btnConfW - btnCancW - 50f;
                    float btnCancX = CARD_X + CARD_ANCHO - btnCancW - 40f;
                    float btnY = CARD3_Y + 53f;

                    if (estaDentro(mx, my, btnConfX, btnY, btnConfW, 54f)) {
                        ejecutarBorradoDatos();
                        return true;
                    }
                    if (estaDentro(mx, my, btnCancX, btnY, btnCancW, 54f)) {
                        cancelarBorradoDatos();
                        return true;
                    }
                }

                float volverW = 280f;
                float volverH = 64f;
                float volverX = 70f;
                float volverY = 60f;
                if (estaDentro(mx, my, volverX, volverY, volverW, volverH)) {
                    volverAlMenu();
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (arrastrandoSlider) {
                    Vector3 m = viewport.unproject(new Vector3(screenX, screenY, 0));
                    actualizarVolumenPorPosicion(m.x);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    arrastrandoSlider = false;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                    volverAlMenu();
                    return true;
                }
                if (keycode == Input.Keys.F || keycode == Input.Keys.F11) {
                    alternarPantallaCompleta();
                    return true;
                }
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
                    modificarVolumenPaso(-0.05f);
                    return true;
                }
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
                    modificarVolumenPaso(0.05f);
                    return true;
                }
                return false;
            }
        });
    }

    private void actualizarVolumenPorPosicion(float mx) {
        float nuevo = MathUtils.clamp((mx - SLIDER_X) / SLIDER_ANCHO, 0.0f, 1.0f);
        if (Math.abs(nuevo - volumen) > 0.01f) {
            volumen = nuevo;
            guardarPreferencias();
            saltoElectron = 6f;
            generarChispas(SLIDER_X + volumen * SLIDER_ANCHO, SLIDER_Y + SLIDER_ALTO / 2f, 4, COLOR_NEON_CYAN);
        }
    }

    private void modificarVolumenPaso(float deltaVol) {
        volumen = MathUtils.clamp(volumen + deltaVol, 0.0f, 1.0f);
        guardarPreferencias();
        saltoElectron = 8f;
        generarChispas(SLIDER_X + volumen * SLIDER_ANCHO, SLIDER_Y + SLIDER_ALTO / 2f, 6, COLOR_CELESTE_BRILLANTE);
    }

    private void alternarPantallaCompleta() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(Constantes.ANCHO_VENTANA, Constantes.ALTO_VENTANA);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
        saltoElectron = 14f;
        generarChispas(CARD_X + CARD_ANCHO - 210f, CARD2_Y + 80f, 16, COLOR_VERDE);
    }

    private void solicitarConfirmacionBorrado() {
        estadoConfirmacionBorrado = true;
        tiempoConfirmacionBorrado = 5.0f;
        saltoElectron = 12f;
        generarChispas(CARD_X + CARD_ANCHO - 210f, CARD3_Y + 80f, 14, COLOR_ROJO);
    }

    private void cancelarBorradoDatos() {
        estadoConfirmacionBorrado = false;
        tiempoConfirmacionBorrado = 0f;
    }

    private void ejecutarBorradoDatos() {
        ProgresoJugador nuevo = new ProgresoJugador(1);
        GestorPersistenciaJSON.guardar(nuevo);
        if (juego instanceof JuegoPrincipal) {
            ((JuegoPrincipal) juego).setProgreso(nuevo);
        }
        estadoConfirmacionBorrado = false;
        tiempoConfirmacionBorrado = 0f;
        tiempoMensajeExito = 3.5f;
        saltoElectron = 24f;
        generarChispas(CARD_X + CARD_ANCHO - 210f, CARD3_Y + 80f, 25, COLOR_VERDE);
    }

    private void acariciarElectron() {
        saltoElectron = 22f;
        giroElectron = 360f;
        generarChispas(electronX, electronY, 16, COLOR_CELESTE_BRILLANTE);
    }

    private void volverAlMenu() {
        if (juego != null) {
            juego.setScreen(new VistaMenu(juego));
        }
    }

    private boolean estaDentro(float px, float py, float bx, float by, float bw, float bh) {
        return px >= bx && px <= bx + bw && py >= by && py <= by + bh;
    }

    private void generarChispas(float x, float y, int cantidad, Color col) {
        for (int i = 0; i < cantidad; i++) {
            float angulo = MathUtils.random(0f, MathUtils.PI2);
            float vel = MathUtils.random(60f, 220f);
            float vx = MathUtils.cos(angulo) * vel;
            float vy = MathUtils.sin(angulo) * vel;
            float vida = MathUtils.random(0.35f, 0.75f);
            chispas.add(new ChispaParticula(x, y, vx, vy, vida, col));
        }
    }

    @Override
    public void render(float delta) {
        tiempoTotal += delta;
        offsetGrid = (offsetGrid + delta * 35f) % 60f;

        viewport.apply();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Vector3 raton = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float mx = raton.x;
        float my = raton.y;

        actualizarAnimaciones(delta, mx, my);

        Gdx.gl.glClearColor(COLOR_FONDO.r, COLOR_FONDO.g, COLOR_FONDO.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarFondoGrid();
        dibujarRayosDecorativos();
        dibujarTarjetasShapes(mx, my);
        dibujarSliderShape(mx, my);
        dibujarBotonesShapes(mx, my);
        dibujarChispas();
        dibujarElectronMascota(mx, my);
        shapeRenderer.end();

        batch.begin();
        dibujarEncabezado();
        dibujarTextosTarjetas();
        dibujarTextosBotones();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void actualizarAnimaciones(float delta, float mx, float my) {
        if (saltoElectron > 0f) {
            saltoElectron = Math.max(0f, saltoElectron - delta * 45f);
        }
        if (giroElectron > 0f) {
            giroElectron = Math.max(0f, giroElectron - delta * 720f);
        }

        tiempoParpadeo += delta;
        if (tiempoParpadeo > 3.4f) {
            parpadeando = (tiempoParpadeo < 3.55f);
            if (tiempoParpadeo > 3.6f) {
                tiempoParpadeo = 0f;
            }
        } else {
            parpadeando = false;
        }

        if (estadoConfirmacionBorrado) {
            tiempoConfirmacionBorrado -= delta;
            if (tiempoConfirmacionBorrado <= 0f) {
                estadoConfirmacionBorrado = false;
            }
        }

        if (tiempoMensajeExito > 0f) {
            tiempoMensajeExito -= delta;
        }

        float flotacionMascota = MathUtils.sin(tiempoTotal * 4.5f) * 6f;
        targetElectronX = SLIDER_X + (volumen * SLIDER_ANCHO);
        targetElectronY = SLIDER_Y + 54f + flotacionMascota + saltoElectron;

        electronX = MathUtils.lerp(electronX, targetElectronX, delta * 12f);
        electronY = MathUtils.lerp(electronY, targetElectronY, delta * 12f);

        boolean hoverVolver = estaDentro(mx, my, 70f, 60f, 280f, 64f);
        animVolver = MathUtils.lerp(animVolver, hoverVolver ? 1f : 0f, delta * 9f);

        float btnPantallaW = 340f;
        float btnPantallaH = 54f;
        float btnPantallaX = CARD_X + CARD_ANCHO - btnPantallaW - 40f;
        float btnPantallaY = CARD2_Y + 53f;
        boolean hoverPantalla = estaDentro(mx, my, btnPantallaX, btnPantallaY, btnPantallaW, btnPantallaH);
        animPantalla = MathUtils.lerp(animPantalla, hoverPantalla ? 1f : 0f, delta * 9f);

        if (!estadoConfirmacionBorrado) {
            float btnBorrarW = 340f;
            float btnBorrarH = 54f;
            float btnBorrarX = CARD_X + CARD_ANCHO - btnBorrarW - 40f;
            float btnBorrarY = CARD3_Y + 53f;
            boolean hoverBorrar = estaDentro(mx, my, btnBorrarX, btnBorrarY, btnBorrarW, btnBorrarH);
            animBorrar = MathUtils.lerp(animBorrar, hoverBorrar ? 1f : 0f, delta * 9f);
        } else {
            float btnConfW = 200f;
            float btnCancW = 130f;
            float btnConfX = CARD_X + CARD_ANCHO - btnConfW - btnCancW - 50f;
            float btnCancX = CARD_X + CARD_ANCHO - btnCancW - 40f;
            float btnY = CARD3_Y + 53f;
            boolean hoverConf = estaDentro(mx, my, btnConfX, btnY, btnConfW, 54f);
            boolean hoverCanc = estaDentro(mx, my, btnCancX, btnY, btnCancW, 54f);
            animBorrar = MathUtils.lerp(animBorrar, hoverConf ? 1f : 0f, delta * 9f);
            animCancelar = MathUtils.lerp(animCancelar, hoverCanc ? 1f : 0f, delta * 9f);
        }

        tiempoChispasAmbiente += delta;
        if (tiempoChispasAmbiente > 0.14f) {
            tiempoChispasAmbiente = 0f;
            float sx = MathUtils.random(60f, ANCHO_VIRTUAL - 60f);
            float sy = MathUtils.random(60f, ALTO_VIRTUAL - 60f);
            chispas.add(new ChispaParticula(sx, sy, MathUtils.random(-15f, 15f), MathUtils.random(20f, 60f), MathUtils.random(0.7f, 1.6f), COLOR_NEON_CYAN));
        }

        for (int i = chispas.size() - 1; i >= 0; i--) {
            ChispaParticula p = chispas.get(i);
            p.vida -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            if (p.vida <= 0f) {
                chispas.remove(i);
            }
        }
    }

    private void dibujarFondoGrid() {
        shapeRenderer.setColor(COLOR_FONDO);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        shapeRenderer.setColor(COLOR_GRID);
        for (float x = 0; x <= ANCHO_VIRTUAL; x += 60f) {
            shapeRenderer.rectLine(x, 0, x, ALTO_VIRTUAL, 1.2f);
        }
        for (float y = offsetGrid; y <= ALTO_VIRTUAL; y += 60f) {
            shapeRenderer.rectLine(0, y, ANCHO_VIRTUAL, y, 1.2f);
        }

        shapeRenderer.setColor(0.00f, 0.90f, 1.00f, 0.22f);
        shapeRenderer.rectLine(30f, 30f, ANCHO_VIRTUAL - 30f, 30f, 2f);
        shapeRenderer.rectLine(30f, ALTO_VIRTUAL - 30f, ANCHO_VIRTUAL - 30f, ALTO_VIRTUAL - 30f, 2f);
        shapeRenderer.rectLine(30f, 30f, 30f, ALTO_VIRTUAL - 30f, 2f);
        shapeRenderer.rectLine(ANCHO_VIRTUAL - 30f, 30f, ANCHO_VIRTUAL - 30f, ALTO_VIRTUAL - 30f, 2f);

        dibujarCruzTecnologica(30f, 30f);
        dibujarCruzTecnologica(ANCHO_VIRTUAL - 30f, 30f);
        dibujarCruzTecnologica(30f, ALTO_VIRTUAL - 30f);
        dibujarCruzTecnologica(ANCHO_VIRTUAL - 30f, ALTO_VIRTUAL - 30f);
    }

    private void dibujarCruzTecnologica(float cx, float cy) {
        shapeRenderer.setColor(COLOR_NEON_CYAN.r, COLOR_NEON_CYAN.g, COLOR_NEON_CYAN.b, 0.5f);
        shapeRenderer.rectLine(cx - 15f, cy, cx + 15f, cy, 2f);
        shapeRenderer.rectLine(cx, cy - 15f, cx, cy + 15f, 2f);
    }

    private void dibujarRayosDecorativos() {
        float yConductor1 = CARD1_Y + CARD1_ALTO + 16f;
        shapeRenderer.setColor(0.08f, 0.18f, 0.28f, 0.75f);
        shapeRenderer.rectLine(CARD_X - 30f, yConductor1, CARD_X + CARD_ANCHO + 30f, yConductor1, 4f);

        float pulsoX = CARD_X - 30f + ((tiempoTotal * 340f) % (CARD_ANCHO + 60f));
        shapeRenderer.setColor(COLOR_NEON_CYAN.r, COLOR_NEON_CYAN.g, COLOR_NEON_CYAN.b, 0.9f);
        shapeRenderer.rectLine(pulsoX - 25f, yConductor1, pulsoX + 25f, yConductor1, 3f);
        shapeRenderer.circle(pulsoX, yConductor1, 4.5f);

        float yConductor2 = CARD3_Y - 20f;
        shapeRenderer.setColor(0.08f, 0.18f, 0.28f, 0.75f);
        shapeRenderer.rectLine(CARD_X - 30f, yConductor2, CARD_X + CARD_ANCHO + 30f, yConductor2, 4f);

        float pulsoX2 = CARD_X + CARD_ANCHO + 30f - ((tiempoTotal * 280f) % (CARD_ANCHO + 60f));
        shapeRenderer.setColor(COLOR_AMBAR.r, COLOR_AMBAR.g, COLOR_AMBAR.b, 0.85f);
        shapeRenderer.rectLine(pulsoX2 - 25f, yConductor2, pulsoX2 + 25f, yConductor2, 3f);
        shapeRenderer.circle(pulsoX2, yConductor2, 4.5f);
    }

    private void dibujarTarjetasShapes(float mx, float my) {
        dibujarBaseTarjeta(CARD_X, CARD1_Y, CARD_ANCHO, CARD1_ALTO, COLOR_NEON_CYAN);
        dibujarBaseTarjeta(CARD_X, CARD2_Y, CARD_ANCHO, CARD2_ALTO, COLOR_CELESTE_BRILLANTE);
        Color colCard3 = estadoConfirmacionBorrado ? COLOR_ROJO : COLOR_AMBAR;
        dibujarBaseTarjeta(CARD_X, CARD3_Y, CARD_ANCHO, CARD3_ALTO, colCard3);
    }

    private void dibujarBaseTarjeta(float x, float y, float w, float h, Color colAura) {
        shapeRenderer.setColor(colAura.r, colAura.g, colAura.b, 0.08f);
        shapeRenderer.rect(x - 5f, y - 5f, w + 10f, h + 10f);

        shapeRenderer.setColor(COLOR_CARD_FONDO);
        shapeRenderer.rect(x, y, w, h);

        shapeRenderer.setColor(colAura.r, colAura.g, colAura.b, 0.40f);
        dibujarRectContorno(x, y, w, h, 2.0f);

        shapeRenderer.setColor(colAura.r, colAura.g, colAura.b, 0.85f);
        shapeRenderer.rectLine(x, y + h, x + 30f, y + h, 3.5f);
        shapeRenderer.rectLine(x, y + h, x, y + h - 30f, 3.5f);
        shapeRenderer.rectLine(x + w, y + h, x + w - 30f, y + h, 3.5f);
        shapeRenderer.rectLine(x + w, y + h, x + w, y + h - 30f, 3.5f);
        shapeRenderer.rectLine(x, y, x + 30f, y, 3.5f);
        shapeRenderer.rectLine(x, y, x, y + 30f, 3.5f);
        shapeRenderer.rectLine(x + w, y, x + w - 30f, y, 3.5f);
        shapeRenderer.rectLine(x + w, y, x + w, y + 30f, 3.5f);
    }

    private void dibujarRectContorno(float rx, float ry, float rw, float rh, float grosor) {
        shapeRenderer.rectLine(rx, ry, rx + rw, ry, grosor);
        shapeRenderer.rectLine(rx + rw, ry, rx + rw, ry + rh, grosor);
        shapeRenderer.rectLine(rx + rw, ry + rh, rx, ry + rh, grosor);
        shapeRenderer.rectLine(rx, ry + rh, rx, ry, grosor);
    }

    private void dibujarSliderShape(float mx, float my) {
        shapeRenderer.setColor(0.015f, 0.035f, 0.07f, 1f);
        shapeRenderer.rect(SLIDER_X, SLIDER_Y, SLIDER_ANCHO, SLIDER_ALTO);

        shapeRenderer.setColor(COLOR_CARD_BORDE);
        dibujarRectContorno(SLIDER_X, SLIDER_Y, SLIDER_ANCHO, SLIDER_ALTO, 1.8f);

        float fillW = SLIDER_ANCHO * volumen;
        if (fillW > 0f) {
            shapeRenderer.setColor(COLOR_NEON_CYAN.r, COLOR_NEON_CYAN.g, COLOR_NEON_CYAN.b, 0.85f);
            shapeRenderer.rect(SLIDER_X, SLIDER_Y, fillW, SLIDER_ALTO);

            shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE.r, COLOR_CELESTE_BRILLANTE.g, COLOR_CELESTE_BRILLANTE.b, 0.95f);
            shapeRenderer.rect(SLIDER_X, SLIDER_Y + SLIDER_ALTO - 4f, fillW, 4f);
        }

        for (int i = 0; i <= 10; i++) {
            float tickX = SLIDER_X + (i * (SLIDER_ANCHO / 10f));
            shapeRenderer.setColor(0.30f, 0.42f, 0.55f, 0.65f);
            shapeRenderer.rectLine(tickX, SLIDER_Y - 8f, tickX, SLIDER_Y - 2f, 2f);
        }

        float thumbX = SLIDER_X + fillW;
        shapeRenderer.setColor(COLOR_NEON_CYAN.r, COLOR_NEON_CYAN.g, COLOR_NEON_CYAN.b, 0.35f);
        shapeRenderer.circle(thumbX, SLIDER_Y + SLIDER_ALTO / 2f, 16f);

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(thumbX, SLIDER_Y + SLIDER_ALTO / 2f, 8f);
    }

    private void dibujarBotonesShapes(float mx, float my) {
        float btnPantallaW = 340f;
        float btnPantallaH = 54f;
        float btnPantallaX = CARD_X + CARD_ANCHO - btnPantallaW - 40f;
        float btnPantallaY = CARD2_Y + 53f;

        boolean esFull = Gdx.graphics.isFullscreen();
        Color colFondoPantalla = esFull ? new Color(0.06f, 0.22f, 0.14f, 0.92f) : new Color(0.04f, 0.08f, 0.14f, 0.92f);
        shapeRenderer.setColor(colFondoPantalla);
        shapeRenderer.rect(btnPantallaX, btnPantallaY, btnPantallaW, btnPantallaH);

        Color colBordePantalla = esFull ? COLOR_VERDE : ((animPantalla > 0.05f) ? COLOR_NEON_CYAN : COLOR_CARD_BORDE);
        shapeRenderer.setColor(colBordePantalla);
        dibujarRectContorno(btnPantallaX, btnPantallaY, btnPantallaW, btnPantallaH, (animPantalla > 0.05f) ? 3.0f : 2.0f);

        float switchW = 60f;
        float switchH = 28f;
        float switchX = btnPantallaX + btnPantallaW - switchW - 16f;
        float switchY = btnPantallaY + (btnPantallaH - switchH) / 2f;
        shapeRenderer.setColor(0.08f, 0.12f, 0.18f, 1f);
        shapeRenderer.rect(switchX, switchY, switchW, switchH);

        float pillX = esFull ? (switchX + switchW - 18f) : (switchX + 6f);
        shapeRenderer.setColor(esFull ? COLOR_VERDE : COLOR_TEXTO_MUTED);
        shapeRenderer.circle(pillX + 6f, switchY + switchH / 2f, 10f);

        if (!estadoConfirmacionBorrado) {
            float btnBorrarW = 340f;
            float btnBorrarH = 54f;
            float btnBorrarX = CARD_X + CARD_ANCHO - btnBorrarW - 40f;
            float btnBorrarY = CARD3_Y + 53f;

            shapeRenderer.setColor(0.12f, 0.04f, 0.06f, 0.92f);
            shapeRenderer.rect(btnBorrarX, btnBorrarY, btnBorrarW, btnBorrarH);

            Color colBordeBorrar = (animBorrar > 0.05f) ? COLOR_ROJO : new Color(0.55f, 0.20f, 0.20f, 0.85f);
            shapeRenderer.setColor(colBordeBorrar);
            dibujarRectContorno(btnBorrarX, btnBorrarY, btnBorrarW, btnBorrarH, (animBorrar > 0.05f) ? 3.0f : 2.0f);
        } else {
            float btnConfW = 200f;
            float btnCancW = 130f;
            float btnConfX = CARD_X + CARD_ANCHO - btnConfW - btnCancW - 50f;
            float btnCancX = CARD_X + CARD_ANCHO - btnCancW - 40f;
            float btnY = CARD3_Y + 53f;

            float pulsoAlerta = 0.7f + MathUtils.sin(tiempoTotal * 12f) * 0.3f;
            shapeRenderer.setColor(0.28f * pulsoAlerta, 0.04f, 0.06f, 0.95f);
            shapeRenderer.rect(btnConfX, btnY, btnConfW, 54f);
            shapeRenderer.setColor(COLOR_ROJO);
            dibujarRectContorno(btnConfX, btnY, btnConfW, 54f, 3.0f);

            shapeRenderer.setColor(0.04f, 0.08f, 0.14f, 0.92f);
            shapeRenderer.rect(btnCancX, btnY, btnCancW, 54f);
            Color colBordeCanc = (animCancelar > 0.05f) ? COLOR_NEON_CYAN : COLOR_CARD_BORDE;
            shapeRenderer.setColor(colBordeCanc);
            dibujarRectContorno(btnCancX, btnY, btnCancW, 54f, 2.0f);
        }

        float vx = 70f;
        float vy = 60f;
        float vw = 280f;
        float vh = 64f;
        shapeRenderer.setColor(0.04f, 0.08f, 0.14f, 0.92f);
        shapeRenderer.rect(vx, vy, vw, vh);

        Color colBordeVolver = (animVolver > 0.05f) ? COLOR_NEON_CYAN : COLOR_CARD_BORDE;
        shapeRenderer.setColor(colBordeVolver);
        dibujarRectContorno(vx, vy, vw, vh, (animVolver > 0.05f) ? 3.0f : 1.8f);
    }

    private void dibujarElectronMascota(float mx, float my) {
        float ex = electronX;
        float ey = electronY;
        float escala = 3.2f;

        float pulso = 1f + MathUtils.sin(tiempoTotal * 5f) * 0.08f;
        shapeRenderer.setColor(0.00f, 0.85f, 1.00f, 0.22f);
        shapeRenderer.circle(ex, ey, 25f * escala * pulso);

        shapeRenderer.setColor(0.00f, 0.95f, 1.00f, 0.85f);
        shapeRenderer.circle(ex, ey, 16f * escala);

        shapeRenderer.setColor(0.75f, 1.00f, 1.00f, 1f);
        shapeRenderer.circle(ex, ey, 11f * escala);

        shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
        shapeRenderer.circle(ex - 4f * escala, ey + 4f * escala, 3.5f * escala);

        shapeRenderer.setColor(1.0f, 0.45f, 0.65f, 0.45f);
        shapeRenderer.circle(ex - 8.5f * escala, ey - 2.5f * escala, 2.8f * escala);
        shapeRenderer.circle(ex + 8.5f * escala, ey - 2.5f * escala, 2.8f * escala);

        float ojoDistX = 5.2f * escala;
        float ojoDistY = 3.2f * escala;

        float vecX = mx - ex;
        float vecY = my - ey;
        float anguloMira = MathUtils.atan2(vecY, vecX);
        float lookX = MathUtils.cos(anguloMira) * 1.6f * escala;
        float lookY = MathUtils.sin(anguloMira) * 1.6f * escala;

        if (parpadeando) {
            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.rectLine(ex - ojoDistX - 4f * escala, ey + ojoDistY, ex - ojoDistX + 4f * escala, ey + ojoDistY, 3.2f);
            shapeRenderer.rectLine(ex + ojoDistX - 4f * escala, ey + ojoDistY, ex + ojoDistX + 4f * escala, ey + ojoDistY, 3.2f);
        } else if (saltoElectron > 6f || arrastrandoSlider) {
            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.rectLine(ex - ojoDistX - 4f * escala, ey + ojoDistY - 1.5f * escala, ex - ojoDistX, ey + ojoDistY + 3.0f * escala, 3.2f);
            shapeRenderer.rectLine(ex - ojoDistX, ey + ojoDistY + 3.0f * escala, ex - ojoDistX + 4f * escala, ey + ojoDistY - 1.5f * escala, 3.2f);
            shapeRenderer.rectLine(ex + ojoDistX - 4f * escala, ey + ojoDistY - 1.5f * escala, ex + ojoDistX, ey + ojoDistY + 3.0f * escala, 3.2f);
            shapeRenderer.rectLine(ex + ojoDistX, ey + ojoDistY + 3.0f * escala, ex + ojoDistX + 4f * escala, ey + ojoDistY - 1.5f * escala, 3.2f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(ex - ojoDistX, ey + ojoDistY, 5.2f * escala);
            shapeRenderer.circle(ex + ojoDistX, ey + ojoDistY, 5.2f * escala);

            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.circle(ex - ojoDistX + lookX, ey + ojoDistY + lookY, 2.7f * escala);
            shapeRenderer.circle(ex + ojoDistX + lookX, ey + ojoDistY + lookY, 2.7f * escala);

            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(ex - ojoDistX + lookX + 0.9f * escala, ey + ojoDistY + lookY + 0.9f * escala, 1.1f * escala);
            shapeRenderer.circle(ex + ojoDistX + lookX + 0.9f * escala, ey + ojoDistY + lookY + 0.9f * escala, 1.1f * escala);
        }

        shapeRenderer.setColor(0.05f, 0.08f, 0.18f, 1f);
        float myCenterY = ey - 5.5f * escala;
        shapeRenderer.rectLine(ex - 3.2f * escala, myCenterY + 1.2f * escala, ex - 1.2f * escala, myCenterY - 0.8f * escala, 2.4f);
        shapeRenderer.rectLine(ex - 1.2f * escala, myCenterY - 0.8f * escala, ex + 1.2f * escala, myCenterY - 0.8f * escala, 2.4f);
        shapeRenderer.rectLine(ex + 1.2f * escala, myCenterY - 0.8f * escala, ex + 3.2f * escala, myCenterY + 1.2f * escala, 2.4f);

        float estela1 = MathUtils.sin(tiempoTotal * 6.5f) * 3.5f * escala;
        float estela2 = MathUtils.cos(tiempoTotal * 5.8f) * 3.5f * escala;
        shapeRenderer.setColor(0.20f, 0.90f, 1.00f, 0.50f);
        shapeRenderer.circle(ex - 18f * escala - estela1, ey - 14f * escala + estela2, 2.8f * escala);
        shapeRenderer.circle(ex + 18f * escala + estela2, ey - 14f * escala - estela1, 2.2f * escala);

        float orbitaR1 = 26f * escala;
        float orbitaR2 = 33f * escala;
        float ang1 = tiempoTotal * 4.2f;
        float ang2 = -tiempoTotal * 3.6f + 1.8f;

        float ox1 = ex + MathUtils.cos(ang1) * orbitaR1;
        float oy1 = ey + MathUtils.sin(ang1) * (orbitaR1 * 0.40f);
        shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE);
        shapeRenderer.circle(ox1, oy1, 3.6f);

        float ox2 = ex + MathUtils.cos(ang2) * orbitaR2;
        float oy2 = ey + MathUtils.sin(ang2) * (orbitaR2 * 0.45f);
        shapeRenderer.setColor(COLOR_AMBAR);
        shapeRenderer.circle(ox2, oy2, 3.0f);
    }

    private void dibujarChispas() {
        for (ChispaParticula p : chispas) {
            float alfa = p.vida / p.vidaMax;
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, alfa);
            shapeRenderer.circle(p.x, p.y, 3.5f * alfa + 1.2f);
        }
    }

    private void dibujarEncabezado() {
        fontTitulo.setColor(COLOR_NEON_CYAN);
        layout.setText(fontTitulo, "CONFIGURACION");
        float tituloX = (ANCHO_VIRTUAL - layout.width) / 2f;
        fontTitulo.draw(batch, "CONFIGURACION", tituloX, ALTO_VIRTUAL - 75f);

        fontSubtitulo.setColor(COLOR_TEXTO_MUTED);
        String subTexto = "Ajustes del Sistema y Preferencias - Schematic";
        layout.setText(fontSubtitulo, subTexto);
        float subX = (ANCHO_VIRTUAL - layout.width) / 2f;
        fontSubtitulo.draw(batch, subTexto, subX, ALTO_VIRTUAL - 135f);
    }

    private void dibujarTextosTarjetas() {
        fontSeccion.setColor(COLOR_TEXTO_BLANCO);
        fontSeccion.draw(batch, "VOLUMEN GENERAL", CARD_X + 40f, CARD1_Y + CARD1_ALTO - 34f);

        String txtPorcentaje = (int) (volumen * 100f) + "%";
        fontSeccion.setColor(COLOR_NEON_CYAN);
        layout.setText(fontSeccion, txtPorcentaje);
        fontSeccion.draw(batch, txtPorcentaje, CARD_X + CARD_ANCHO - layout.width - 40f, CARD1_Y + CARD1_ALTO - 34f);

        fontDetalle.setColor(COLOR_TEXTO_MUTED);
        fontDetalle.draw(batch, "Regula el nivel sonoro y la retroalimentacion electrodinamica del simulador.", CARD_X + 40f, CARD1_Y + CARD1_ALTO - 72f);

        fontPequena.setColor(COLOR_TEXTO_MUTED);
        fontPequena.draw(batch, "0%", SLIDER_X - 35f, SLIDER_Y + 16f);
        fontPequena.draw(batch, "100%", SLIDER_X + SLIDER_ANCHO + 12f, SLIDER_Y + 16f);

        fontSeccion.setColor(COLOR_TEXTO_BLANCO);
        fontSeccion.draw(batch, "PANTALLA COMPLETA", CARD_X + 40f, CARD2_Y + CARD2_ALTO - 34f);

        fontDetalle.setColor(COLOR_TEXTO_MUTED);
        String descPantalla = Gdx.graphics.isFullscreen() ? "Modo pantalla completa activo. Resolucion optimizada para tu monitor." : "Modo ventana activo a 1920x1080. Cambia a pantalla completa para maxima inmersion.";
        fontDetalle.draw(batch, descPantalla, CARD_X + 40f, CARD2_Y + CARD2_ALTO - 72f, 580f, Align.left, true);

        fontSeccion.setColor(COLOR_TEXTO_BLANCO);
        fontSeccion.draw(batch, "GESTION DE PROGRESO", CARD_X + 40f, CARD3_Y + CARD3_ALTO - 34f);

        if (tiempoMensajeExito > 0f) {
            fontDetalle.setColor(COLOR_VERDE);
            fontDetalle.draw(batch, "DATOS REINICIADOS: El progreso se ha restablecido exitosamente al Nivel 1.", CARD_X + 40f, CARD3_Y + CARD3_ALTO - 72f, 580f, Align.left, true);
        } else if (estadoConfirmacionBorrado) {
            fontDetalle.setColor(COLOR_ROJO);
            String segsRestantes = String.format("%.1f", Math.max(0f, tiempoConfirmacionBorrado));
            fontDetalle.draw(batch, "ADVERTENCIA: Esta accion bloqueara los niveles superiores. (" + segsRestantes + "s)", CARD_X + 40f, CARD3_Y + CARD3_ALTO - 72f, 580f, Align.left, true);
        } else {
            fontDetalle.setColor(COLOR_TEXTO_MUTED);
            fontDetalle.draw(batch, "Reinicia todo el avance guardado y bloquea los diagramas para comenzar desde cero.", CARD_X + 40f, CARD3_Y + CARD3_ALTO - 72f, 580f, Align.left, true);
        }
    }

    private void dibujarTextosBotones() {
        boolean esFull = Gdx.graphics.isFullscreen();
        float btnPantallaW = 340f;
        float btnPantallaX = CARD_X + CARD_ANCHO - btnPantallaW - 40f;
        float btnPantallaY = CARD2_Y + 53f;
        String txtPantalla = esFull ? "ACTIVADA" : "DESACTIVADA";
        layout.setText(fontBotones, txtPantalla);
        fontBotones.setColor(esFull ? COLOR_VERDE : COLOR_TEXTO_MUTED);
        fontBotones.draw(batch, txtPantalla, btnPantallaX + 24f, btnPantallaY + 36f);

        if (!estadoConfirmacionBorrado) {
            float btnBorrarW = 340f;
            float btnBorrarX = CARD_X + CARD_ANCHO - btnBorrarW - 40f;
            float btnBorrarY = CARD3_Y + 53f;
            String txtBorrar = "BORRAR DATOS";
            layout.setText(fontBotones, txtBorrar);
            fontBotones.setColor((animBorrar > 0.05f) ? COLOR_ROJO : new Color(0.85f, 0.40f, 0.40f, 1f));
            fontBotones.draw(batch, txtBorrar, btnBorrarX + (btnBorrarW - layout.width) / 2f, btnBorrarY + 36f);
        } else {
            float btnConfW = 200f;
            float btnCancW = 130f;
            float btnConfX = CARD_X + CARD_ANCHO - btnConfW - btnCancW - 50f;
            float btnCancX = CARD_X + CARD_ANCHO - btnCancW - 40f;
            float btnY = CARD3_Y + 53f;

            String txtConf = "SI, BORRAR";
            layout.setText(fontBotones, txtConf);
            fontBotones.setColor(Color.WHITE);
            fontBotones.draw(batch, txtConf, btnConfX + (btnConfW - layout.width) / 2f, btnY + 36f);

            String txtCanc = "CANCELAR";
            layout.setText(fontBotones, txtCanc);
            fontBotones.setColor((animCancelar > 0.05f) ? COLOR_NEON_CYAN : COLOR_TEXTO_MUTED);
            fontBotones.draw(batch, txtCanc, btnCancX + (btnCancW - layout.width) / 2f, btnY + 36f);
        }

        layout.setText(fontBotones, "VOLVER");
        fontBotones.setColor((animVolver > 0.05f) ? COLOR_NEON_CYAN : COLOR_TEXTO_BLANCO);
        fontBotones.draw(batch, "VOLVER", 70f + (280f - layout.width) / 2f, 60f + 42f);

        fontPequena.setColor(new Color(0.40f, 0.50f, 0.65f, 0.8f));
        String ayuda = "[ESC] Volver   |   [F] Pantalla Completa   |   [A / D o Flechas] Volumen   |   [Clic en Electron] Interactuar";
        layout.setText(fontPequena, ayuda);
        fontPequena.draw(batch, ayuda, (ANCHO_VIRTUAL - layout.width) / 2f, 28f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);
        camara.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        fontTitulo.dispose();
        fontSubtitulo.dispose();
        fontSeccion.dispose();
        fontDetalle.dispose();
        fontPequena.dispose();
        fontBotones.dispose();
    }
}

