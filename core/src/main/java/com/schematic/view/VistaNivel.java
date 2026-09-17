package com.schematic.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.schematic.controller.ControladorNivel;
import com.schematic.model.*;

import java.util.ArrayList;
import java.util.List;

public class VistaNivel extends ScreenAdapter {

    public static final float ANCHO_VIRTUAL = 1920f;
    public static final float ALTO_VIRTUAL = 1080f;

    private Game juego;
    private Minijuego minijuego;
    private OrthographicCamera camara;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont fontTitulo;
    private BitmapFont fontPequena;
    private GlyphLayout glyphLayout;
    private ControladorNivel controlador;

    private List<Texture> texturasCargadas;
    private Animation<TextureRegion> animLedNormal;
    private Animation<TextureRegion> animLedPrendiendo;
    private Animation<TextureRegion> animLedMuriendo;
    private Animation<TextureRegion> animResistenciaNormal;
    private Animation<TextureRegion> animResistenciaFallo;

    private float tiempoTotal;
    private float tiempoAnimError;
    private float tiempoIntro;
    private boolean introCompletada;
    private float voltajeMedidorDC;
    private float tiempoExitoAcumulado;
    private float offsetGridPerspectiva;
    private float tiempoIntroNivel;

    private static final int FASE_DERROTA_NINGUNA = 0;
    private static final int FASE_DERROTA_ALARMA = 1;
    private static final int FASE_DERROTA_EXPLOSION = 2;
    private static final int FASE_DERROTA_LIMPIEZA = 3;
    private static final int FASE_DERROTA_MODAL = 4;

    private int faseDerrota = FASE_DERROTA_NINGUNA;
    private float tiempoFaseDerrota = 0f;
    private float sacudidaCamaraX = 0f;
    private float sacudidaCamaraY = 0f;
    private float opacidadHollin = 0f;
    private float posicionLimpiadorX = -150f;
    private Vector2 centroExplosion = new Vector2(950f, 500f);

    private boolean cableEnProgreso;
    private Terminal terminalOrigenCable;
    private Vector2 ratonMundo;
    private Terminal terminalBajoRaton;

    private List<ParticulaChispa> particulas;

    private final Color COLOR_FONDO_NEGRO = new Color(0.02f, 0.03f, 0.06f, 1f);
    private final Color COLOR_SLATE_OSCURO = new Color(0.06f, 0.09f, 0.14f, 1f);
    private final Color COLOR_GRIS_METAL = new Color(0.30f, 0.38f, 0.48f, 1f);
    private final Color COLOR_GRIS_CLARO = new Color(0.65f, 0.76f, 0.88f, 1f);
    private final Color COLOR_NEON_CELESTE = new Color(0.0f, 0.88f, 1.0f, 1f);
    private final Color COLOR_CELESTE_BRILLANTE = new Color(0.60f, 0.95f, 1.0f, 1f);
    private final Color COLOR_CAJA_OSCURA = new Color(0.05f, 0.07f, 0.11f, 0.96f);

    private static class ParticulaChispa {
        float x, y, vx, vy, vida, vidaMax, r, g, b;
        ParticulaChispa(float x, float y, float vx, float vy, float vidaMax, float r, float g, float b) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.vida = vidaMax; this.vidaMax = vidaMax;
            this.r = r; this.g = g; this.b = b;
        }
    }

    public VistaNivel(Minijuego minijuego) {
        this(minijuego, null);
    }

    public VistaNivel(Minijuego minijuego, Game juego) {
        this.minijuego = minijuego;
        this.juego = juego;

        this.camara = new OrthographicCamera();
        this.viewport = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
        this.viewport.apply();
        this.camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);
        this.camara.update();

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();

        this.font = new BitmapFont();
        this.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.font.getData().setScale(1.25f);

        this.fontTitulo = new BitmapFont();
        this.fontTitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontTitulo.getData().setScale(1.75f);

        this.fontPequena = new BitmapFont();
        this.fontPequena.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontPequena.getData().setScale(0.95f);

        this.texturasCargadas = new ArrayList<>();
        cargarAnimaciones();

        this.tiempoTotal = 0f;
        this.tiempoAnimError = 0f;
        this.tiempoIntro = 0f;
        this.introCompletada = false;
        this.voltajeMedidorDC = 0f;
        this.tiempoExitoAcumulado = 0f;
        this.offsetGridPerspectiva = 0f;
        this.tiempoIntroNivel = 0f;

        this.cableEnProgreso = false;
        this.terminalOrigenCable = null;
        this.ratonMundo = new Vector2();
        this.terminalBajoRaton = null;

        this.particulas = new ArrayList<>();

        this.controlador = new ControladorNivel(minijuego, camara, viewport, this);
    }

    private void cargarAnimaciones() {
        animLedNormal = cargarAnimacion("led_normal", 10, 0.12f);
        animLedPrendiendo = cargarAnimacion("led_prendiendo", 8, 0.10f);
        animLedMuriendo = cargarAnimacion("led_muriendo", 10, 0.14f);
        animResistenciaNormal = cargarAnimacion("resistencia_normal", 8, 0.22f);
        animResistenciaFallo = cargarAnimacion("resistencia_fail", 15, 0.15f);
    }

    private Animation<TextureRegion> cargarAnimacion(String carpeta, int cantidadFrames, float duracionFrame) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < cantidadFrames; i++) {
            String path = String.format("sprites/%s/frame_%05d.png", carpeta, i);
            FileHandle fh = Gdx.files.internal(path);
            if (fh.exists()) {
                Texture tex = new Texture(fh);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                texturasCargadas.add(tex);
                frames.add(new TextureRegion(tex));
            }
        }
        if (frames.size == 0) {
            Texture texFallback = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            texturasCargadas.add(texFallback);
            frames.add(new TextureRegion(texFallback));
        }
        return new Animation<>(duracionFrame, frames);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(controlador);
    }

    @Override
    public void render(float delta) {
        tiempoTotal += delta;
        offsetGridPerspectiva = (offsetGridPerspectiva + delta * 50f) % 70f;

        if (!introCompletada) {
            tiempoIntro += delta;
            voltajeMedidorDC = Math.min(5.0f, (tiempoIntro / 2.2f) * 5.0f);
            if (tiempoIntro >= 2.6f) {
                introCompletada = true;
                voltajeMedidorDC = 5.0f;
                generarChispasImpacto(310f, 530f, 40);
            }
        }

        if (minijuego != null) {
            minijuego.actualizar(delta);

            if (minijuego.estaGanado()) {
                tiempoExitoAcumulado += delta;
                if (MathUtils.randomBoolean(0.25f)) {
                    for (Componente c : minijuego.getComponentes()) {
                        if (c instanceof LED) {
                            generarChispasImpacto(c.getPosicionX() + c.getAncho() * 0.49f, c.getPosicionY() + c.getAlto() * 0.65f, 4);
                        }
                    }
                }
            } else {
                tiempoExitoAcumulado = 0f;
            }

            boolean hayError = false;
            for (Componente c : minijuego.getComponentes()) {
                if ("ERROR".equals(c.getEstadoActual())) {
                    hayError = true;
                    break;
                }
            }
            if (hayError) {
                if (faseDerrota == FASE_DERROTA_NINGUNA) {
                    faseDerrota = FASE_DERROTA_ALARMA;
                    tiempoFaseDerrota = 0f;
                    localizarCentroFallo();
                }
            }
        }

        if (faseDerrota != FASE_DERROTA_NINGUNA) {
            actualizarFaseDerrota(delta);
        } else {
            sacudidaCamaraX = 0f;
            sacudidaCamaraY = 0f;
        }

        actualizarParticulas(delta);

        camara.position.set(ANCHO_VIRTUAL / 2f + sacudidaCamaraX, ALTO_VIRTUAL / 2f + sacudidaCamaraY, 0);
        camara.update();

        viewport.apply();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Gdx.gl.glClearColor(COLOR_FONDO_NEGRO.r, COLOR_FONDO_NEGRO.g, COLOR_FONDO_NEGRO.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarMesaDeTrabajo();
        dibujarFuentePoder();
        dibujarBarraLateralAlmacen();
        dibujarCablesConectadosRectos();
        if (cableEnProgreso && terminalOrigenCable != null) {
            dibujarCableArrastreRecto();
        }
        dibujarTerminalesFisicos();
        dibujarElectronesIntro();
        dibujarParticulas();
        shapeRenderer.end();

        batch.begin();
        dibujarComponentesSprites();
        dibujarContenidoBarraLateral();
        dibujarTextosHUD();
        if (controlador != null && controlador.estaArrastrandoDesdeBarra()) {
            dibujarVistaPreviaArrastre();
        }
        batch.end();

        dibujarEfectosDerrota();

        if (minijuego != null && ((minijuego.estaGanado() && tiempoExitoAcumulado >= 3.0f) || faseDerrota == FASE_DERROTA_MODAL)) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dibujarFondoModal();
            shapeRenderer.end();

            batch.begin();
            dibujarContenidoModal();
            batch.end();
        }

        tiempoIntroNivel += delta;
        if (tiempoIntroNivel < 0.5f) {
            float alfa = MathUtils.clamp(1f - (tiempoIntroNivel / 0.5f), 0f, 1f);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(COLOR_FONDO_NEGRO.r, COLOR_FONDO_NEGRO.g, COLOR_FONDO_NEGRO.b, alfa);
            shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);
            float barraY = ALTO_VIRTUAL * (1f - alfa);
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alfa * 0.85f);
            shapeRenderer.rectLine(0, barraY, ANCHO_VIRTUAL, barraY, 4f);
            shapeRenderer.end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private boolean hayErrorLED() {
        if (minijuego == null) return false;
        for (Componente c : minijuego.getComponentes()) {
            if (c instanceof LED && ((LED) c).estaQuemado()) {
                return true;
            }
        }
        return false;
    }

    private void localizarCentroFallo() {
        if (minijuego == null) return;
        for (Componente c : minijuego.getComponentes()) {
            if ("ERROR".equals(c.getEstadoActual())) {
                centroExplosion.set(c.getPosicionX() + c.getAncho() / 2f, c.getPosicionY() + c.getAlto() / 2f);
                return;
            }
        }
    }

    private void actualizarFaseDerrota(float delta) {
        tiempoFaseDerrota += delta;
        tiempoAnimError += delta;

        if (faseDerrota == FASE_DERROTA_ALARMA) {
            sacudidaCamaraX = MathUtils.random(-3.0f, 3.0f);
            sacudidaCamaraY = MathUtils.random(-3.0f, 3.0f);
            if (MathUtils.randomBoolean(0.4f)) {
                generarChispasImpacto(centroExplosion.x, centroExplosion.y, 4);
            }
            if (tiempoFaseDerrota >= 1.0f) {
                faseDerrota = FASE_DERROTA_EXPLOSION;
                tiempoFaseDerrota = 0f;
                generarGranExplosion(centroExplosion.x, centroExplosion.y);
            }
        } else if (faseDerrota == FASE_DERROTA_EXPLOSION) {
            float progreso = MathUtils.clamp(tiempoFaseDerrota / 1.1f, 0f, 1f);
            float fuerza = (1f - progreso) * 44f;
            sacudidaCamaraX = MathUtils.random(-fuerza, fuerza);
            sacudidaCamaraY = MathUtils.random(-fuerza, fuerza);
            opacidadHollin = Math.min(1.0f, progreso * 1.8f);

            if (MathUtils.randomBoolean(0.45f)) {
                generarChispasImpacto(centroExplosion.x + MathUtils.random(-80f, 80f), centroExplosion.y + MathUtils.random(-80f, 80f), 5);
            }

            if (tiempoFaseDerrota >= 1.1f) {
                faseDerrota = FASE_DERROTA_LIMPIEZA;
                tiempoFaseDerrota = 0f;
                sacudidaCamaraX = 0f;
                sacudidaCamaraY = 0f;
                posicionLimpiadorX = -140f;
            }
        } else if (faseDerrota == FASE_DERROTA_LIMPIEZA) {
            sacudidaCamaraX = 0f;
            sacudidaCamaraY = 0f;
            float duracionLimpieza = 2.2f;
            float progreso = MathUtils.clamp(tiempoFaseDerrota / duracionLimpieza, 0f, 1f);
            posicionLimpiadorX = MathUtils.lerp(-140f, ANCHO_VIRTUAL + 180f, progreso);

            if (MathUtils.randomBoolean(0.4f) && posicionLimpiadorX > 0 && posicionLimpiadorX < ANCHO_VIRTUAL) {
                float yParticula = 540f + MathUtils.sin(tiempoFaseDerrota * 6.5f) * 230f;
                generarChispasImpacto(posicionLimpiadorX - 25f, yParticula, 3);
            }

            if (tiempoFaseDerrota >= duracionLimpieza) {
                faseDerrota = FASE_DERROTA_MODAL;
                tiempoFaseDerrota = 0f;
                opacidadHollin = 0f;
            }
        }
    }

    public void generarGranExplosion(float cx, float cy) {
        for (int i = 0; i < 75; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float vel = MathUtils.random(100f, 550f);
            float vx = MathUtils.cos(ang) * vel;
            float vy = MathUtils.sin(ang) * vel;
            float vida = MathUtils.random(0.5f, 1.4f);
            float r = MathUtils.random(0.9f, 1f);
            float g = MathUtils.random(0.2f, 0.8f);
            float b = MathUtils.random(0.0f, 0.2f);
            particulas.add(new ParticulaChispa(cx, cy, vx, vy, vida, r, g, b));
        }
        for (int i = 0; i < 35; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float vel = MathUtils.random(40f, 240f);
            float vx = MathUtils.cos(ang) * vel;
            float vy = MathUtils.sin(ang) * vel;
            float vida = MathUtils.random(0.8f, 2.0f);
            particulas.add(new ParticulaChispa(cx, cy, vx, vy, vida, 0.12f, 0.12f, 0.16f));
        }
    }

    private void dibujarEfectosDerrota() {
        if (faseDerrota == FASE_DERROTA_NINGUNA) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (faseDerrota == FASE_DERROTA_ALARMA) {
            float pulso = 0.22f + 0.18f * MathUtils.sin(tiempoFaseDerrota * 18f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.15f, 0.15f, pulso);
            shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, 24f);
            shapeRenderer.rect(0, ALTO_VIRTUAL - 24f, ANCHO_VIRTUAL, 24f);
            shapeRenderer.rect(0, 0, 24f, ALTO_VIRTUAL);
            shapeRenderer.rect(ANCHO_VIRTUAL - 24f, 0, 24f, ALTO_VIRTUAL);
            shapeRenderer.end();
        } else if (faseDerrota == FASE_DERROTA_EXPLOSION) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (tiempoFaseDerrota < 0.15f) {
                float alfaFlash = (1f - (tiempoFaseDerrota / 0.15f)) * 0.98f;
                shapeRenderer.setColor(1f, 0.95f, 0.85f, alfaFlash);
                shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);
            }

            float onda = tiempoFaseDerrota * 950f;
            float alfaOnda = Math.max(0f, 1f - (tiempoFaseDerrota / 0.75f));
            shapeRenderer.setColor(1f, 0.50f, 0.10f, alfaOnda * 0.55f);
            shapeRenderer.circle(centroExplosion.x, centroExplosion.y, onda);

            float alfaNegroPuro = MathUtils.clamp(opacidadHollin, 0f, 1f);
            shapeRenderer.setColor(0.00f, 0.00f, 0.00f, alfaNegroPuro);
            shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

            if (alfaNegroPuro < 0.95f) {
                float radioHollin = Math.min(1200f, tiempoFaseDerrota * 1100f);
                shapeRenderer.setColor(0.00f, 0.00f, 0.00f, 0.95f);
                shapeRenderer.circle(centroExplosion.x, centroExplosion.y, radioHollin);
                shapeRenderer.circle(centroExplosion.x - 180f, centroExplosion.y + 80f, radioHollin * 0.8f);
                shapeRenderer.circle(centroExplosion.x + 200f, centroExplosion.y - 70f, radioHollin * 0.85f);
            }
            shapeRenderer.end();
        } else if (faseDerrota == FASE_DERROTA_LIMPIEZA) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (posicionLimpiadorX < ANCHO_VIRTUAL) {
                float xInicio = Math.max(0f, posicionLimpiadorX);
                float anchoRestante = ANCHO_VIRTUAL - xInicio;
                shapeRenderer.setColor(0.00f, 0.00f, 0.00f, 1.0f);
                shapeRenderer.rect(xInicio, 0, anchoRestante, ALTO_VIRTUAL);
            }

            shapeRenderer.setColor(0.00f, 0.95f, 1.00f, 0.35f);
            shapeRenderer.rectLine(posicionLimpiadorX, 0, posicionLimpiadorX, ALTO_VIRTUAL, 16f);
            shapeRenderer.setColor(0.25f, 0.95f, 1.00f, 0.85f);
            shapeRenderer.rectLine(posicionLimpiadorX, 0, posicionLimpiadorX, ALTO_VIRTUAL, 7f);
            shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            shapeRenderer.rectLine(posicionLimpiadorX, 0, posicionLimpiadorX, ALTO_VIRTUAL, 2.5f);

            for (int i = 0; i < 7; i++) {
                float gotaY = ((tiempoFaseDerrota * 450f + i * 160f) % ALTO_VIRTUAL);
                shapeRenderer.setColor(0.6f, 0.95f, 1f, 0.75f);
                shapeRenderer.circle(posicionLimpiadorX - 4f, gotaY, 3.5f);
            }
            shapeRenderer.end();

            float yElectron = 540f + MathUtils.sin(tiempoFaseDerrota * 6.5f) * 230f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dibujarElectronLimpiador(posicionLimpiadorX, yElectron, tiempoFaseDerrota);
            shapeRenderer.end();
        }
    }

    private void dibujarElectronLimpiador(float ex, float ey, float tiempo) {
        float escala = 3.4f;

        shapeRenderer.setColor(0.0f, 0.88f, 1.0f, 0.40f);
        shapeRenderer.circle(ex, ey, 25f * escala);

        shapeRenderer.setColor(0.0f, 0.95f, 1.0f, 0.95f);
        shapeRenderer.circle(ex, ey, 18f * escala);

        shapeRenderer.setColor(0.80f, 1.00f, 1.00f, 1f);
        shapeRenderer.circle(ex, ey, 12.5f * escala);

        shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
        shapeRenderer.circle(ex - 4.5f * escala, ey + 4.5f * escala, 4.2f * escala);

        shapeRenderer.setColor(1.0f, 0.45f, 0.65f, 0.45f);
        shapeRenderer.circle(ex - 8f * escala, ey - 2f * escala, 3f * escala);
        shapeRenderer.circle(ex + 8f * escala, ey - 2f * escala, 3f * escala);

        float oscilacionBrazo = MathUtils.sin(tiempo * 16f) * 12f;
        float bx1 = ex + (10f * escala);
        float by1 = ey - (16f * escala) + oscilacionBrazo;
        float bx2 = bx1;
        float by2 = by1 + (38f * escala);

        shapeRenderer.setColor(0.0f, 0.90f, 1.0f, 0.95f);
        shapeRenderer.rectLine(bx1, by1 - 35f, bx2, by2 + 35f, 7f);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(bx1, by1 - 35f, bx2, by2 + 35f, 2.5f);

        shapeRenderer.setColor(0.15f, 0.25f, 0.40f, 1f);
        shapeRenderer.rectLine(ex + 4f * escala, ey - 2f * escala, bx1, by1 + 18f * escala, 4f);

        boolean parpadeo = (tiempo % 1.3f) > 1.15f;
        float ojoDistX = 5.5f * escala;
        float ojoDistY = 3.5f * escala;

        if (parpadeo) {
            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.rectLine(ex - ojoDistX - 5f, ey + ojoDistY, ex - ojoDistX + 5f, ey + ojoDistY, 3.5f);
            shapeRenderer.rectLine(ex + ojoDistX - 5f, ey + ojoDistY, ex + ojoDistX + 5f, ey + ojoDistY, 3.5f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(ex - ojoDistX, ey + ojoDistY, 5.8f * escala);
            shapeRenderer.circle(ex + ojoDistX, ey + ojoDistY, 5.8f * escala);

            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.circle(ex - ojoDistX + 1.8f * escala, ey + ojoDistY, 3.0f * escala);
            shapeRenderer.circle(ex + ojoDistX + 1.8f * escala, ey + ojoDistY, 3.0f * escala);

            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(ex - ojoDistX + 2.2f * escala, ey + ojoDistY + 1.2f * escala, 1.2f * escala);
            shapeRenderer.circle(ex + ojoDistX + 2.2f * escala, ey + ojoDistY + 1.2f * escala, 1.2f * escala);
        }

        shapeRenderer.setColor(0.05f, 0.10f, 0.20f, 1f);
        shapeRenderer.ellipse(ex - 2f * escala, ey - 7f * escala, 5f * escala, 3f * escala);
        shapeRenderer.setColor(1.00f, 0.45f, 0.65f, 1f);
        shapeRenderer.circle(ex + 0.5f * escala, ey - 7.5f * escala, 1.5f * escala);
    }

    private void dibujarMesaDeTrabajo() {
        for (float y = 60f; y < ALTO_VIRTUAL - 90f; y += 35f) {
            float t = (y - 60f) / (ALTO_VIRTUAL - 150f);
            float r = MathUtils.lerp(0.015f, 0.038f, t);
            float g = MathUtils.lerp(0.024f, 0.052f, t);
            float b = MathUtils.lerp(0.045f, 0.100f, t);
            shapeRenderer.setColor(r, g, b, 1f);
            shapeRenderer.rect(0, y, 1620f, 36f);
        }

        shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.08f);
        for (float x = 0; x <= 1620f; x += 60f) {
            shapeRenderer.rectLine(x, 60f, x, ALTO_VIRTUAL - 90f, 1.0f);
        }
        for (float y = 60f; y <= ALTO_VIRTUAL - 90f; y += 60f) {
            shapeRenderer.rectLine(0, y, 1620f, y, 1.0f);
        }

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.12f);
        for (float x = 0; x <= 1620f; x += 300f) {
            shapeRenderer.rectLine(x, 60f, x, ALTO_VIRTUAL - 90f, 1.5f);
        }
        for (float y = 60f; y <= ALTO_VIRTUAL - 90f; y += 300f) {
            shapeRenderer.rectLine(0, y, 1620f, y, 1.5f);
        }

        float[] pistasY = {240f, 500f, 780f};
        for (int i = 0; i < pistasY.length; i++) {
            float py = pistasY[i];
            shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.14f);
            shapeRenderer.rectLine(0, py, 1620f, py, 1.5f);

            float velocidadX = 140f + (i * 35f);
            float pulsoX = ((tiempoTotal * velocidadX) + i * 450f) % (1620f + 200f) - 100f;
            if (pulsoX >= -40f && pulsoX <= 1660f) {
                shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f);
                shapeRenderer.rectLine(Math.max(0, pulsoX - 35f), py, Math.min(1620f, pulsoX + 35f), py, 2.5f);
                shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE.r, COLOR_CELESTE_BRILLANTE.g, COLOR_CELESTE_BRILLANTE.b, 0.55f);
                if (pulsoX >= 0 && pulsoX <= 1620f) {
                    shapeRenderer.circle(pulsoX, py, 2.5f);
                }
            }
        }

        dibujarOndaOsciloscopioMesa();

        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(0, ALTO_VIRTUAL - 90f, ANCHO_VIRTUAL, 90f);
        shapeRenderer.setColor(COLOR_NEON_CELESTE);
        shapeRenderer.rectLine(0, ALTO_VIRTUAL - 90f, ANCHO_VIRTUAL, ALTO_VIRTUAL - 90f, 3.8f);

        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, 60f);
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.75f);
        shapeRenderer.rectLine(0, 60f, ANCHO_VIRTUAL, 60f, 2.5f);
    }

    private void dibujarOndaOsciloscopioMesa() {
        float centroY = 220f;
        float paso = 20f;
        float prevX = 0f;
        float prevY = centroY + MathUtils.sin(tiempoTotal * 2.5f) * 12f;

        for (float x = paso; x <= 1620f; x += paso) {
            float onda1 = MathUtils.sin(x * 0.006f + tiempoTotal * 2.2f) * 16f;
            float y = centroY + onda1;

            float alpha = 0.08f + MathUtils.sin(tiempoTotal * 2f + x * 0.005f) * 0.03f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alpha);
            shapeRenderer.rectLine(prevX, prevY, x, y, 2.0f);

            prevX = x;
            prevY = y;
        }
    }

    private void dibujarBarraLateralAlmacen() {
        float bx = 1620f;
        float by = 60f;
        float bw = 300f;
        float bh = ALTO_VIRTUAL - 150f;

        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(bx, by, bw, bh);

        shapeRenderer.setColor(COLOR_NEON_CELESTE);
        shapeRenderer.rectLine(bx, by, bx, by + bh, 3.8f);

        float anchoTarjeta = 250f;
        float altoTarjeta = 200f;

        float c1x = 1645f;
        float c1y = 690f;
        boolean puedeUsarLed = (minijuego != null && minijuego.buscarCasilla("LED") != null && minijuego.buscarCasilla("LED").puedeUsar());
        shapeRenderer.setColor(puedeUsarLed ? COLOR_SLATE_OSCURO : COLOR_CAJA_OSCURA);
        shapeRenderer.rect(c1x, c1y, anchoTarjeta, altoTarjeta);
        shapeRenderer.setColor(puedeUsarLed ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
        shapeRenderer.rectLine(c1x, c1y, c1x + anchoTarjeta, c1y, 2.5f);
        shapeRenderer.rectLine(c1x + anchoTarjeta, c1y, c1x + anchoTarjeta, c1y + altoTarjeta, 2.5f);
        shapeRenderer.rectLine(c1x + anchoTarjeta, c1y + altoTarjeta, c1x, c1y + altoTarjeta, 2.5f);
        shapeRenderer.rectLine(c1x, c1y + altoTarjeta, c1x, c1y, 2.5f);

        float c2x = 1645f;
        float c2y = 450f;
        boolean puedeUsarRes = (minijuego != null && minijuego.buscarCasilla("RESISTENCIA_220") != null && minijuego.buscarCasilla("RESISTENCIA_220").puedeUsar());
        shapeRenderer.setColor(puedeUsarRes ? COLOR_SLATE_OSCURO : COLOR_CAJA_OSCURA);
        shapeRenderer.rect(c2x, c2y, anchoTarjeta, altoTarjeta);
        shapeRenderer.setColor(puedeUsarRes ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
        shapeRenderer.rectLine(c2x, c2y, c2x + anchoTarjeta, c2y, 2.5f);
        shapeRenderer.rectLine(c2x + anchoTarjeta, c2y, c2x + anchoTarjeta, c2y + altoTarjeta, 2.5f);
        shapeRenderer.rectLine(c2x + anchoTarjeta, c2y + altoTarjeta, c2x, c2y + altoTarjeta, 2.5f);
        shapeRenderer.rectLine(c2x, c2y + altoTarjeta, c2x, c2y, 2.5f);

        shapeRenderer.setColor(COLOR_FONDO_NEGRO);
        shapeRenderer.rect(c1x + anchoTarjeta - 55f, c1y + 8f, 48f, 32f);
        shapeRenderer.rect(c2x + anchoTarjeta - 55f, c2y + 8f, 48f, 32f);
    }

    private void dibujarContenidoBarraLateral() {
        fontTitulo.setColor(0.00f, 0.85f, 1.00f, 1f);
        glyphLayout.setText(fontTitulo, "COMPONENTES");
        fontTitulo.draw(batch, glyphLayout, 1620f + (300f - glyphLayout.width) / 2f, 950f);

        Minijuego.CasillaInventario casillaLed = (minijuego != null) ? minijuego.buscarCasilla("LED") : null;
        Minijuego.CasillaInventario casillaRes = (minijuego != null) ? minijuego.buscarCasilla("RESISTENCIA_220") : null;

        int cantLed = (casillaLed != null) ? casillaLed.getCantidadDisponible() : 0;
        int cantRes = (casillaRes != null) ? casillaRes.getCantidadDisponible() : 0;

        TextureRegion frameLed = animLedNormal.getKeyFrame(tiempoTotal, true);
        if (cantLed == 0) batch.setColor(0.5f, 0.5f, 0.5f, 0.45f);
        batch.draw(frameLed, 1700f, 725f, 140f, 140f);
        batch.setColor(Color.WHITE);

        font.setColor(cantLed > 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "LED", 1660f, 875f);
        fontTitulo.setColor(cantLed > 0 ? new Color(0.2f, 0.95f, 0.4f, 1f) : Color.DARK_GRAY);
        fontTitulo.draw(batch, "x" + cantLed, 1645f + 250f - 50f, 725f);

        TextureRegion frameRes = animResistenciaNormal.getKeyFrame(tiempoTotal, true);
        if (cantRes == 0) batch.setColor(0.5f, 0.5f, 0.5f, 0.45f);
        batch.draw(frameRes, 1680f, 495f, 180f, 115f);
        batch.setColor(Color.WHITE);

        font.setColor(cantRes > 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "220 Ω", 1660f, 635f);
        fontTitulo.setColor(cantRes > 0 ? new Color(0.2f, 0.95f, 0.4f, 1f) : Color.DARK_GRAY);
        fontTitulo.draw(batch, "x" + cantRes, 1645f + 250f - 50f, 485f);
    }

    private void dibujarVistaPreviaArrastre() {
        if (controlador == null || !controlador.estaArrastrandoDesdeBarra()) return;
        String tipo = controlador.getTipoComponenteArrastrado();
        Vector2 pos = controlador.getPosicionArrastre();

        batch.setColor(1f, 1f, 1f, 0.85f);
        if ("LED".equals(tipo)) {
            TextureRegion frame = animLedNormal.getKeyFrame(tiempoTotal, true);
            batch.draw(frame, pos.x - 170f, pos.y - 170f, 340f, 340f);
        } else if ("RESISTENCIA_220".equals(tipo)) {
            TextureRegion frame = animResistenciaNormal.getKeyFrame(tiempoTotal, true);
            batch.draw(frame, pos.x - 180f, pos.y - 115f, 360f, 230f);
        }
        batch.setColor(Color.WHITE);
    }

    private void dibujarFuentePoder() {
        FuenteAlimentacion fuente = null;
        if (minijuego != null) {
            for (Componente c : minijuego.getComponentes()) {
                if (c instanceof FuenteAlimentacion) {
                    fuente = (FuenteAlimentacion) c;
                    break;
                }
            }
        }
        if (fuente == null) return;

        float fx = fuente.getPosicionX();
        float fy = fuente.getPosicionY();
        float fw = fuente.getAncho();
        float fh = fuente.getAlto();

        shapeRenderer.setColor(0.12f, 0.14f, 0.18f, 1f);
        shapeRenderer.rect(fx - 140f, fy + fh * 0.5f - 14f, 140f, 28f);
        shapeRenderer.setColor(0.05f, 0.06f, 0.08f, 1f);
        shapeRenderer.rectLine(fx - 140f, fy + fh * 0.5f + 14f, fx, fy + fh * 0.5f + 14f, 2f);
        shapeRenderer.rectLine(fx - 140f, fy + fh * 0.5f - 14f, fx, fy + fh * 0.5f - 14f, 2f);

        shapeRenderer.setColor(0.09f, 0.11f, 0.15f, 1f);
        shapeRenderer.rect(fx - 8f, fy - 8f, fw + 16f, fh + 16f);

        shapeRenderer.setColor(0.16f, 0.19f, 0.25f, 1f);
        shapeRenderer.rect(fx, fy, fw, fh);

        shapeRenderer.setColor(0.28f, 0.35f, 0.46f, 1f);
        shapeRenderer.rectLine(fx, fy, fx + fw, fy, 3f);
        shapeRenderer.rectLine(fx + fw, fy, fx + fw, fy + fh, 3f);
        shapeRenderer.rectLine(fx + fw, fy + fh, fx, fy + fh, 3f);
        shapeRenderer.rectLine(fx, fy + fh, fx, fy, 3f);

        shapeRenderer.setColor(0.08f, 0.10f, 0.14f, 1f);
        shapeRenderer.rect(fx + 20f, fy + fh - 130f, fw - 40f, 95f);

        float rLed = 0.2f, gLed = 0.8f, bLed = 0.2f;
        if (!introCompletada) {
            rLed = 0.8f; gLed = 0.7f; bLed = 0.1f;
        }
        shapeRenderer.setColor(rLed, gLed, bLed, 0.35f);
        shapeRenderer.circle(fx + 38f, fy + fh - 165f, 16f);
        shapeRenderer.setColor(rLed, gLed, bLed, 1f);
        shapeRenderer.circle(fx + 38f, fy + fh - 165f, 9f);
    }

    private void dibujarElectronesIntro() {
        if (introCompletada) return;

        float yCable = 530f;
        float xFin = 80f;

        for (int i = 0; i < 6; i++) {
            float retraso = i * 0.35f;
            if (tiempoIntro > retraso) {
                float progreso = (tiempoIntro - retraso) / 1.1f;
                if (progreso < 1.0f) {
                    float x = MathUtils.lerp(-120f, xFin, progreso);
                    float y = yCable + MathUtils.sin(x * 0.05f + tiempoTotal * 12f) * 10f;
                    dibujarElectronCaricatura(x, y, 0f, 1.15f, tiempoTotal + i);
                } else if (progreso >= 1.0f && progreso <= 1.05f) {
                    generarChispasImpacto(xFin, yCable, 3);
                }
            }
        }
    }

    private void dibujarElectronCaricatura(float x, float y, float angulo, float escala, float tiempoAnim) {
        shapeRenderer.setColor(0.00f, 0.85f, 1.00f, 0.30f);
        shapeRenderer.circle(x, y, 22f * escala);

        shapeRenderer.setColor(0.00f, 0.95f, 1.00f, 0.85f);
        shapeRenderer.circle(x, y, 16f * escala);

        shapeRenderer.setColor(0.75f, 1.00f, 1.00f, 1f);
        shapeRenderer.circle(x, y, 11f * escala);

        shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
        shapeRenderer.circle(x - 4f * escala, y + 4f * escala, 3.5f * escala);

        float dirX = MathUtils.cos(angulo);
        float dirY = MathUtils.sin(angulo);
        float ojoDistX = 5f * escala;
        float ojoDistY = 3f * escala;

        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.circle(x - ojoDistX, y + ojoDistY, 5f * escala);
        shapeRenderer.circle(x + ojoDistX, y + ojoDistY, 5f * escala);

        shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
        shapeRenderer.circle(x - ojoDistX + dirX * 1.5f * escala, y + ojoDistY + dirY * 1.5f * escala, 2.5f * escala);
        shapeRenderer.circle(x + ojoDistX + dirX * 1.5f * escala, y + ojoDistY + dirY * 1.5f * escala, 2.5f * escala);

        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.circle(x - ojoDistX + 1.2f * escala, y + ojoDistY + 1.2f * escala, 1f * escala);
        shapeRenderer.circle(x + ojoDistX + 1.2f * escala, y + ojoDistY + 1.2f * escala, 1f * escala);

        shapeRenderer.setColor(0.10f, 0.15f, 0.30f, 1f);
        shapeRenderer.ellipse(x - 3f * escala, y - 6f * escala, 6f * escala, 4f * escala);
        shapeRenderer.setColor(1.00f, 0.40f, 0.60f, 1f);
        shapeRenderer.circle(x, y - 6.5f * escala, 1.6f * escala);

        float estela1 = MathUtils.sin(tiempoAnim * 20f) * 4f * escala;
        float estela2 = MathUtils.cos(tiempoAnim * 18f) * 4f * escala;
        shapeRenderer.setColor(0.20f, 0.90f, 1.00f, 0.6f);
        shapeRenderer.circle(x - dirX * 20f * escala - dirY * estela1, y - dirY * 20f * escala + dirX * estela1, 3.5f * escala);
        shapeRenderer.circle(x - dirX * 30f * escala - dirY * estela2, y - dirY * 30f * escala + dirX * estela2, 2.2f * escala);
    }

    private void dibujarCablesConectadosRectos() {
        if (minijuego == null) return;

        Color colFondo = new Color(0.08f, 0.10f, 0.14f, 1f);
        Color colActivo = new Color(0.00f, 0.95f, 1.00f, 1f);
        Color colActivoBrillante = new Color(1f, 1f, 1f, 0.92f);
        Color colInactivo = new Color(0.95f, 0.60f, 0.15f, 1f);

        for (Cable cable : minijuego.getCables()) {
            Terminal t1 = cable.getTerminalOrigen();
            Terminal t2 = cable.getTerminalDestino();
            if (t1 == null || t2 == null) continue;

            List<Vector2> ruta = calcularRutaOrtogonal(cable, t1, t2);
            boolean tieneEnergia = cable.isTieneEnergia();

            if (tieneEnergia) {
                dibujarLineaCableOrtogonal(ruta, colFondo, 10f, colActivo, 5.5f);
                for (int i = 0; i < ruta.size() - 1; i++) {
                    Vector2 va = ruta.get(i);
                    Vector2 vb = ruta.get(i + 1);
                    shapeRenderer.setColor(colActivoBrillante);
                    shapeRenderer.rectLine(va.x, va.y, vb.x, vb.y, 2.0f);
                }
                dibujarElectronesEnRuta(ruta, tiempoTotal);
            } else {
                dibujarLineaCableOrtogonal(ruta, colFondo, 10f, colInactivo, 5.0f);
            }

            for (int i = 1; i < ruta.size() - 1; i++) {
                Vector2 codo = ruta.get(i);
                boolean codoHover = (ratonMundo.dst(codo.x, codo.y) <= 22f);

                shapeRenderer.setColor(COLOR_CAJA_OSCURA);
                shapeRenderer.circle(codo.x, codo.y, 10f);

                shapeRenderer.setColor(codoHover ? Color.WHITE : COLOR_CELESTE_BRILLANTE);
                shapeRenderer.circle(codo.x, codo.y, 6f);

                if (codoHover) {
                    float pulso = 1f + 0.25f * MathUtils.sin(tiempoTotal * 14f);
                    shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.55f);
                    shapeRenderer.circle(codo.x, codo.y, 19f * pulso);
                }
            }
        }
    }

    private void dibujarCableArrastreRecto() {
        if (terminalOrigenCable == null) return;

        List<Vector2> ruta = calcularRutaOrtogonalArrastre(terminalOrigenCable, ratonMundo);
        Color colFondo = new Color(0.08f, 0.10f, 0.14f, 0.95f);
        Color colGuia = new Color(1.00f, 0.85f, 0.20f, 1f);

        dibujarLineaCableOrtogonal(ruta, colFondo, 9f, colGuia, 4.5f);

        Vector2 extremo = ruta.get(ruta.size() - 1);
        shapeRenderer.setColor(1.00f, 0.85f, 0.20f, 0.5f);
        shapeRenderer.circle(extremo.x, extremo.y, 14f);
        shapeRenderer.setColor(1.00f, 0.95f, 0.50f, 1f);
        shapeRenderer.circle(extremo.x, extremo.y, 7f);
    }

    private void dibujarLineaCableOrtogonal(List<Vector2> ruta, Color colorFondo, float grosorFondo, Color colorFrente, float grosorFrente) {
        if (ruta == null || ruta.size() < 2) return;
        for (int i = 0; i < ruta.size() - 1; i++) {
            Vector2 va = ruta.get(i);
            Vector2 vb = ruta.get(i + 1);
            shapeRenderer.setColor(colorFondo);
            shapeRenderer.rectLine(va.x, va.y, vb.x, vb.y, grosorFondo);
        }
        for (int i = 0; i < ruta.size() - 1; i++) {
            Vector2 va = ruta.get(i);
            Vector2 vb = ruta.get(i + 1);
            shapeRenderer.setColor(colorFrente);
            shapeRenderer.rectLine(va.x, va.y, vb.x, vb.y, grosorFrente);
        }
        for (int i = 1; i < ruta.size() - 1; i++) {
            Vector2 codo = ruta.get(i);
            shapeRenderer.setColor(colorFondo);
            shapeRenderer.circle(codo.x, codo.y, grosorFondo * 0.55f);
            shapeRenderer.setColor(colorFrente);
            shapeRenderer.circle(codo.x, codo.y, grosorFrente * 0.55f);
        }
    }

    private void dibujarElectronesEnRuta(List<Vector2> ruta, float tiempo) {
        if (ruta == null || ruta.size() < 2) return;
        float longitudTotal = 0f;
        float[] distancias = new float[ruta.size() - 1];
        for (int i = 0; i < ruta.size() - 1; i++) {
            distancias[i] = ruta.get(i).dst(ruta.get(i + 1));
            longitudTotal += distancias[i];
        }
        if (longitudTotal <= 1f) return;

        for (int e = 0; e < 2; e++) {
            float tElect = (tiempo * 0.6f + e * 0.5f) % 1.0f;
            float distDeseada = tElect * longitudTotal;
            float acumulado = 0f;

            Vector2 posElectron = ruta.get(0);
            float anguloElectron = 0f;

            for (int i = 0; i < ruta.size() - 1; i++) {
                float segLen = distancias[i];
                if (distDeseada <= acumulado + segLen || i == ruta.size() - 2) {
                    float factor = (segLen > 0.001f) ? (distDeseada - acumulado) / segLen : 0f;
                    factor = MathUtils.clamp(factor, 0f, 1f);
                    Vector2 va = ruta.get(i);
                    Vector2 vb = ruta.get(i + 1);
                    posElectron = va.cpy().lerp(vb, factor);
                    anguloElectron = MathUtils.atan2(vb.y - va.y, vb.x - va.x);
                    break;
                }
                acumulado += segLen;
            }

            dibujarElectronCaricatura(posElectron.x, posElectron.y, anguloElectron, 0.9f, tiempo + e);
        }
    }

    public List<Vector2> calcularRutaOrtogonal(Cable cable, Terminal tOrigen, Terminal tDestino) {
        List<Vector2> vertices = new ArrayList<>();
        if (tOrigen == null || tDestino == null) return vertices;
        Vector2 p1 = tOrigen.getPosicionAbsoluta();
        Vector2 p2 = tDestino.getPosicionAbsoluta();

        boolean origenEsLed = (tOrigen.getComponentePadre() instanceof LED);
        boolean destinoEsLed = (tDestino.getComponentePadre() instanceof LED);

        if (cable != null && cable.tieneCodoPersonalizado()) {
            float cx = cable.getCodoPersonalizadoX();
            float cy = cable.getCodoPersonalizadoY();
            return calcularPuntosOrtogonalesConCodoManual(p1, p2, cx, cy, origenEsLed, destinoEsLed);
        }

        int indiceCable = (minijuego != null && cable != null) ? minijuego.getCables().indexOf(cable) : 0;
        float desfaseCanal = 0f;
        if (indiceCable == 1) {
            desfaseCanal = 32f;
        } else if (indiceCable == 2) {
            desfaseCanal = -32f;
        } else if (indiceCable > 2) {
            desfaseCanal = ((indiceCable % 2 == 0) ? -1f : 1f) * (32f + (indiceCable / 2) * 16f);
        }

        return calcularPuntosOrtogonales(p1, p2, origenEsLed, destinoEsLed, desfaseCanal);
    }

    public List<Vector2> calcularRutaOrtogonal(Terminal tOrigen, Terminal tDestino) {
        return calcularRutaOrtogonal(null, tOrigen, tDestino);
    }

    public List<Vector2> calcularRutaOrtogonalArrastre(Terminal tOrigen, Vector2 pDestino) {
        List<Vector2> vertices = new ArrayList<>();
        if (tOrigen == null || pDestino == null) return vertices;
        Vector2 p1 = tOrigen.getPosicionAbsoluta();

        boolean origenEsLed = (tOrigen.getComponentePadre() instanceof LED);
        boolean destinoEsLed = (terminalBajoRaton != null && terminalBajoRaton.getComponentePadre() instanceof LED);

        return calcularPuntosOrtogonales(p1, pDestino, origenEsLed, destinoEsLed, 0f);
    }

    private List<Vector2> calcularPuntosOrtogonalesConCodoManual(Vector2 p1, Vector2 p2, float cx, float cy, boolean origenEsVertical, boolean destinoEsVertical) {
        List<Vector2> ruta = new ArrayList<>();
        ruta.add(new Vector2(p1.x, p1.y));

        if (origenEsVertical) {
            ruta.add(new Vector2(p1.x, cy));
            if (Math.abs(p2.x - p1.x) > 1f) {
                ruta.add(new Vector2(p2.x, cy));
            }
            ruta.add(new Vector2(p2.x, p2.y));
        } else {
            ruta.add(new Vector2(cx, p1.y));
            if (Math.abs(p2.y - p1.y) > 1f) {
                ruta.add(new Vector2(cx, p2.y));
            }
            ruta.add(new Vector2(p2.x, p2.y));
        }

        return simplificarPuntosConsecutivos(ruta);
    }

    private List<Vector2> calcularPuntosOrtogonales(Vector2 p1, Vector2 p2, boolean origenEsVertical, boolean destinoEsVertical, float desfaseCanal) {
        List<Vector2> ruta = new ArrayList<>();
        ruta.add(new Vector2(p1.x, p1.y));

        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float distManhattan = Math.abs(dx) + Math.abs(dy);
        if (distManhattan < 1f) {
            ruta.add(new Vector2(p2.x, p2.y));
            return ruta;
        }

        float anguloGrados = Math.abs(MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
        if (anguloGrados > 90f) {
            anguloGrados = 180f - anguloGrados;
        }

        if (anguloGrados <= 6f || Math.abs(dy) <= 8f) {
            ruta.add(new Vector2(p2.x, p1.y));
            return ruta;
        }

        if (anguloGrados >= 84f || Math.abs(dx) <= 8f) {
            ruta.add(new Vector2(p1.x, p2.y));
            return ruta;
        }

        if (origenEsVertical) {
            float codoY = p2.y + desfaseCanal;
            ruta.add(new Vector2(p1.x, codoY));
            ruta.add(new Vector2(p2.x, codoY));
            ruta.add(new Vector2(p2.x, p2.y));
        } else if (destinoEsVertical) {
            float codoX = p2.x + desfaseCanal;
            ruta.add(new Vector2(codoX, p1.y));
            ruta.add(new Vector2(codoX, p2.y));
            ruta.add(new Vector2(p2.x, p2.y));
        } else {
            if (Math.abs(dx) >= Math.abs(dy)) {
                float codoX = (p1.x + p2.x) * 0.5f + desfaseCanal;
                ruta.add(new Vector2(codoX, p1.y));
                ruta.add(new Vector2(codoX, p2.y));
                ruta.add(new Vector2(p2.x, p2.y));
            } else {
                float codoY = (p1.y + p2.y) * 0.5f + desfaseCanal;
                ruta.add(new Vector2(p1.x, codoY));
                ruta.add(new Vector2(p2.x, codoY));
                ruta.add(new Vector2(p2.x, p2.y));
            }
        }

        return simplificarPuntosConsecutivos(ruta);
    }

    private List<Vector2> simplificarPuntosConsecutivos(List<Vector2> entrada) {
        List<Vector2> limpia = new ArrayList<>();
        if (entrada == null || entrada.isEmpty()) return limpia;
        limpia.add(entrada.get(0));
        for (int i = 1; i < entrada.size(); i++) {
            Vector2 actual = entrada.get(i);
            Vector2 anterior = limpia.get(limpia.size() - 1);
            if (actual.dst(anterior) > 1f) {
                limpia.add(actual);
            }
        }
        return limpia;
    }

    public Cable obtenerCableCodoBajoRaton(float mx, float my, float radio) {
        if (minijuego == null) return null;
        for (Cable cable : minijuego.getCables()) {
            Terminal t1 = cable.getTerminalOrigen();
            Terminal t2 = cable.getTerminalDestino();
            if (t1 == null || t2 == null) continue;
            List<Vector2> ruta = calcularRutaOrtogonal(cable, t1, t2);
            for (int i = 1; i < ruta.size() - 1; i++) {
                Vector2 codo = ruta.get(i);
                if (codo.dst(mx, my) <= radio) {
                    return cable;
                }
            }
        }
        return null;
    }

    private void dibujarTerminalesFisicos() {
        if (minijuego == null) return;

        for (Componente comp : minijuego.getComponentes()) {
            for (Terminal t : comp.getTerminales()) {
                Vector2 pos = t.getPosicionAbsoluta();
                boolean hovered = (terminalBajoRaton == t);
                boolean conectado = t.estaConectado();
                boolean esPositivo = t.getIdentificador().contains("POSITIVO") || t.getIdentificador().contains("ANODO");
                boolean esNegativo = t.getIdentificador().contains("NEGATIVO") || t.getIdentificador().contains("CATODO");

                shapeRenderer.setColor(0.12f, 0.15f, 0.20f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 17f);

                if (esPositivo) {
                    shapeRenderer.setColor(0.95f, 0.20f, 0.20f, 1f);
                } else if (esNegativo) {
                    shapeRenderer.setColor(0.20f, 0.55f, 0.95f, 1f);
                } else {
                    shapeRenderer.setColor(0.80f, 0.85f, 0.95f, 1f);
                }
                shapeRenderer.circle(pos.x, pos.y, 13f);

                shapeRenderer.setColor(0.05f, 0.07f, 0.10f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 6f);

                if (hovered) {
                    float pulso = 1f + 0.25f * MathUtils.sin(tiempoTotal * 12f);
                    shapeRenderer.setColor(0.20f, 1.00f, 0.40f, 0.45f);
                    shapeRenderer.circle(pos.x, pos.y, 28f * pulso);
                }

                if (conectado) {
                    shapeRenderer.setColor(0.00f, 0.95f, 1.00f, 0.35f);
                    shapeRenderer.circle(pos.x, pos.y, 20f);
                }
            }
        }
    }

    private void dibujarComponentesSprites() {
        if (minijuego == null) return;

        for (Componente comp : minijuego.getComponentes()) {
            float x = comp.getPosicionX();
            float y = comp.getPosicionY();
            float w = comp.getAncho();
            float h = comp.getAlto();

            if (comp instanceof LED) {
                LED led = (LED) comp;
                TextureRegion frame;
                if (led.estaQuemado()) {
                    frame = animLedMuriendo.getKeyFrame(tiempoAnimError, false);
                } else if ("EXITO".equals(led.getEstadoActual())) {
                    frame = animLedPrendiendo.getKeyFrame(tiempoTotal, true);
                } else {
                    frame = animLedNormal.getKeyFrame(tiempoTotal, true);
                }
                batch.draw(frame, x, y, w, h);

                fontPequena.setColor(1f, 0.3f, 0.3f, 1f);
                fontPequena.draw(batch, "+", x + 138f, y + 90f);
                fontPequena.setColor(0.3f, 0.7f, 1f, 1f);
                fontPequena.draw(batch, "-", x + 167f, y + 90f);

                font.setColor(1f, 1f, 1f, 1f);
                font.draw(batch, "LED", x + 145f, y + h + 24f);
            } else if (comp instanceof Resistencia) {
                Resistencia res = (Resistencia) comp;
                TextureRegion frame;
                if ("ERROR".equals(res.getEstadoActual())) {
                    frame = animResistenciaFallo.getKeyFrame(tiempoAnimError, true);
                } else {
                    frame = animResistenciaNormal.getKeyFrame(tiempoTotal, true);
                }
                batch.draw(frame, x, y, w, h);

                fontPequena.setColor(1f, 1f, 1f, 1f);
                fontPequena.draw(batch, "ENT", x + 36f, y + 145f);
                fontPequena.draw(batch, "SAL", x + 300f, y + 145f);

                font.setColor(1f, 1f, 1f, 1f);
                font.draw(batch, res.getTextoEtiqueta(), x + 150f, y + h + 22f);
            }
        }
    }

    private void dibujarTextosHUD() {
        FuenteAlimentacion fuente = null;
        if (minijuego != null) {
            for (Componente c : minijuego.getComponentes()) {
                if (c instanceof FuenteAlimentacion) {
                    fuente = (FuenteAlimentacion) c;
                    break;
                }
            }
        }

        if (fuente != null) {
            float fx = fuente.getPosicionX();
            float fy = fuente.getPosicionY();
            float fw = fuente.getAncho();
            float fh = fuente.getAlto();

            fontTitulo.setColor(0.00f, 0.95f, 1.00f, 1f);
            fontTitulo.draw(batch, String.format("%.2f V", voltajeMedidorDC), fx + 38f, fy + fh - 52f);

            float corriente = (minijuego.estaGanado()) ? 0.02f : 0.00f;
            font.setColor(0.95f, 0.80f, 0.20f, 1f);
            font.draw(batch, String.format("%.2f A", corriente), fx + 42f, fy + fh - 95f);

            fontPequena.setColor(0.85f, 0.90f, 0.98f, 1f);
            fontPequena.draw(batch, "FUENTE DE PODER", fx + 32f, fy + fh - 152f);

            fontTitulo.setColor(1.0f, 0.25f, 0.25f, 1f);
            fontTitulo.draw(batch, "+", fx + fw - 58f, fy + fh * 0.70f + 12f);

            fontTitulo.setColor(0.25f, 0.65f, 1.0f, 1f);
            fontTitulo.draw(batch, "-", fx + fw - 54f, fy + fh * 0.30f + 12f);
        }

        fontTitulo.setColor(0.00f, 0.90f, 1.00f, 1f);
        fontTitulo.draw(batch, "NIVEL 1", 40f, ALTO_VIRTUAL - 30f);

        if (minijuego != null) {
            String mensaje = minijuego.getMensajeEstado();
            if (minijuego.estaGanado()) {
                font.setColor(0.20f, 1.00f, 0.40f, 1f);
            } else if (hayErrorLED()) {
                font.setColor(1.00f, 0.30f, 0.30f, 1f);
            } else {
                font.setColor(0.95f, 0.98f, 1.00f, 1f);
            }
            font.draw(batch, mensaje, 40f, ALTO_VIRTUAL - 62f);
        }

        fontPequena.setColor(0.80f, 0.88f, 0.95f, 1f);
        fontPequena.draw(batch, "[Arrastrar desde la barra derecha] Colocar piezas | [Clic Izq] Cables rectos | [Clic Der] Quitar | [R] Reiniciar | [ESC] Salir", 40f, 38f);
    }

    private void dibujarFondoModal() {
        shapeRenderer.setColor(0.04f, 0.05f, 0.07f, 0.80f);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        float mx = 560f;
        float my = 320f;
        float mw = 800f;
        float mh = 440f;

        shapeRenderer.setColor(0.12f, 0.15f, 0.20f, 0.98f);
        shapeRenderer.rect(mx, my, mw, mh);

        if (minijuego != null && minijuego.estaGanado()) {
            shapeRenderer.setColor(0.15f, 0.85f, 0.45f, 1f);
        } else {
            shapeRenderer.setColor(0.95f, 0.25f, 0.25f, 1f);
        }
        shapeRenderer.rectLine(mx, my, mx + mw, my, 4f);
        shapeRenderer.rectLine(mx + mw, my, mx + mw, my + mh, 4f);
        shapeRenderer.rectLine(mx + mw, my + mh, mx, my + mh, 4f);
        shapeRenderer.rectLine(mx, my + mh, mx, my, 4f);

        shapeRenderer.setColor(0.20f, 0.26f, 0.36f, 1f);
        shapeRenderer.rect(630f, 360f, 300f, 60f);
        shapeRenderer.rect(970f, 360f, 300f, 60f);
    }

    private void dibujarContenidoModal() {
        boolean ganado = (minijuego != null && minijuego.estaGanado());

        if (ganado) {
            fontTitulo.setColor(0.20f, 1.00f, 0.50f, 1f);
            glyphLayout.setText(fontTitulo, "¡NIVEL 1 COMPLETADO CON ÉXITO!");
            fontTitulo.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 700f);

            font.setColor(0.95f, 0.95f, 1f, 1f);
            glyphLayout.setText(font, "¡Excelente! Has cerrado el circuito de corriente continua.");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 620f);

            glyphLayout.setText(font, "La resistencia limitó la corriente, protegiendo al diodo LED.");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 570f);

            glyphLayout.setText(font, "Los electrones fluyen libremente en un camino cerrado.");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 520f);

            font.setColor(0.20f, 0.95f, 0.45f, 1f);
            glyphLayout.setText(font, "REPETIR RETO");
            font.draw(batch, glyphLayout, 630f + (300f - glyphLayout.width) / 2f, 398f);

            font.setColor(0.95f, 0.85f, 0.30f, 1f);
            glyphLayout.setText(font, "MENÚ PRINCIPAL");
            font.draw(batch, glyphLayout, 970f + (300f - glyphLayout.width) / 2f, 398f);
        } else {
            fontTitulo.setColor(1.00f, 0.25f, 0.25f, 1f);
            glyphLayout.setText(fontTitulo, "¡ALERTA DE SOBRETENSIÓN!");
            fontTitulo.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 700f);

            font.setColor(0.95f, 0.95f, 1f, 1f);
            glyphLayout.setText(font, "El LED se ha quemado por exceso de corriente eléctrica.");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 620f);

            glyphLayout.setText(font, "Conectaste 5V directo a tierra sin una resistencia limitadora.");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 570f);

            glyphLayout.setText(font, "Inserta la resistencia en serie para disipar el voltaje sobrante.");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 520f);

            font.setColor(1.00f, 0.40f, 0.40f, 1f);
            glyphLayout.setText(font, "REINTENTAR");
            font.draw(batch, glyphLayout, 630f + (300f - glyphLayout.width) / 2f, 398f);

            font.setColor(0.85f, 0.90f, 0.95f, 1f);
            glyphLayout.setText(font, "MENÚ PRINCIPAL");
            font.draw(batch, glyphLayout, 970f + (300f - glyphLayout.width) / 2f, 398f);
        }
    }

    private void actualizarParticulas(float delta) {
        for (int i = particulas.size() - 1; i >= 0; i--) {
            ParticulaChispa p = particulas.get(i);
            p.vida -= delta;
            if (p.vida <= 0f) {
                particulas.remove(i);
            } else {
                p.x += p.vx * delta;
                p.y += p.vy * delta;
                p.vy -= 180f * delta;
            }
        }
    }

    private void dibujarParticulas() {
        for (ParticulaChispa p : particulas) {
            float alfa = p.vida / p.vidaMax;
            shapeRenderer.setColor(p.r, p.g, p.b, alfa);
            shapeRenderer.circle(p.x, p.y, 3.5f * alfa);
        }
    }

    public void generarChispasImpacto(float cx, float cy, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float vel = MathUtils.random(60f, 260f);
            float vx = MathUtils.cos(ang) * vel;
            float vy = MathUtils.sin(ang) * vel;
            float vida = MathUtils.random(0.3f, 0.8f);
            particulas.add(new ParticulaChispa(cx, cy, vx, vy, vida, 0.1f, 0.9f, 1f));
        }
    }

    public Terminal obtenerTerminalBajoRaton(float mx, float my, float radio) {
        if (minijuego == null) return null;
        for (Componente comp : minijuego.getComponentes()) {
            for (Terminal t : comp.getTerminales()) {
                Vector2 pos = t.getPosicionAbsoluta();
                if (pos.dst(mx, my) <= radio) {
                    return t;
                }
            }
        }
        return null;
    }

    public Componente obtenerComponenteBajoRaton(float mx, float my) {
        if (minijuego == null) return null;
        for (Componente comp : minijuego.getComponentes()) {
            float cx = comp.getPosicionX();
            float cy = comp.getPosicionY();
            float cw = comp.getAncho();
            float ch = comp.getAlto();
            if (mx >= cx && mx <= cx + cw && my >= cy && my <= cy + ch) {
                return comp;
            }
        }
        return null;
    }

    private float distanciaPuntoASegmento(Vector2 a, Vector2 b, float px, float py) {
        float l2 = a.dst2(b);
        if (l2 == 0f) return a.dst(px, py);
        float t = Math.max(0f, Math.min(1f, ((px - a.x) * (b.x - a.x) + (py - a.y) * (b.y - a.y)) / l2));
        float projX = a.x + t * (b.x - a.x);
        float projY = a.y + t * (b.y - a.y);
        float dx = px - projX;
        float dy = py - projY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public Cable obtenerCableBajoRaton(float mx, float my, float tolerancia) {
        if (minijuego == null) return null;
        for (Cable cable : minijuego.getCables()) {
            Terminal t1 = cable.getTerminalOrigen();
            Terminal t2 = cable.getTerminalDestino();
            if (t1 == null || t2 == null) continue;
            List<Vector2> ruta = calcularRutaOrtogonal(t1, t2);
            for (int i = 0; i < ruta.size() - 1; i++) {
                Vector2 va = ruta.get(i);
                Vector2 vb = ruta.get(i + 1);
                float d = distanciaPuntoASegmento(va, vb, mx, my);
                if (d <= tolerancia) {
                    return cable;
                }
            }
        }
        return null;
    }

    public boolean manejarClicUI(float mx, float my) {
        boolean modalActivo = (minijuego != null && ((minijuego.estaGanado() && tiempoExitoAcumulado >= 3.0f) || faseDerrota == FASE_DERROTA_MODAL));
        if (modalActivo) {
            if (mx >= 630f && mx <= 930f && my >= 360f && my <= 420f) {
                reiniciarNivel();
                return true;
            }
            if (mx >= 970f && mx <= 1270f && my >= 360f && my <= 420f) {
                volverAlMenu();
                return true;
            }
            return true;
        }
        return false;
    }

    public void iniciarCable(Terminal term, float mx, float my) {
        this.cableEnProgreso = true;
        this.terminalOrigenCable = term;
        this.ratonMundo.set(mx, my);
    }

    public void actualizarCableEnProgreso(float mx, float my) {
        this.ratonMundo.set(mx, my);
    }

    public void finalizarCable(Terminal termDestino) {
        if (cableEnProgreso && terminalOrigenCable != null && termDestino != null) {
            if (termDestino != terminalOrigenCable && termDestino.getComponentePadre() != terminalOrigenCable.getComponentePadre()) {
                boolean existe = false;
                for (Cable c : minijuego.getCables()) {
                    if (c.conectaTerminal(terminalOrigenCable) && c.conectaTerminal(termDestino)) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    Cable nuevo = new Cable(terminalOrigenCable, termDestino);
                    minijuego.agregarCable(nuevo);
                    generarChispasImpacto(termDestino.getPosicionAbsoluta().x, termDestino.getPosicionAbsoluta().y, 12);
                }
            }
        }
        cancelarCable();
    }

    public void cancelarCable() {
        this.cableEnProgreso = false;
        this.terminalOrigenCable = null;
    }

    public void actualizarPosicionCursor(float mx, float my) {
        this.ratonMundo.set(mx, my);
        this.terminalBajoRaton = obtenerTerminalBajoRaton(mx, my, 34f);
    }

    public void reiniciarNivel() {
        if (minijuego != null) {
            minijuego.inicializarNivel1();
        }
        this.tiempoIntro = 0f;
        this.introCompletada = false;
        this.voltajeMedidorDC = 0f;
        this.tiempoExitoAcumulado = 0f;
        this.tiempoIntroNivel = 0f;
        this.faseDerrota = FASE_DERROTA_NINGUNA;
        this.tiempoFaseDerrota = 0f;
        this.sacudidaCamaraX = 0f;
        this.sacudidaCamaraY = 0f;
        this.opacidadHollin = 0f;
        this.posicionLimpiadorX = -150f;
        cancelarCable();
    }

    public void volverAlMenu() {
        if (juego != null) {
            juego.setScreen(new VistaMenu(juego));
        }
    }

    public void saltarIntro() {
        this.tiempoIntro = 3f;
        this.introCompletada = true;
        this.voltajeMedidorDC = 5.0f;
    }

    public boolean estaCableEnProgreso() {
        return cableEnProgreso;
    }

    public boolean isCableEnProgreso() {
        return cableEnProgreso;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontPequena != null) fontPequena.dispose();
        if (texturasCargadas != null) {
            for (Texture t : texturasCargadas) {
                if (t != null) t.dispose();
            }
            texturasCargadas.clear();
        }
    }
}
