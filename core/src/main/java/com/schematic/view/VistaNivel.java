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
import com.schematic.audio.GestorAudio;
import com.schematic.audio.TipoEfecto;
import com.schematic.model.componentes.*;
import com.schematic.model.niveles.*;
import com.schematic.model.progreso.GestorPersistenciaJSON;
import com.schematic.model.progreso.ProgresoJugador;
import com.schematic.JuegoPrincipal;

import java.util.ArrayList;
import java.util.List;

public class VistaNivel extends ScreenAdapter {

    public static final float ANCHO_VIRTUAL = 1920f;
    public static final float ALTO_VIRTUAL = 1080f;

    private static final float[] SLOTS_Y = {720f, 515f, 310f, 105f};
    private static final int FASE_DERROTA_NINGUNA = 0;
    private static final int FASE_DERROTA_ALARMA = 1;
    private static final int FASE_DERROTA_EXPLOSION = 2;
    private static final int FASE_DERROTA_LIMPIEZA = 3;
    private static final int FASE_DERROTA_MODAL = 4;

    private Game juego;
    private GestorAudio gestorAudio;
    private Nivel nivel;
    private OrthographicCamera camara;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font, fontTitulo, fontPequena;
    private GlyphLayout glyphLayout;
    private ControladorNivel controlador;

    private List<Texture> texturasCargadas;
    private Animation<TextureRegion> animLedNormal, animLedPrendiendo, animLedMuriendo;
    private Animation<TextureRegion> animResistenciaNormal, animResistenciaFallo, animAnd, animOr;

    private boolean mostrandoGuiaNivel;
    private float tiempoTotal, tiempoAnimError, tiempoIntro, voltajeMedidorDC, tiempoExitoAcumulado;
    private boolean introCompletada, progresoGuardado, derrotaPorTiempo;
    private float offsetGridPerspectiva, tiempoIntroNivel;
    private int ultimoSegundoSonado = -1;

    private int faseDerrota = FASE_DERROTA_NINGUNA;
    private float tiempoFaseDerrota = 0f;
    private float sacudidaCamaraX = 0f, sacudidaCamaraY = 0f, opacidadHollin = 0f, posicionLimpiadorX = -150f;
    private Vector2 centroExplosion = new Vector2(950f, 500f);

    private boolean cableEnProgreso;
    private Terminal terminalOrigenCable;
    private Vector2 ratonMundo;
    private Terminal terminalBajoRaton, ultimoTerminalHover;
    private int ultimoUIHover = -1;

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

    public VistaNivel(Nivel nivel) {
        this(nivel, null);
    }

    public VistaNivel(Nivel nivel, Game juego) {
        this.nivel = nivel;
        this.juego = juego;
        this.gestorAudio = GestorAudio.obtenerDesde(juego);

        this.camara = new OrthographicCamera();
        this.viewport = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
        this.viewport.apply();
        this.camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);
        this.camara.update();

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();

        this.font = crearFont(1.25f);
        this.fontTitulo = crearFont(1.75f);
        this.fontPequena = crearFont(0.95f);

        this.texturasCargadas = new ArrayList<>();
        cargarAnimaciones();

        this.ratonMundo = new Vector2();
        this.particulas = new ArrayList<>();
        this.mostrandoGuiaNivel = (nivel != null && (nivel.getNumeroNivel() == 3 || nivel.getNumeroNivel() == 4));
        this.controlador = new ControladorNivel(nivel, camara, viewport, this);
    }

    private BitmapFont crearFont(float escala) {
        BitmapFont f = new BitmapFont();
        f.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        f.getData().setScale(escala);
        return f;
    }

    private void cargarAnimaciones() {
        animLedNormal = cargarAnimacion("led_normal", 10, 0.12f);
        animLedPrendiendo = cargarAnimacion("led_prendiendo", 8, 0.10f);
        animLedMuriendo = cargarAnimacion("led_muriendo", 10, 0.14f);
        animResistenciaNormal = cargarAnimacion("resistencia_normal", 8, 0.22f);
        animResistenciaFallo = cargarAnimacion("resistencia_fail", 15, 0.15f);
        animAnd = cargarAnimacion("and", 12, 0.12f);
        animOr = cargarAnimacion("or", 12, 0.12f);
    }

    private Animation<TextureRegion> cargarAnimacion(String carpeta, int cantidadFrames, float duracionFrame) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < cantidadFrames; i++) {
            FileHandle fh = Gdx.files.internal(String.format("sprites/%s/frame_%05d.png", carpeta, i));
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

        if (nivel != null) {
            nivel.actualizar((introCompletada && !mostrandoGuiaNivel) ? delta : 0f);
            actualizarEstadoNivel(delta);
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
        dibujarSwitches();
        dibujarBarraLateralAlmacen();
        dibujarCablesConectadosRectos();
        if (cableEnProgreso && terminalOrigenCable != null) {
            dibujarCableArrastreRecto();
        }
        dibujarTerminalesFisicos();
        dibujarElectronesIntro();
        dibujarParticulas();
        dibujarBarraTemporizadorSuperior();
        dibujarBarraValidacionCircuito();
        dibujarBotonGuiaSuperiorShape();
        dibujarPasosGuiaHUDShapes();
        shapeRenderer.end();

        batch.begin();
        dibujarComponentesSprites();
        dibujarContenidoBarraLateral();
        dibujarTextosHUD();
        dibujarPasosGuiaHUDTextos();
        if (controlador != null && controlador.estaArrastrandoDesdeBarra()) {
            dibujarVistaPreviaArrastre();
        }
        batch.end();

        dibujarEfectosDerrota();

        boolean modalFinActivo = (nivel != null && ((nivel.estaGanado() && tiempoExitoAcumulado >= 1.2f) || faseDerrota == FASE_DERROTA_MODAL));
        if (modalFinActivo) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dibujarFondoModal();
            shapeRenderer.end();
            batch.begin();
            dibujarContenidoModal();
            batch.end();
        } else if (mostrandoGuiaNivel) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dibujarGuiaNivelShapes();
            shapeRenderer.end();
            batch.begin();
            dibujarGuiaNivelTextos();
            batch.end();
        }

        tiempoIntroNivel += delta;
        if (tiempoIntroNivel < 0.5f) {
            dibujarCortinaIntro();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void actualizarEstadoNivel(float delta) {
        if (nivel.estaGanado()) {
            if (!progresoGuardado) {
                progresoGuardado = true;
                sonar(TipoEfecto.EXITO_NIVEL);
                guardarProgresoSiguiente();
            }
            tiempoExitoAcumulado += delta;
            if (MathUtils.randomBoolean(0.25f)) {
                for (Componente c : nivel.getComponentes()) {
                    if (c instanceof LED) {
                        generarChispasImpacto(c.getPosicionX() + c.getAncho() * 0.49f, c.getPosicionY() + c.getAlto() * 0.65f, 4);
                    }
                }
            }
        } else {
            tiempoExitoAcumulado = 0f;
            progresoGuardado = false;
        }

        if (nivel.getTiempoCircuitoActivo() > 0 && !nivel.estaGanado()) {
            int seg = (int) nivel.getTiempoCircuitoActivo();
            if (seg > ultimoSegundoSonado && seg >= 1) {
                ultimoSegundoSonado = seg;
                sonar(TipoEfecto.ELECTRON);
            }
        } else if (nivel.getTiempoCircuitoActivo() <= 0) {
            ultimoSegundoSonado = 0;
        }

        boolean hayError = false;
        for (Componente c : nivel.getComponentes()) {
            if ("ERROR".equals(c.getEstadoActual())) {
                hayError = true;
                break;
            }
        }
        boolean tiempoAgotado = (introCompletada && nivel.getTiempoRestante() <= 0f && !nivel.estaGanado());
        if ((hayError || tiempoAgotado) && faseDerrota == FASE_DERROTA_NINGUNA) {
            derrotaPorTiempo = tiempoAgotado && !hayError;
            faseDerrota = FASE_DERROTA_ALARMA;
            tiempoFaseDerrota = 0f;
            sonar(TipoEfecto.ALARMA_TIEMPO);
            if (derrotaPorTiempo) centroExplosion.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f);
            else localizarCentroFallo();
        }
    }

    private void guardarProgresoSiguiente() {
        int siguiente = nivel.getNumeroNivel() + 1;
        if (juego instanceof JuegoPrincipal) {
            JuegoPrincipal jp = (JuegoPrincipal) juego;
            if (jp.getProgreso() != null) {
                jp.getProgreso().desbloquearNivel(siguiente);
                jp.guardarProgreso();
            }
        } else {
            ProgresoJugador p = GestorPersistenciaJSON.cargar();
            p.desbloquearNivel(siguiente);
            GestorPersistenciaJSON.guardar(p);
        }
    }

    private void dibujarCortinaIntro() {
        float alfa = MathUtils.clamp(1f - (tiempoIntroNivel / 0.5f), 0f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_FONDO_NEGRO.r, COLOR_FONDO_NEGRO.g, COLOR_FONDO_NEGRO.b, alfa);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        float barraY = ALTO_VIRTUAL * (1f - alfa);
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alfa * 0.85f);
        shapeRenderer.rectLine(0, barraY, ANCHO_VIRTUAL, barraY, 4f);
        shapeRenderer.end();
    }

    private boolean hayErrorLED() {
        if (nivel == null) return false;
        for (Componente c : nivel.getComponentes()) {
            if (c instanceof LED && ((LED) c).estaQuemado()) return true;
        }
        return false;
    }

    private void localizarCentroFallo() {
        if (nivel == null) return;
        for (Componente c : nivel.getComponentes()) {
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
            sacudidaCamaraX = MathUtils.random(-3f, 3f);
            sacudidaCamaraY = MathUtils.random(-3f, 3f);
            if (MathUtils.randomBoolean(0.4f)) generarChispasImpacto(centroExplosion.x, centroExplosion.y, 4);
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
            float duracion = 2.2f;
            float progreso = MathUtils.clamp(tiempoFaseDerrota / duracion, 0f, 1f);
            posicionLimpiadorX = MathUtils.lerp(-140f, ANCHO_VIRTUAL + 180f, progreso);
            if (MathUtils.randomBoolean(0.4f) && posicionLimpiadorX > 0 && posicionLimpiadorX < ANCHO_VIRTUAL) {
                float yParticula = 540f + MathUtils.sin(tiempoFaseDerrota * 6.5f) * 230f;
                generarChispasImpacto(posicionLimpiadorX - 25f, yParticula, 3);
            }
            if (tiempoFaseDerrota >= duracion) {
                faseDerrota = FASE_DERROTA_MODAL;
                tiempoFaseDerrota = 0f;
                opacidadHollin = 0f;
            }
        }
    }

    public void generarGranExplosion(float cx, float cy) {
        sonar(TipoEfecto.CORTOCIRCUITO);
        for (int i = 0; i < 75; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float vel = MathUtils.random(100f, 550f);
            particulas.add(new ParticulaChispa(cx, cy, MathUtils.cos(ang) * vel, MathUtils.sin(ang) * vel,
                    MathUtils.random(0.5f, 1.4f), MathUtils.random(0.9f, 1f), MathUtils.random(0.2f, 0.8f), 0f));
        }
        for (int i = 0; i < 35; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float vel = MathUtils.random(40f, 240f);
            particulas.add(new ParticulaChispa(cx, cy, MathUtils.cos(ang) * vel, MathUtils.sin(ang) * vel,
                    MathUtils.random(0.8f, 2.0f), 0.12f, 0.12f, 0.16f));
        }
    }

    private void dibujarEfectosDerrota() {
        if (faseDerrota == FASE_DERROTA_NINGUNA) return;

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
            shapeRenderer.setColor(0f, 0f, 0f, alfaNegroPuro);
            shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);
            if (alfaNegroPuro < 0.95f) {
                float radioHollin = Math.min(1200f, tiempoFaseDerrota * 1100f);
                shapeRenderer.setColor(0f, 0f, 0f, 0.95f);
                shapeRenderer.circle(centroExplosion.x, centroExplosion.y, radioHollin);
                shapeRenderer.circle(centroExplosion.x - 180f, centroExplosion.y + 80f, radioHollin * 0.8f);
                shapeRenderer.circle(centroExplosion.x + 200f, centroExplosion.y - 70f, radioHollin * 0.85f);
            }
            shapeRenderer.end();
        } else if (faseDerrota == FASE_DERROTA_LIMPIEZA) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (posicionLimpiadorX < ANCHO_VIRTUAL) {
                float xInicio = Math.max(0f, posicionLimpiadorX);
                shapeRenderer.setColor(0f, 0f, 0f, 1f);
                shapeRenderer.rect(xInicio, 0, ANCHO_VIRTUAL - xInicio, ALTO_VIRTUAL);
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
            float yElectron = 540f + MathUtils.sin(tiempoFaseDerrota * 6.5f) * 230f;
            dibujarElectronLimpiador(posicionLimpiadorX, yElectron, tiempoFaseDerrota);
            shapeRenderer.end();
        }
    }

    private void dibujarCuerpoElectron(float x, float y, float escala, float dirX, float dirY, boolean parpadeo) {
        shapeRenderer.setColor(0.00f, 0.85f, 1.00f, 0.30f);
        shapeRenderer.circle(x, y, 22f * escala);
        shapeRenderer.setColor(0.00f, 0.95f, 1.00f, 0.85f);
        shapeRenderer.circle(x, y, 16f * escala);
        shapeRenderer.setColor(0.75f, 1.00f, 1.00f, 1f);
        shapeRenderer.circle(x, y, 11f * escala);
        shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
        shapeRenderer.circle(x - 4f * escala, y + 4f * escala, 3.5f * escala);

        shapeRenderer.setColor(1.0f, 0.45f, 0.65f, 0.45f);
        shapeRenderer.circle(x - 7.5f * escala, y - 2f * escala, 3f * escala);
        shapeRenderer.circle(x + 7.5f * escala, y - 2f * escala, 3f * escala);

        float ojoX = 5.2f * escala;
        float ojoY = 3.2f * escala;
        if (parpadeo) {
            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.rectLine(x - ojoX - 5f, y + ojoY, x - ojoX + 5f, y + ojoY, 3.5f);
            shapeRenderer.rectLine(x + ojoX - 5f, y + ojoY, x + ojoX + 5f, y + ojoY, 3.5f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(x - ojoX, y + ojoY, 5f * escala);
            shapeRenderer.circle(x + ojoX, y + ojoY, 5f * escala);
            shapeRenderer.setColor(0.05f, 0.08f, 0.15f, 1f);
            shapeRenderer.circle(x - ojoX + dirX * 1.5f * escala, y + ojoY + dirY * 1.5f * escala, 2.6f * escala);
            shapeRenderer.circle(x + ojoX + dirX * 1.5f * escala, y + ojoY + dirY * 1.5f * escala, 2.6f * escala);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(x - ojoX + 1.2f * escala, y + ojoY + 1.2f * escala, 1f * escala);
            shapeRenderer.circle(x + ojoX + 1.2f * escala, y + ojoY + 1.2f * escala, 1f * escala);
        }
        shapeRenderer.setColor(0.10f, 0.15f, 0.30f, 1f);
        shapeRenderer.ellipse(x - 2.5f * escala, y - 6f * escala, 5f * escala, 3f * escala);
        shapeRenderer.setColor(1.00f, 0.40f, 0.60f, 1f);
        shapeRenderer.circle(x, y - 6.5f * escala, 1.5f * escala);
    }

    private void dibujarElectronLimpiador(float ex, float ey, float tiempo) {
        float escala = 3.4f;
        dibujarCuerpoElectron(ex, ey, escala, 1f, 0f, (tiempo % 1.3f) > 1.15f);

        float osc = MathUtils.sin(tiempo * 16f) * 12f;
        float bx1 = ex + 34f, by1 = ey - 54f + osc;
        float bx2 = bx1, by2 = by1 + 130f;

        shapeRenderer.setColor(0.15f, 0.25f, 0.40f, 1f);
        shapeRenderer.rectLine(ex + 14f, ey - 7f, bx1, by1 + 60f, 4f);
        shapeRenderer.setColor(0.0f, 0.90f, 1.0f, 0.95f);
        shapeRenderer.rectLine(bx1, by1 - 35f, bx2, by2 + 35f, 7f);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(bx1, by1 - 35f, bx2, by2 + 35f, 2.5f);
    }

    private void dibujarElectronCaricatura(float x, float y, float angulo, float escala, float tiempoAnim) {
        float dirX = MathUtils.cos(angulo), dirY = MathUtils.sin(angulo);
        dibujarCuerpoElectron(x, y, escala, dirX, dirY, false);

        float estela1 = MathUtils.sin(tiempoAnim * 20f) * 4f * escala;
        float estela2 = MathUtils.cos(tiempoAnim * 18f) * 4f * escala;
        shapeRenderer.setColor(0.20f, 0.90f, 1.00f, 0.6f);
        shapeRenderer.circle(x - dirX * 20f * escala - dirY * estela1, y - dirY * 20f * escala + dirX * estela1, 3.5f * escala);
        shapeRenderer.circle(x - dirX * 30f * escala - dirY * estela2, y - dirY * 30f * escala + dirX * estela2, 2.2f * escala);
    }

    private void dibujarBordeRect(float x, float y, float w, float h, Color borde, float grosor) {
        shapeRenderer.setColor(borde);
        shapeRenderer.rectLine(x, y, x + w, y, grosor);
        shapeRenderer.rectLine(x + w, y, x + w, y + h, grosor);
        shapeRenderer.rectLine(x + w, y + h, x, y + h, grosor);
        shapeRenderer.rectLine(x, y + h, x, y, grosor);
    }

    private void dibujarCajaBorde(float x, float y, float w, float h, Color relleno, Color borde, float grosor) {
        shapeRenderer.setColor(relleno);
        shapeRenderer.rect(x, y, w, h);
        dibujarBordeRect(x, y, w, h, borde, grosor);
    }

    private void dibujarFlechaHorizontal(float x1, float x2, float y, Color col) {
        shapeRenderer.setColor(col);
        shapeRenderer.rectLine(x1, y, x2 - 8f, y, 3.5f);
        shapeRenderer.triangle(x2, y, x2 - 11f, y + 6.5f, x2 - 11f, y - 6.5f);
    }

    private void dibujarPulsoCableGuia(float x1, float y1, float x2, float y2, float desfase) {
        float t = (tiempoTotal * 0.85f + desfase) % 1.0f;
        float px = MathUtils.lerp(x1, x2, t);
        float py = MathUtils.lerp(y1, y2, t);
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.35f);
        shapeRenderer.circle(px, py, 5.5f);
        shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
        shapeRenderer.circle(px, py, 2.5f);
    }

    private void dibujarMesaDeTrabajo() {
        for (float y = 60f; y < ALTO_VIRTUAL - 90f; y += 35f) {
            float t = (y - 60f) / (ALTO_VIRTUAL - 150f);
            shapeRenderer.setColor(MathUtils.lerp(0.015f, 0.038f, t), MathUtils.lerp(0.024f, 0.052f, t), MathUtils.lerp(0.045f, 0.100f, t), 1f);
            shapeRenderer.rect(0, y, 1620f, 36f);
        }

        shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.08f);
        for (float x = 0; x <= 1620f; x += 60f) shapeRenderer.rectLine(x, 60f, x, ALTO_VIRTUAL - 90f, 1.0f);
        for (float y = 60f; y <= ALTO_VIRTUAL - 90f; y += 60f) shapeRenderer.rectLine(0, y, 1620f, y, 1.0f);

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.12f);
        for (float x = 0; x <= 1620f; x += 300f) shapeRenderer.rectLine(x, 60f, x, ALTO_VIRTUAL - 90f, 1.5f);
        for (float y = 60f; y <= ALTO_VIRTUAL - 90f; y += 300f) shapeRenderer.rectLine(0, y, 1620f, y, 1.5f);

        float[] pistasY = {240f, 500f, 780f};
        for (int i = 0; i < pistasY.length; i++) {
            float py = pistasY[i];
            shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.14f);
            shapeRenderer.rectLine(0, py, 1620f, py, 1.5f);
            float pulsoX = ((tiempoTotal * (140f + i * 35f)) + i * 450f) % 1820f - 100f;
            if (pulsoX >= -40f && pulsoX <= 1660f) {
                shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f);
                shapeRenderer.rectLine(Math.max(0, pulsoX - 35f), py, Math.min(1620f, pulsoX + 35f), py, 2.5f);
                shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE.r, COLOR_CELESTE_BRILLANTE.g, COLOR_CELESTE_BRILLANTE.b, 0.55f);
                if (pulsoX >= 0 && pulsoX <= 1620f) shapeRenderer.circle(pulsoX, py, 2.5f);
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
        float centroY = 220f, paso = 20f, prevX = 0f;
        float prevY = centroY + MathUtils.sin(tiempoTotal * 2.5f) * 12f;
        for (float x = paso; x <= 1620f; x += paso) {
            float y = centroY + MathUtils.sin(x * 0.006f + tiempoTotal * 2.2f) * 16f;
            float alpha = 0.08f + MathUtils.sin(tiempoTotal * 2f + x * 0.005f) * 0.03f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, alpha);
            shapeRenderer.rectLine(prevX, prevY, x, y, 2.0f);
            prevX = x;
            prevY = y;
        }
    }

    private void dibujarBarraTemporizadorSuperior() {
        if (nivel == null) return;
        float limite = nivel.getTiempoLimite() <= 0f ? 60f : nivel.getTiempoLimite();
        float ratio = MathUtils.clamp(nivel.getTiempoRestante() / limite, 0f, 1f);
        float altoBarra = 9f, yBarra = ALTO_VIRTUAL - altoBarra;
        float anchoActual = ANCHO_VIRTUAL * ratio;

        shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 0.95f);
        shapeRenderer.rect(0f, yBarra, ANCHO_VIRTUAL, altoBarra);
        shapeRenderer.setColor(0.15f, 0.22f, 0.32f, 0.85f);
        shapeRenderer.rectLine(0f, yBarra, ANCHO_VIRTUAL, yBarra, 1.5f);

        for (float xTick = 120f; xTick < ANCHO_VIRTUAL; xTick += 120f) {
            shapeRenderer.setColor(0.20f, 0.30f, 0.42f, 0.35f);
            shapeRenderer.rectLine(xTick, yBarra, xTick, yBarra + altoBarra, 1f);
        }

        if (anchoActual > 0f) {
            Color colP = ratio > 0.45f ? COLOR_NEON_CELESTE : (ratio > 0.20f ? new Color(1f, 0.78f, 0.15f, 1f) : new Color(1f, 0.20f, 0.25f, 1f));
            Color colN = ratio > 0.45f ? COLOR_CELESTE_BRILLANTE : (ratio > 0.20f ? new Color(1f, 0.95f, 0.60f, 1f) : new Color(1f, 0.85f, 0.85f, 1f));
            float pulso = ratio <= 0.20f ? (0.65f + 0.35f * MathUtils.sin(tiempoTotal * 16f)) : 1f;

            shapeRenderer.setColor(colP.r, colP.g, colP.b, colP.a * pulso);
            shapeRenderer.rect(0f, yBarra, anchoActual, altoBarra);
            shapeRenderer.setColor(colN.r, colN.g, colN.b, 0.85f * pulso);
            shapeRenderer.rect(0f, yBarra + altoBarra * 0.30f, anchoActual, altoBarra * 0.40f);
            shapeRenderer.setColor(colP.r, colP.g, colP.b, 0.45f * pulso);
            shapeRenderer.circle(anchoActual, yBarra + altoBarra / 2f, 7f);
            shapeRenderer.setColor(1f, 1f, 1f, 0.95f * pulso);
            shapeRenderer.circle(anchoActual, yBarra + altoBarra / 2f, 3.5f);
        }
    }

    private void dibujarBarraValidacionCircuito() {
        if (nivel == null) return;
        float bx = 40f, by = ALTO_VIRTUAL - 110f, bw = 260f, bh = 10f;
        float activo = nivel.getTiempoCircuitoActivo();
        float req = nivel.getTiempoRequeridoVictoria();
        float ratio = MathUtils.clamp(activo / req, 0f, 1f);
        boolean ganado = nivel.estaGanado();

        shapeRenderer.setColor(0.04f, 0.07f, 0.11f, 0.90f);
        shapeRenderer.rect(bx, by, bw, bh);
        Color borde = ganado ? new Color(0.20f, 0.95f, 0.65f, 0.95f) : (activo > 0 ? COLOR_NEON_CELESTE : new Color(0.25f, 0.35f, 0.48f, 0.55f));
        dibujarBordeRect(bx, by, bw, bh, borde, 1.4f);

        if (ratio > 0f) {
            shapeRenderer.setColor(ganado ? new Color(0.20f, 0.95f, 0.65f, 0.90f) : new Color(0.00f, 0.85f, 1.00f, 0.85f));
            shapeRenderer.rect(bx + 2f, by + 2f, (bw - 4f) * ratio, bh - 4f);
            shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
            shapeRenderer.circle(bx + 2f + (bw - 4f) * ratio, by + bh / 2f, 3.0f);
        }
    }

    private void dibujarTarjetaAlmacen(float cx, float cy, float ancho, float alto, boolean puedeUsar) {
        dibujarCajaBorde(cx, cy, ancho, alto, puedeUsar ? COLOR_SLATE_OSCURO : COLOR_CAJA_OSCURA,
                puedeUsar ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL, 2.5f);
        shapeRenderer.setColor(COLOR_FONDO_NEGRO);
        shapeRenderer.rect(cx + ancho - 55f, cy + 8f, 48f, 32f);
    }

    private void dibujarBarraLateralAlmacen() {
        float bx = 1620f, by = 60f, bw = 300f, bh = ALTO_VIRTUAL - 150f;
        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.setColor(COLOR_NEON_CELESTE);
        shapeRenderer.rectLine(bx, by, bx, by + bh, 3.8f);

        float anchoT = 250f, altoT = 185f, cx = 1645f;
        boolean puedeLed = nivel != null && nivel.buscarCasilla("LED") != null && nivel.buscarCasilla("LED").puedeUsar();
        dibujarTarjetaAlmacen(cx, SLOTS_Y[0], anchoT, altoT, puedeLed);

        boolean puedeRes = nivel != null && nivel.buscarCasilla("RESISTENCIA_220") != null && nivel.buscarCasilla("RESISTENCIA_220").puedeUsar();
        dibujarTarjetaAlmacen(cx, SLOTS_Y[1], anchoT, altoT, puedeRes);

        Nivel.CasillaInventario casSw = (nivel != null) ? nivel.buscarCasilla("SWITCH") : null;
        if (casSw != null) {
            dibujarTarjetaAlmacen(cx, SLOTS_Y[2], anchoT, altoT, casSw.puedeUsar());
            boolean esDoble = nivel.getNumeroNivel() >= 3;
            shapeRenderer.setColor(0.10f, 0.14f, 0.22f, 1f);
            shapeRenderer.rect(cx + 38f, SLOTS_Y[2] + 46f, 170f, 90f);
            if (esDoble) {
                shapeRenderer.setColor(COLOR_NEON_CELESTE);
                shapeRenderer.rectLine(cx + 52f, SLOTS_Y[2] + 91f, cx + 82f, SLOTS_Y[2] + 91f, 3.5f);
                shapeRenderer.rectLine(cx + 82f, SLOTS_Y[2] + 68f, cx + 82f, SLOTS_Y[2] + 114f, 3.5f);
                shapeRenderer.setColor(0.20f, 0.95f, 0.45f, 1f);
                shapeRenderer.rectLine(cx + 82f, SLOTS_Y[2] + 114f, cx + 188f, SLOTS_Y[2] + 114f, 4f);
                shapeRenderer.rectLine(cx + 82f, SLOTS_Y[2] + 68f, cx + 188f, SLOTS_Y[2] + 68f, 4f);
                shapeRenderer.circle(cx + 52f, SLOTS_Y[2] + 91f, 5f);
                shapeRenderer.circle(cx + 188f, SLOTS_Y[2] + 114f, 5f);
                shapeRenderer.circle(cx + 188f, SLOTS_Y[2] + 68f, 5f);
            } else {
                shapeRenderer.setColor(0.20f, 0.95f, 0.45f, 1f);
                shapeRenderer.rectLine(cx + 60f, SLOTS_Y[2] + 91f, cx + 185f, SLOTS_Y[2] + 91f, 5f);
                shapeRenderer.circle(cx + 60f, SLOTS_Y[2] + 91f, 6f);
                shapeRenderer.circle(cx + 185f, SLOTS_Y[2] + 91f, 6f);
            }
        }

        Nivel.CasillaInventario casComp = (nivel != null) ? (nivel.buscarCasilla("AND") != null ? nivel.buscarCasilla("AND") : nivel.buscarCasilla("OR")) : null;
        if (casComp != null) {
            dibujarTarjetaAlmacen(cx, SLOTS_Y[3], anchoT, altoT, casComp.puedeUsar());
        }
    }

    private void dibujarContenidoBarraLateral() {
        fontTitulo.setColor(0.00f, 0.85f, 1.00f, 1f);
        glyphLayout.setText(fontTitulo, "COMPONENTES");
        fontTitulo.draw(batch, glyphLayout, 1620f + (300f - glyphLayout.width) / 2f, 955f);

        dibujarSlotInventario(SLOTS_Y[0], "LED", "LED", animLedNormal.getKeyFrame(tiempoTotal, true), 1705f, 742f, 130f, 130f);
        dibujarSlotInventario(SLOTS_Y[1], "RESISTENCIA_220", "220 Ohm", animResistenciaNormal.getKeyFrame(tiempoTotal, true), 1682f, 548f, 175f, 112f);

        Nivel.CasillaInventario casSw = (nivel != null) ? nivel.buscarCasilla("SWITCH") : null;
        if (casSw != null) {
            int c = casSw.getCantidadDisponible();
            font.setColor(c > 0 ? Color.WHITE : COLOR_GRIS_METAL);
            font.draw(batch, "SWITCH", 1660f, SLOTS_Y[2] + 172f);
            fontTitulo.setColor(c > 0 ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
            fontTitulo.draw(batch, "x" + c, 1845f, SLOTS_Y[2] + 32f);
        }

        Nivel.CasillaInventario casComp = (nivel != null) ? (nivel.buscarCasilla("AND") != null ? nivel.buscarCasilla("AND") : nivel.buscarCasilla("OR")) : null;
        if (casComp != null) {
            boolean esAnd = (nivel.buscarCasilla("AND") != null);
            TextureRegion f = (esAnd ? animAnd : animOr).getKeyFrame(tiempoTotal, true);
            dibujarSlotInventario(SLOTS_Y[3], esAnd ? "AND" : "OR", esAnd ? "AND" : "OR", f, 1702f, 122f, 136f, 136f);
        }
    }

    private void dibujarSlotInventario(float ySlot, String idCasilla, String nombre, TextureRegion frame, float fx, float fy, float fw, float fh) {
        Nivel.CasillaInventario cas = (nivel != null) ? nivel.buscarCasilla(idCasilla) : null;
        int cant = (cas != null) ? cas.getCantidadDisponible() : 0;
        if (cant == 0) batch.setColor(0.5f, 0.5f, 0.5f, 0.45f);
        batch.draw(frame, fx, fy, fw, fh);
        batch.setColor(Color.WHITE);

        font.setColor(cant > 0 ? Color.WHITE : COLOR_GRIS_METAL);
        font.draw(batch, nombre, 1660f, ySlot + 172f);
        fontTitulo.setColor(cant > 0 ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
        fontTitulo.draw(batch, "x" + cant, 1845f, ySlot + 32f);
    }

    private void dibujarVistaPreviaArrastre() {
        if (controlador == null || !controlador.estaArrastrandoDesdeBarra()) return;
        String tipo = controlador.getTipoComponenteArrastrado();
        Vector2 pos = controlador.getPosicionArrastre();
        batch.setColor(1f, 1f, 1f, 0.85f);
        if ("LED".equals(tipo)) batch.draw(animLedNormal.getKeyFrame(tiempoTotal, true), pos.x - 120f, pos.y - 120f, 240f, 240f);
        else if ("RESISTENCIA_220".equals(tipo)) batch.draw(animResistenciaNormal.getKeyFrame(tiempoTotal, true), pos.x - 135f, pos.y - 86f, 270f, 172f);
        else if ("AND".equals(tipo)) batch.draw(animAnd.getKeyFrame(tiempoTotal, true), pos.x - 140f, pos.y - 140f, 280f, 280f);
        else if ("OR".equals(tipo)) batch.draw(animOr.getKeyFrame(tiempoTotal, true), pos.x - 140f, pos.y - 140f, 280f, 280f);
        else if ("SWITCH".equals(tipo)) {
            boolean esDoble = (nivel != null && nivel.getNumeroNivel() >= 3);
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dibujarCajaBorde(pos.x - (esDoble ? 115f : 95f), pos.y - (esDoble ? 90f : 55f), esDoble ? 230f : 190f, esDoble ? 180f : 110f,
                    new Color(0.10f, 0.14f, 0.20f, 0.90f), new Color(0.20f, 0.95f, 0.45f, 0.95f), 3f);
            shapeRenderer.end();
            batch.begin();
        }
        batch.setColor(Color.WHITE);
    }

    private void dibujarFuentePoder() {
        FuenteAlimentacion fuente = null;
        if (nivel != null) {
            for (Componente c : nivel.getComponentes()) {
                if (c instanceof FuenteAlimentacion) { fuente = (FuenteAlimentacion) c; break; }
            }
        }
        if (fuente == null) return;
        float fx = fuente.getPosicionX(), fy = fuente.getPosicionY(), fw = fuente.getAncho(), fh = fuente.getAlto();

        shapeRenderer.setColor(0.12f, 0.14f, 0.18f, 1f);
        shapeRenderer.rect(fx - 140f, fy + fh * 0.5f - 14f, 140f, 28f);
        dibujarBordeRect(fx - 8f, fy - 8f, fw + 16f, fh + 16f, new Color(0.09f, 0.11f, 0.15f, 1f), 2f);
        dibujarCajaBorde(fx, fy, fw, fh, new Color(0.16f, 0.19f, 0.25f, 1f), new Color(0.28f, 0.35f, 0.46f, 1f), 3f);

        shapeRenderer.setColor(0.08f, 0.10f, 0.14f, 1f);
        shapeRenderer.rect(fx + 20f, fy + fh - 130f, fw - 40f, 95f);

        float rLed = introCompletada ? 0.2f : 0.8f, gLed = introCompletada ? 0.8f : 0.7f, bLed = introCompletada ? 0.2f : 0.1f;
        shapeRenderer.setColor(rLed, gLed, bLed, 0.35f);
        shapeRenderer.circle(fx + 38f, fy + fh - 165f, 16f);
        shapeRenderer.setColor(rLed, gLed, bLed, 1f);
        shapeRenderer.circle(fx + 38f, fy + fh - 165f, 9f);
    }

    private void dibujarPalancaSwitch(float x1, float y1, float x2, float y2, boolean cerrada, float anguloY) {
        shapeRenderer.setColor(0.85f, 0.92f, 1.0f, 1f);
        shapeRenderer.circle(x1, y1, 7.0f);
        shapeRenderer.circle(x2, y2, 7.0f);
        if (cerrada) {
            shapeRenderer.setColor(0.15f, 0.95f, 0.70f, 1f);
            shapeRenderer.rectLine(x1, y1, x2, y2, 6.0f);
            shapeRenderer.setColor(1f, 1f, 1f, 0.85f);
            shapeRenderer.rectLine(x1, y1, x2, y2, 1.8f);
        } else {
            shapeRenderer.setColor(0.50f, 0.60f, 0.74f, 1f);
            shapeRenderer.rectLine(x1, y1, x2 - 22f, y2 + anguloY, 5.5f);
            shapeRenderer.setColor(0.85f, 0.92f, 1f, 0.65f);
            shapeRenderer.rectLine(x1, y1, x2 - 22f, y2 + anguloY, 1.6f);
        }
    }

    private void dibujarSwitches() {
        if (nivel == null) return;
        for (Componente comp : nivel.getComponentes()) {
            if (!(comp instanceof Switch)) continue;
            Switch sw = (Switch) comp;
            float sx = sw.getPosicionX(), sy = sw.getPosicionY(), swW = sw.getAncho(), swH = sw.getAlto();
            boolean conduciendo = "EXITO".equals(sw.getEstadoActual());

            dibujarCajaBorde(sx, sy, swW, swH, new Color(0.08f, 0.11f, 0.16f, 0.95f),
                    conduciendo ? new Color(0.20f, 0.85f, 0.45f, 0.9f) : COLOR_GRIS_METAL, 2.5f);

            if (conduciendo) {
                float pulso = 0.85f + 0.15f * MathUtils.sin(tiempoTotal * 10f);
                shapeRenderer.setColor(0.15f, 0.95f, 0.45f, 0.30f * pulso);
                shapeRenderer.rect(sx - 4f, sy - 4f, swW + 8f, swH + 8f);
            }

            shapeRenderer.setColor(0.04f, 0.06f, 0.09f, 1f);
            shapeRenderer.rect(sx + 12f, sy + 12f, swW - 24f, swH - 24f);

            if (sw.isDoble()) {
                float yIn = sy + 90f, yA = sy + 136f, yB = sy + 44f;
                float xIn = sx + 28f, xFork = sx + 60f, xPivote = sx + 88f, xOut = sx + 202f;

                shapeRenderer.setColor(0.18f, 0.25f, 0.36f, 0.55f);
                shapeRenderer.rectLine(sx + 74f, yIn, sx + swW - 16f, yIn, 1.5f);

                boolean energiaEntrada = sw.getTerminalEntrada() != null && sw.getTerminalEntrada().getValorLogico();
                shapeRenderer.setColor(energiaEntrada ? COLOR_NEON_CELESTE : new Color(0.45f, 0.54f, 0.66f, 1f));
                shapeRenderer.rectLine(xIn, yIn, xFork, yIn, 4.5f);
                shapeRenderer.rectLine(xFork, yB, xFork, yA, 4.5f);
                shapeRenderer.rectLine(xFork, yA, xPivote, yA, 4.5f);
                shapeRenderer.rectLine(xFork, yB, xPivote, yB, 4.5f);
                shapeRenderer.circle(xFork, yIn, 5f);

                dibujarPalancaSwitch(xPivote, yA, xOut, yA, sw.isCerradoA(), 16f);
                dibujarPalancaSwitch(xPivote, yB, xOut, yB, sw.isCerradoB(), 16f);
            } else {
                float cy = sy + swH / 2f, xPivote = sx + 28f, xOut = sx + swW - 28f;
                dibujarPalancaSwitch(xPivote, cy, xOut, cy, sw.isCerrado(), 32f);

                float indX = sx + swW / 2f, indY = sy + swH - 18f;
                Color colInd = sw.isCerrado() ? new Color(0.20f, 0.95f, 0.45f, 1f) : new Color(0.95f, 0.25f, 0.25f, 1f);
                shapeRenderer.setColor(colInd.r, colInd.g, colInd.b, 0.40f);
                shapeRenderer.circle(indX, indY, 7f);
                shapeRenderer.setColor(colInd);
                shapeRenderer.circle(indX, indY, 3.5f);
            }
        }
    }

    private void dibujarElectronesIntro() {
        if (introCompletada) return;
        float yCable = 530f, xFin = 80f;
        for (int i = 0; i < 6; i++) {
            float retraso = i * 0.35f;
            if (tiempoIntro > retraso) {
                float progreso = (tiempoIntro - retraso) / 1.1f;
                if (progreso < 1.0f) {
                    float x = MathUtils.lerp(-120f, xFin, progreso);
                    float y = yCable + MathUtils.sin(x * 0.05f + tiempoTotal * 12f) * 10f;
                    dibujarElectronCaricatura(x, y, 0f, 1.15f, tiempoTotal + i);
                } else if (progreso <= 1.05f) {
                    generarChispasImpacto(xFin, yCable, 3);
                }
            }
        }
    }

    private void dibujarCablesConectadosRectos() {
        if (nivel == null) return;
        Color colFondo = new Color(0.08f, 0.10f, 0.14f, 1f);
        Color colActivo = new Color(0.00f, 0.95f, 1.00f, 1f);
        Color colBrillante = new Color(1f, 1f, 1f, 0.92f);
        Color colInactivo = new Color(0.95f, 0.60f, 0.15f, 1f);

        for (Cable cable : nivel.getCables()) {
            Terminal t1 = cable.getTerminalOrigen(), t2 = cable.getTerminalDestino();
            if (t1 == null || t2 == null) continue;

            List<Vector2> ruta = calcularRutaOrtogonal(cable, t1, t2);
            boolean tieneEnergia = cable.isTieneEnergia();
            dibujarLineaCableOrtogonal(ruta, colFondo, 10f, tieneEnergia ? colActivo : colInactivo, tieneEnergia ? 5.5f : 5.0f);

            if (tieneEnergia) {
                for (int i = 0; i < ruta.size() - 1; i++) {
                    shapeRenderer.setColor(colBrillante);
                    shapeRenderer.rectLine(ruta.get(i).x, ruta.get(i).y, ruta.get(i + 1).x, ruta.get(i + 1).y, 2.0f);
                }
                dibujarElectronesEnRuta(ruta, tiempoTotal);
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
        dibujarLineaCableOrtogonal(ruta, new Color(0.08f, 0.10f, 0.14f, 0.95f), 9f, new Color(1.00f, 0.85f, 0.20f, 1f), 4.5f);
        Vector2 extremo = ruta.get(ruta.size() - 1);
        shapeRenderer.setColor(1.00f, 0.85f, 0.20f, 0.5f);
        shapeRenderer.circle(extremo.x, extremo.y, 14f);
        shapeRenderer.setColor(1.00f, 0.95f, 0.50f, 1f);
        shapeRenderer.circle(extremo.x, extremo.y, 7f);
    }

    private void dibujarLineaCableOrtogonal(List<Vector2> ruta, Color colorFondo, float gFondo, Color colorFrente, float gFrente) {
        if (ruta == null || ruta.size() < 2) return;
        for (int i = 0; i < ruta.size() - 1; i++) {
            shapeRenderer.setColor(colorFondo);
            shapeRenderer.rectLine(ruta.get(i).x, ruta.get(i).y, ruta.get(i + 1).x, ruta.get(i + 1).y, gFondo);
        }
        for (int i = 0; i < ruta.size() - 1; i++) {
            shapeRenderer.setColor(colorFrente);
            shapeRenderer.rectLine(ruta.get(i).x, ruta.get(i).y, ruta.get(i + 1).x, ruta.get(i + 1).y, gFrente);
        }
        for (int i = 1; i < ruta.size() - 1; i++) {
            Vector2 c = ruta.get(i);
            shapeRenderer.setColor(colorFondo);
            shapeRenderer.circle(c.x, c.y, gFondo * 0.55f);
            shapeRenderer.setColor(colorFrente);
            shapeRenderer.circle(c.x, c.y, gFrente * 0.55f);
        }
    }

    private void dibujarElectronesEnRuta(List<Vector2> ruta, float tiempo) {
        if (ruta == null || ruta.size() < 2) return;
        float total = 0f;
        float[] dists = new float[ruta.size() - 1];
        for (int i = 0; i < ruta.size() - 1; i++) {
            dists[i] = ruta.get(i).dst(ruta.get(i + 1));
            total += dists[i];
        }
        if (total <= 1f) return;

        for (int e = 0; e < 2; e++) {
            float distDeseada = ((tiempo * 0.6f + e * 0.5f) % 1.0f) * total;
            float acum = 0f;
            Vector2 pos = ruta.get(0);
            float ang = 0f;

            for (int i = 0; i < ruta.size() - 1; i++) {
                if (distDeseada <= acum + dists[i] || i == ruta.size() - 2) {
                    float f = dists[i] > 0.001f ? MathUtils.clamp((distDeseada - acum) / dists[i], 0f, 1f) : 0f;
                    Vector2 va = ruta.get(i), vb = ruta.get(i + 1);
                    pos = va.cpy().lerp(vb, f);
                    ang = MathUtils.atan2(vb.y - va.y, vb.x - va.x);
                    break;
                }
                acum += dists[i];
            }
            dibujarElectronCaricatura(pos.x, pos.y, ang, 0.9f, tiempo + e);
        }
    }

    public List<Vector2> calcularRutaOrtogonal(Cable cable, Terminal tOrigen, Terminal tDestino) {
        if (tOrigen == null || tDestino == null) return new ArrayList<>();
        Vector2 p1 = tOrigen.getPosicionAbsoluta(), p2 = tDestino.getPosicionAbsoluta();
        boolean origLed = (tOrigen.getComponentePadre() instanceof LED), destLed = (tDestino.getComponentePadre() instanceof LED);

        if (cable != null && cable.tieneCodoPersonalizado()) {
            return calcularPuntosOrtogonalesConCodoManual(p1, p2, cable.getCodoPersonalizadoX(), cable.getCodoPersonalizadoY(), origLed, destLed);
        }

        int idx = (nivel != null && cable != null) ? nivel.getCables().indexOf(cable) : 0;
        float desfase = 0f;
        if (!origLed && !destLed) {
            if (idx == 1) desfase = 32f;
            else if (idx == 2) desfase = -32f;
            else if (idx > 2) desfase = ((idx % 2 == 0) ? -1f : 1f) * (32f + (idx / 2) * 16f);
        }
        return calcularPuntosOrtogonales(p1, p2, origLed, destLed, desfase);
    }

    public List<Vector2> calcularRutaOrtogonal(Terminal tOrigen, Terminal tDestino) {
        return calcularRutaOrtogonal(null, tOrigen, tDestino);
    }

    public List<Vector2> calcularRutaOrtogonalArrastre(Terminal tOrigen, Vector2 pDestino) {
        if (tOrigen == null || pDestino == null) return new ArrayList<>();
        boolean origLed = (tOrigen.getComponentePadre() instanceof LED);
        boolean destLed = (terminalBajoRaton != null && terminalBajoRaton.getComponentePadre() instanceof LED);
        return calcularPuntosOrtogonales(tOrigen.getPosicionAbsoluta(), pDestino, origLed, destLed, 0f);
    }

    private List<Vector2> calcularPuntosOrtogonalesConCodoManual(Vector2 p1, Vector2 p2, float cx, float cy, boolean origVert, boolean destVert) {
        List<Vector2> ruta = new ArrayList<>();
        ruta.add(new Vector2(p1.x, p1.y));
        if (origVert) {
            ruta.add(new Vector2(p1.x, cy));
            if (Math.abs(p2.x - p1.x) > 1f) ruta.add(new Vector2(p2.x, cy));
            ruta.add(new Vector2(p2.x, p2.y));
        } else {
            ruta.add(new Vector2(cx, p1.y));
            if (Math.abs(p2.y - p1.y) > 1f) ruta.add(new Vector2(cx, p2.y));
            ruta.add(new Vector2(p2.x, p2.y));
        }
        return simplificarPuntosConsecutivos(ruta);
    }

    private List<Vector2> calcularPuntosOrtogonales(Vector2 p1, Vector2 p2, boolean origVert, boolean destVert, float desfase) {
        List<Vector2> ruta = new ArrayList<>();
        ruta.add(new Vector2(p1.x, p1.y));
        float dx = p2.x - p1.x, dy = p2.y - p1.y;
        if (Math.abs(dx) + Math.abs(dy) < 1f) {
            ruta.add(new Vector2(p2.x, p2.y));
            return ruta;
        }

        float ang = Math.abs(MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
        if (ang > 90f) ang = 180f - ang;

        if (ang <= 6f || Math.abs(dy) <= 8f) {
            ruta.add(new Vector2(p2.x, p1.y));
            return ruta;
        }
        if (ang >= 84f || Math.abs(dx) <= 8f) {
            ruta.add(new Vector2(p1.x, p2.y));
            return ruta;
        }

        if (origVert) {
            float codoY = p2.y + desfase;
            ruta.add(new Vector2(p1.x, codoY));
            ruta.add(new Vector2(p2.x, codoY));
        } else if (destVert) {
            float codoX = p2.x + desfase;
            ruta.add(new Vector2(codoX, p1.y));
            ruta.add(new Vector2(codoX, p2.y));
        } else {
            if (Math.abs(dx) >= Math.abs(dy)) {
                float codoX = (p1.x + p2.x) * 0.5f + desfase;
                ruta.add(new Vector2(codoX, p1.y));
                ruta.add(new Vector2(codoX, p2.y));
            } else {
                float codoY = (p1.y + p2.y) * 0.5f + desfase;
                ruta.add(new Vector2(p1.x, codoY));
                ruta.add(new Vector2(p2.x, codoY));
            }
        }
        ruta.add(new Vector2(p2.x, p2.y));
        return simplificarPuntosConsecutivos(ruta);
    }

    private List<Vector2> simplificarPuntosConsecutivos(List<Vector2> entrada) {
        List<Vector2> limpia = new ArrayList<>();
        if (entrada == null || entrada.isEmpty()) return limpia;
        limpia.add(entrada.get(0));
        for (int i = 1; i < entrada.size(); i++) {
            Vector2 actual = entrada.get(i);
            if (actual.dst(limpia.get(limpia.size() - 1)) > 1f) limpia.add(actual);
        }
        return limpia;
    }

    public Cable obtenerCableCodoBajoRaton(float mx, float my, float radio) {
        if (nivel == null) return null;
        for (Cable cable : nivel.getCables()) {
            Terminal t1 = cable.getTerminalOrigen(), t2 = cable.getTerminalDestino();
            if (t1 == null || t2 == null) continue;
            List<Vector2> ruta = calcularRutaOrtogonal(cable, t1, t2);
            for (int i = 1; i < ruta.size() - 1; i++) {
                if (ruta.get(i).dst(mx, my) <= radio) return cable;
            }
        }
        return null;
    }

    private void dibujarTerminalesFisicos() {
        if (nivel == null) return;
        for (Componente comp : nivel.getComponentes()) {
            boolean esComp = (comp instanceof CompuertaLogica);
            float rExt = esComp ? 11.5f : 14.5f, rCol = esComp ? 8.5f : 11f, rCen = esComp ? 4.0f : 5.0f;

            for (Terminal t : comp.getTerminales()) {
                Vector2 pos = t.getPosicionAbsoluta();
                boolean hov = (terminalBajoRaton == t);
                boolean posId = t.getIdentificador().contains("POSITIVO") || t.getIdentificador().contains("ANODO");
                boolean negId = t.getIdentificador().contains("NEGATIVO") || t.getIdentificador().contains("CATODO");

                shapeRenderer.setColor(0.12f, 0.15f, 0.20f, 1f);
                shapeRenderer.circle(pos.x, pos.y, rExt);

                if (posId) shapeRenderer.setColor(0.95f, 0.20f, 0.20f, 1f);
                else if (negId) shapeRenderer.setColor(0.20f, 0.55f, 0.95f, 1f);
                else if (t.getValorLogico()) shapeRenderer.setColor(0.20f, 0.95f, 0.50f, 1f);
                else shapeRenderer.setColor(0.80f, 0.85f, 0.95f, 1f);
                shapeRenderer.circle(pos.x, pos.y, rCol);

                shapeRenderer.setColor(0.05f, 0.07f, 0.10f, 1f);
                shapeRenderer.circle(pos.x, pos.y, rCen);

                if (hov) {
                    float pulso = 1f + 0.22f * MathUtils.sin(tiempoTotal * 12f);
                    shapeRenderer.setColor(0.20f, 1.00f, 0.40f, 0.45f);
                    shapeRenderer.circle(pos.x, pos.y, (rExt + 9f) * pulso);
                }
                if (t.estaConectado()) {
                    shapeRenderer.setColor(0.00f, 0.95f, 1.00f, 0.30f);
                    shapeRenderer.circle(pos.x, pos.y, rExt + 3f);
                }
                if (nivel.buscarCablesConTerminal(t).size() > 1) {
                    shapeRenderer.setColor(1.00f, 0.85f, 0.20f, 0.40f);
                    shapeRenderer.circle(pos.x, pos.y, rExt + 5f);
                    shapeRenderer.setColor(1.00f, 0.90f, 0.30f, 0.85f);
                    shapeRenderer.circle(pos.x, pos.y, 4f);
                }
            }
        }
    }

    private void dibujarComponentesSprites() {
        if (nivel == null) return;
        for (Componente comp : nivel.getComponentes()) {
            float x = comp.getPosicionX(), y = comp.getPosicionY(), w = comp.getAncho(), h = comp.getAlto();

            if (comp instanceof LED) {
                LED led = (LED) comp;
                TextureRegion f = led.estaQuemado() ? animLedMuriendo.getKeyFrame(tiempoAnimError, false)
                        : ("EXITO".equals(led.getEstadoActual()) ? animLedPrendiendo.getKeyFrame(tiempoTotal, true) : animLedNormal.getKeyFrame(tiempoTotal, true));
                batch.draw(f, x, y, w, h);
                fontPequena.setColor(1f, 0.42f, 0.42f, 0.90f); fontPequena.draw(batch, "+", x + 54f, y + 88f);
                fontPequena.setColor(0.42f, 0.78f, 1f, 0.90f); fontPequena.draw(batch, "-", x + 174f, y + 88f);
                fontPequena.setColor(COLOR_GRIS_CLARO); glyphLayout.setText(fontPequena, "LED");
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h - 6f);
            } else if (comp instanceof Resistencia) {
                Resistencia res = (Resistencia) comp;
                TextureRegion f = "ERROR".equals(res.getEstadoActual()) ? animResistenciaFallo.getKeyFrame(tiempoAnimError, true) : animResistenciaNormal.getKeyFrame(tiempoTotal, true);
                batch.draw(f, x, y, w, h);
                fontPequena.setColor(COLOR_GRIS_CLARO); glyphLayout.setText(fontPequena, res.getTextoEtiqueta());
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h + 8f);
            } else if (comp instanceof CompuertaLogica) {
                CompuertaLogica gate = (CompuertaLogica) comp;
                boolean esAnd = (comp instanceof CompuertaAND);
                batch.draw((esAnd ? animAnd : animOr).getKeyFrame(tiempoTotal, true), x, y, w, h);
                boolean valA = gate.getTerminalEntradaA() != null && gate.getTerminalEntradaA().getValorLogico();
                boolean valB = gate.getTerminalEntradaB() != null && gate.getTerminalEntradaB().getValorLogico();
                boolean valSal = gate.getTerminalSalida() != null && gate.getTerminalSalida().getValorLogico();
                fontPequena.setColor(valA ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO); fontPequena.draw(batch, "A", x + 14f, y + 172f);
                fontPequena.setColor(valB ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO); fontPequena.draw(batch, "B", x + 14f, y + 134f);
                fontPequena.setColor(valSal ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO);
                glyphLayout.setText(fontPequena, esAnd ? "AND" : "OR");
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h - 8f);
            } else if (comp instanceof Switch) {
                Switch sw = (Switch) comp;
                if (sw.isDoble()) {
                    fontPequena.setColor(sw.isCerradoA() ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO); fontPequena.draw(batch, "A", x + w - 22f, y + 164f);
                    fontPequena.setColor(sw.isCerradoB() ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO); fontPequena.draw(batch, "B", x + w - 22f, y + 26f);
                    fontPequena.setColor((sw.isCerradoA() || sw.isCerradoB()) ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO);
                } else {
                    fontPequena.setColor(sw.isCerrado() ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO);
                }
                glyphLayout.setText(fontPequena, "SWITCH");
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h + 18f);
            }
        }
    }

    private void dibujarBotonGuiaSuperiorShape() {
        float bx = 1430f, by = ALTO_VIRTUAL - 66f, bw = 160f, bh = 38f;
        boolean hov = (ratonMundo.x >= bx && ratonMundo.x <= bx + bw && ratonMundo.y >= by && ratonMundo.y <= by + bh);
        dibujarCajaBorde(bx, by, bw, bh, hov ? new Color(0.08f, 0.16f, 0.26f, 0.98f) : COLOR_SLATE_OSCURO,
                hov ? COLOR_CELESTE_BRILLANTE : COLOR_NEON_CELESTE, hov ? 2.2f : 1.6f);
    }

    private float calcularXPasoGuia(int i, int n) {
        float wPill = 162f, gap = 22f;
        float xInicio = (1620f - (n * wPill + (n - 1) * gap)) * 0.5f;
        return xInicio + i * (wPill + gap);
    }

    private void dibujarPasosGuiaHUDShapes() {
        if (nivel == null) return;
        String[] pasos = nivel.obtenerPasosGuia();
        boolean[] estados = nivel.obtenerEstadoPasosGuia();
        if (pasos == null || estados == null || pasos.length == 0) return;

        int n = pasos.length, pasoActual = -1;
        for (int i = 0; i < estados.length; i++) {
            if (!estados[i]) { pasoActual = i; break; }
        }
        float wPill = 162f, hPill = 28f, gap = 22f, yPill = 72f, cyPill = yPill + hPill * 0.5f;

        for (int i = 0; i < n; i++) {
            float px = calcularXPasoGuia(i, n);
            boolean listo = (i < estados.length && estados[i]);
            boolean esSig = (i == pasoActual);

            if (i < n - 1) {
                shapeRenderer.setColor(listo ? COLOR_NEON_CELESTE : COLOR_GRIS_METAL);
                shapeRenderer.rectLine(px + wPill, cyPill, px + wPill + gap, cyPill, 1.8f);
            }

            Color fondo = esSig ? new Color(0.06f, 0.11f, 0.19f, 0.95f) : new Color(0.035f, 0.055f, 0.095f, 0.90f);
            Color borde = listo ? COLOR_NEON_CELESTE : (esSig ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
            dibujarCajaBorde(px, yPill, wPill, hPill, fondo, borde, esSig ? 1.9f : 1.2f);

            float dotX = px + 14f;
            if (listo) {
                shapeRenderer.setColor(0.15f, 0.95f, 0.72f, 1f);
                shapeRenderer.circle(dotX, cyPill, 4.2f);
            } else if (esSig) {
                shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f);
                shapeRenderer.circle(dotX, cyPill, 7.2f);
                shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE);
                shapeRenderer.circle(dotX, cyPill, 4.0f);
            } else {
                shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.45f);
                shapeRenderer.circle(dotX, cyPill, 3.2f);
            }
        }
    }

    private void dibujarPasosGuiaHUDTextos() {
        if (nivel == null) return;
        String[] pasos = nivel.obtenerPasosGuia();
        boolean[] estados = nivel.obtenerEstadoPasosGuia();
        if (pasos == null || estados == null || pasos.length == 0) return;

        int n = pasos.length, pasoActual = -1;
        for (int i = 0; i < estados.length; i++) {
            if (!estados[i]) { pasoActual = i; break; }
        }
        float wPill = 162f, hPill = 28f, yPill = 72f;

        for (int i = 0; i < n; i++) {
            float px = calcularXPasoGuia(i, n);
            boolean listo = (i < estados.length && estados[i]);
            boolean esSig = (i == pasoActual);

            fontPequena.setColor(listo ? COLOR_CELESTE_BRILLANTE : (esSig ? Color.WHITE : new Color(COLOR_GRIS_CLARO.r, COLOR_GRIS_CLARO.g, COLOR_GRIS_CLARO.b, 0.55f)));
            glyphLayout.setText(fontPequena, pasos[i]);
            fontPequena.draw(batch, pasos[i], px + 26f + Math.max(0f, (wPill - 32f - glyphLayout.width) * 0.5f), yPill + (hPill + glyphLayout.height) * 0.5f);
        }
    }

    private void dibujarTextosHUD() {
        FuenteAlimentacion fuente = null;
        if (nivel != null) {
            for (Componente c : nivel.getComponentes()) {
                if (c instanceof FuenteAlimentacion) { fuente = (FuenteAlimentacion) c; break; }
            }
        }
        if (fuente != null) {
            float fx = fuente.getPosicionX(), fy = fuente.getPosicionY(), fw = fuente.getAncho(), fh = fuente.getAlto();
            fontTitulo.setColor(0.00f, 0.95f, 1.00f, 1f);
            fontTitulo.draw(batch, String.format("%.2f V", voltajeMedidorDC), fx + 38f, fy + fh - 52f);
            float corriente = (nivel != null && (nivel.estaGanado() || nivel.getTiempoCircuitoActivo() > 0)) ? 0.02f : 0.00f;
            font.setColor(COLOR_GRIS_CLARO);
            font.draw(batch, String.format("%.2f A", corriente), fx + 42f, fy + fh - 95f);
            fontPequena.setColor(COLOR_GRIS_CLARO);
            fontPequena.draw(batch, "FUENTE DC", fx + 62f, fy + fh - 159f);
            fontTitulo.setColor(0.95f, 0.30f, 0.30f, 1f); fontTitulo.draw(batch, "+", fx + fw - 56f, fy + fh * 0.70f + 12f);
            fontTitulo.setColor(0.30f, 0.68f, 1.0f, 1f); fontTitulo.draw(batch, "-", fx + fw - 52f, fy + fh * 0.30f + 12f);
        }

        fontTitulo.setColor(COLOR_CELESTE_BRILLANTE);
        fontTitulo.draw(batch, nivel != null ? nivel.getTituloReto() : "NIVEL 1", 40f, ALTO_VIRTUAL - 24f);

        if (nivel != null) {
            float tRest = nivel.getTiempoRestante();
            fontTitulo.setColor(tRest <= 10f ? new Color(1f, 0.32f, 0.32f, 1f) : COLOR_CELESTE_BRILLANTE);
            String txtT = String.format("%.1fs", tRest);
            glyphLayout.setText(fontTitulo, txtT);
            fontTitulo.draw(batch, txtT, 1405f - glyphLayout.width, ALTO_VIRTUAL - 34f);

            font.setColor(nivel.estaGanado() ? new Color(0.20f, 0.95f, 0.70f, 1f) : (nivel.getTiempoCircuitoActivo() > 0 ? COLOR_CELESTE_BRILLANTE : (hayErrorLED() ? new Color(1f, 0.35f, 0.35f, 1f) : COLOR_GRIS_CLARO)));
            font.draw(batch, nivel.getMensajeEstado(), 40f, ALTO_VIRTUAL - 60f);

            float activo = nivel.getTiempoCircuitoActivo();
            fontPequena.setColor(nivel.estaGanado() ? new Color(0.20f, 0.95f, 0.70f, 1f) : (activo > 0 ? COLOR_CELESTE_BRILLANTE : new Color(COLOR_GRIS_CLARO.r, COLOR_GRIS_CLARO.g, COLOR_GRIS_CLARO.b, 0.65f)));
            fontPequena.draw(batch, String.format("%.1f / %.0fs", activo, nivel.getTiempoRequeridoVictoria()), 312f, ALTO_VIRTUAL - 101f);
        }

        fontPequena.setColor(COLOR_CELESTE_BRILLANTE);
        glyphLayout.setText(fontPequena, "?  GUÍA (H)");
        fontPequena.draw(batch, glyphLayout, 1430f + (160f - glyphLayout.width) / 2f, ALTO_VIRTUAL - 66f + (38f + glyphLayout.height) / 2f);
        fontPequena.setColor(COLOR_GRIS_CLARO.r, COLOR_GRIS_CLARO.g, COLOR_GRIS_CLARO.b, 0.68f);
        fontPequena.draw(batch, "Clic Izq: Conectar / Switch   ·   Clic Der: Borrar   ·   H: Guía   ·   R: Reiniciar   ·   ESC: Menú", 40f, 36f);
    }

    private void dibujarGuiaNivelShapes() {
        shapeRenderer.setColor(0.01f, 0.02f, 0.04f, 0.84f);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        float mx = 500f, my = 270f, mw = 920f, mh = 540f;
        shapeRenderer.setColor(0f, 0f, 0f, 0.72f);
        shapeRenderer.rect(mx + 10f, my - 10f, mw, mh);
        dibujarCajaBorde(mx, my, mw, mh, new Color(0.04f, 0.06f, 0.10f, 0.98f), COLOR_NEON_CELESTE, 2.4f);

        shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE);
        float brk = 18f;
        shapeRenderer.rectLine(mx, my, mx + brk, my, 3.8f);
        shapeRenderer.rectLine(mx, my, mx, my + brk, 3.8f);
        shapeRenderer.rectLine(mx + mw, my, mx + mw - brk, my, 3.8f);
        shapeRenderer.rectLine(mx + mw, my, mx + mw, my + brk, 3.8f);
        shapeRenderer.rectLine(mx, my + mh, mx + brk, my + mh, 3.8f);
        shapeRenderer.rectLine(mx, my + mh, mx, my + mh - brk, 3.8f);
        shapeRenderer.rectLine(mx + mw, my + mh, mx + mw - brk, my + mh, 3.8f);
        shapeRenderer.rectLine(mx + mw, my + mh, mx + mw, my + mh - brk, 3.8f);

        shapeRenderer.setColor(0.06f, 0.10f, 0.16f, 0.95f);
        shapeRenderer.rect(mx + 2f, my + mh - 56f, mw - 4f, 54f);
        shapeRenderer.setColor(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.45f);
        shapeRenderer.rectLine(mx + 2f, my + mh - 56f, mx + mw - 2f, my + mh - 56f, 1.4f);

        float vx = mx + 36f, vy = my + 226f, vw = mw - 72f, vh = 236f;
        dibujarCajaBorde(vx, vy, vw, vh, new Color(0.018f, 0.030f, 0.055f, 0.98f),
                new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.55f), 1.5f);

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.06f);
        for (float gx = vx + 28f; gx < vx + vw; gx += 28f) shapeRenderer.rectLine(gx, vy, gx, vy + vh, 1f);
        for (float gy = vy + 28f; gy < vy + vh; gy += 28f) shapeRenderer.rectLine(vx, gy, vx + vw, gy, 1f);

        int num = (nivel != null) ? nivel.getNumeroNivel() : 1;
        float cy = vy + 128f, yRet = vy + 28f;
        float fx = vx + 44f, fy = vy + 46f, fw = 68f, fh = 148f;
        dibujarCajaBorde(fx, fy, fw, fh, new Color(0.11f, 0.15f, 0.22f, 1f), COLOR_GRIS_METAL, 1.8f);
        shapeRenderer.setColor(0.04f, 0.06f, 0.09f, 1f);
        shapeRenderer.rect(fx + 10f, fy + fh - 40f, fw - 20f, 26f);

        float posPinX = fx + fw, posPinY = cy + 24f, negPinY = vy + 66f;

        if (num >= 3) {
            float swX = vx + 166f, swY = vy + 80f, swW = 90f, swH = 96f, yA = cy + 22f, yB = cy - 22f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(posPinX, posPinY, vx + 138f, posPinY, 3.2f);
            shapeRenderer.rectLine(vx + 138f, posPinY, vx + 138f, cy, 3.2f);
            shapeRenderer.rectLine(vx + 138f, cy, swX, cy, 3.2f);

            dibujarCajaBorde(swX, swY, swW, swH, new Color(0.06f, 0.09f, 0.14f, 1f), COLOR_GRIS_METAL, 1.6f);
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(swX, cy, swX + 24f, cy, 2.8f);
            shapeRenderer.rectLine(swX + 24f, yB, swX + 24f, yA, 2.8f);
            shapeRenderer.setColor(0.15f, 0.95f, 0.72f, 1f);
            shapeRenderer.rectLine(swX + 24f, yA, swX + swW, yA, 3.2f);
            boolean cerrBGuia = (num == 3);
            if (cerrBGuia) {
                shapeRenderer.rectLine(swX + 24f, yB, swX + swW, yB, 3.2f);
            } else {
                shapeRenderer.setColor(0.50f, 0.62f, 0.76f, 1f);
                shapeRenderer.rectLine(swX + 24f, yB, swX + swW - 14f, yB + 11f, 3.0f);
            }

            float gateInX = vx + 325f, gateInAY = vy + 138f, gateInBY = vy + 121f, gateOutX = vx + 411f, gateOutY = vy + 130f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(swX + swW, yA, vx + 290f, yA, 3.0f);
            shapeRenderer.rectLine(vx + 290f, yA, vx + 290f, gateInAY, 3.0f);
            shapeRenderer.rectLine(vx + 290f, gateInAY, gateInX, gateInAY, 3.0f);

            shapeRenderer.setColor(cerrBGuia ? COLOR_NEON_CELESTE : new Color(0.32f, 0.48f, 0.64f, 0.9f));
            shapeRenderer.rectLine(swX + swW, yB, vx + 282f, yB, 3.0f);
            shapeRenderer.rectLine(vx + 282f, yB, vx + 282f, gateInBY, 3.0f);
            shapeRenderer.rectLine(vx + 282f, gateInBY, gateInX, gateInBY, 3.0f);

            float resInX = vx + 488f, resOutX = vx + 584f, resY = vy + 132f, ledAnodoX = vx + 696f, ledCatodoX = vx + 728f, ledLegY = vy + 97f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(gateOutX, gateOutY, resInX, resY, 3.2f);
            shapeRenderer.rectLine(resOutX, resY, vx + 636f, resY, 3.2f);
            shapeRenderer.rectLine(vx + 636f, resY, vx + 636f, ledLegY, 3.2f);
            shapeRenderer.rectLine(vx + 636f, ledLegY, ledAnodoX, ledLegY, 3.2f);

            shapeRenderer.setColor(0.20f, 0.68f, 1.0f, 0.90f);
            shapeRenderer.rectLine(ledCatodoX, ledLegY, ledCatodoX, yRet, 3.0f);
            shapeRenderer.rectLine(ledCatodoX, yRet, vx + 138f, yRet, 3.0f);
            shapeRenderer.rectLine(vx + 138f, yRet, vx + 138f, negPinY, 3.0f);
            shapeRenderer.rectLine(vx + 138f, negPinY, posPinX, negPinY, 3.0f);

            dibujarPulsoCableGuia(posPinX, posPinY, swX, cy, 0.0f);
            dibujarPulsoCableGuia(swX + swW, yA, gateInX, gateInAY, 0.25f);
            dibujarPulsoCableGuia(gateOutX, gateOutY, resInX, resY, 0.50f);
            dibujarPulsoCableGuia(resOutX, resY, ledAnodoX, ledLegY, 0.72f);
            dibujarPulsoCableGuia(ledCatodoX, yRet, vx + 138f, yRet, 0.40f);
        } else {
            float resInX = (num == 2) ? vx + 390f : vx + 290f, resOutX = resInX + 96f, resY = vy + 132f, ledAnodoX = vx + 656f, ledCatodoX = vx + 688f, ledLegY = vy + 97f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            if (num == 2) {
                float swX = vx + 195f, swY = cy - 26f;
                dibujarCajaBorde(swX, swY, 100f, 52f, new Color(0.06f, 0.09f, 0.14f, 1f), COLOR_GRIS_METAL, 1.6f);
                shapeRenderer.setColor(0.15f, 0.95f, 0.72f, 1f);
                shapeRenderer.rectLine(swX + 16f, cy, swX + 84f, cy, 4.0f);
                shapeRenderer.setColor(COLOR_NEON_CELESTE);
                shapeRenderer.rectLine(posPinX, posPinY, vx + 150f, posPinY, 3.2f);
                shapeRenderer.rectLine(vx + 150f, posPinY, vx + 150f, cy, 3.2f);
                shapeRenderer.rectLine(vx + 150f, cy, swX, cy, 3.2f);
                shapeRenderer.rectLine(swX + 100f, cy, resInX, resY, 3.2f);
            } else {
                shapeRenderer.rectLine(posPinX, posPinY, vx + 185f, posPinY, 3.2f);
                shapeRenderer.rectLine(vx + 185f, posPinY, vx + 185f, resY, 3.2f);
                shapeRenderer.rectLine(vx + 185f, resY, resInX, resY, 3.2f);
            }

            shapeRenderer.rectLine(resOutX, resY, vx + 585f, resY, 3.2f);
            shapeRenderer.rectLine(vx + 585f, resY, vx + 585f, ledLegY, 3.2f);
            shapeRenderer.rectLine(vx + 585f, ledLegY, ledAnodoX, ledLegY, 3.2f);

            shapeRenderer.setColor(0.20f, 0.68f, 1.0f, 0.90f);
            shapeRenderer.rectLine(ledCatodoX, ledLegY, ledCatodoX, yRet, 3.0f);
            shapeRenderer.rectLine(ledCatodoX, yRet, vx + 138f, yRet, 3.0f);
            shapeRenderer.rectLine(vx + 138f, yRet, vx + 138f, negPinY, 3.0f);
            shapeRenderer.rectLine(vx + 138f, negPinY, posPinX, negPinY, 3.0f);

            dibujarPulsoCableGuia(posPinX, posPinY, resInX, resY, 0.1f);
            dibujarPulsoCableGuia(resOutX, resY, ledAnodoX, ledLegY, 0.55f);
            dibujarPulsoCableGuia(ledCatodoX, yRet, vx + 138f, yRet, 0.35f);
        }

        shapeRenderer.setColor(0.95f, 0.25f, 0.25f, 1f); shapeRenderer.circle(posPinX, posPinY, 5.5f);
        shapeRenderer.setColor(0.25f, 0.62f, 1.0f, 1f); shapeRenderer.circle(posPinX, negPinY, 5.5f);

        if (num == 3 || num == 4) {
            boolean esOr = (num == 4);
            boolean[][] filas = {{false, false, false}, {true, false, esOr}, {false, true, esOr}, {true, true, true}};
            float pw = 198f, ph = 62f, gap = (vw - 4f * pw) / 3f, py = my + 102f;
            for (int r = 0; r < 4; r++) {
                float px = vx + r * (pw + gap), dotY = py + ph * 0.5f;
                boolean a = filas[r][0], b = filas[r][1], enc = filas[r][2];
                dibujarCajaBorde(px, py, pw, ph, enc ? new Color(0.05f, 0.12f, 0.19f, 0.96f) : new Color(0.025f, 0.04f, 0.07f, 0.92f),
                        enc ? COLOR_NEON_CELESTE : new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.45f), enc ? 1.9f : 1.1f);
                shapeRenderer.setColor(a ? new Color(0.15f, 0.95f, 0.72f, 1f) : new Color(0.22f, 0.28f, 0.38f, 1f)); shapeRenderer.circle(px + 38f, dotY, 6f);
                shapeRenderer.setColor(b ? new Color(0.15f, 0.95f, 0.72f, 1f) : new Color(0.22f, 0.28f, 0.38f, 1f)); shapeRenderer.circle(px + 84f, dotY, 6f);
                dibujarFlechaHorizontal(px + 104f, px + 128f, dotY, enc ? COLOR_CELESTE_BRILLANTE : new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.65f));
                if (enc) {
                    shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f); shapeRenderer.circle(px + 145f, dotY, 10.5f);
                    shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE); shapeRenderer.circle(px + 145f, dotY, 6f);
                } else {
                    shapeRenderer.setColor(0.20f, 0.26f, 0.35f, 1f); shapeRenderer.circle(px + 145f, dotY, 5.5f);
                }
            }
        } else {
            float pw = 408f, ph = 54f, py = my + 106f;
            dibujarCajaBorde(vx, py, pw, ph, new Color(0.03f, 0.06f, 0.11f, 0.95f), new Color(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.65f), 1.4f);
            dibujarCajaBorde(vx + pw + 32f, py, pw, ph, new Color(0.03f, 0.06f, 0.11f, 0.95f), new Color(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.65f), 1.4f);
        }

        float btnW = 250f, btnH = 46f, btnX = mx + (mw - btnW) / 2f, btnY = my + 24f;
        boolean hovBtn = (ratonMundo.x >= btnX && ratonMundo.x <= btnX + btnW && ratonMundo.y >= btnY && ratonMundo.y <= btnY + btnH);
        dibujarCajaBorde(btnX, btnY, btnW, btnH, hovBtn ? new Color(0.08f, 0.17f, 0.28f, 1f) : COLOR_SLATE_OSCURO,
                hovBtn ? COLOR_CELESTE_BRILLANTE : COLOR_NEON_CELESTE, hovBtn ? 2.4f : 1.8f);
    }

    private void dibujarGuiaNivelTextos() {
        float mx = 500f, my = 270f, mw = 920f, mh = 540f;
        int num = (nivel != null) ? nivel.getNumeroNivel() : 1;

        String tituloGuia = num == 3 ? "NIVEL 3  ·  COMPUERTA AND" : (num == 4 ? "NIVEL 4  ·  COMPUERTA OR" : (num == 2 ? "NIVEL 2  ·  INTERRUPTOR" : "NIVEL 1  ·  CIRCUITO LED"));
        fontTitulo.setColor(COLOR_CELESTE_BRILLANTE);
        fontTitulo.draw(batch, tituloGuia, mx + 28f, my + mh - 18f);

        fontPequena.setColor(COLOR_GRIS_CLARO.r, COLOR_GRIS_CLARO.g, COLOR_GRIS_CLARO.b, 0.78f);
        glyphLayout.setText(fontPequena, "PAUSA");
        fontPequena.draw(batch, "PAUSA", mx + mw - glyphLayout.width - 28f, my + mh - 23f);

        float vx = mx + 36f, vy = my + 226f, vw = mw - 72f;
        fontPequena.setColor(COLOR_CELESTE_BRILLANTE); fontPequena.draw(batch, "5V", vx + 64f, vy + 172f);
        fontPequena.setColor(1f, 0.45f, 0.45f, 1f); fontPequena.draw(batch, "+", vx + 94f, vy + 166f);
        fontPequena.setColor(0.45f, 0.78f, 1f, 1f); fontPequena.draw(batch, "-", vx + 96f, vy + 78f);

        if (num >= 3) {
            boolean esOr = (num == 4);
            batch.draw((esOr ? animOr : animAnd).getKeyFrame(tiempoTotal, true), vx + 304f, vy + 64f, 128f, 128f);
            batch.draw(animResistenciaNormal.getKeyFrame(tiempoTotal, true), vx + 470f, vy + 90f, 132f, 84f);
            batch.draw(animLedPrendiendo.getKeyFrame(tiempoTotal, true), vx + 650f, vy + 66f, 126f, 126f);

            fontPequena.setColor(COLOR_GRIS_CLARO);
            fontPequena.draw(batch, "SWITCH", vx + 183f, vy + 194f);
            fontPequena.draw(batch, esOr ? "OR" : "AND", vx + 354f, vy + 202f);
            fontPequena.draw(batch, "220 Ohm", vx + 506f, vy + 190f);
            fontPequena.draw(batch, "LED", vx + 699f, vy + 202f);

            fontPequena.setColor(COLOR_CELESTE_BRILLANTE);
            fontPequena.draw(batch, "A", vx + 260f, vy + 165f);
            fontPequena.draw(batch, "B", vx + 260f, vy + 100f);
            fontPequena.setColor(1f, 0.45f, 0.45f, 0.9f); fontPequena.draw(batch, "+", vx + 676f, vy + 106f);
            fontPequena.setColor(0.45f, 0.78f, 1f, 0.9f); fontPequena.draw(batch, "-", vx + 738f, vy + 106f);
        } else {
            float resX = (num == 2) ? vx + 372f : vx + 272f;
            batch.draw(animResistenciaNormal.getKeyFrame(tiempoTotal, true), resX, vy + 90f, 132f, 84f);
            batch.draw(animLedPrendiendo.getKeyFrame(tiempoTotal, true), vx + 610f, vy + 66f, 126f, 126f);
            fontPequena.setColor(COLOR_GRIS_CLARO);
            if (num == 2) fontPequena.draw(batch, "SWITCH", vx + 216f, vy + 172f);
            fontPequena.draw(batch, "220 Ohm", resX + 36f, vy + 190f);
            fontPequena.draw(batch, "LED", vx + 659f, vy + 202f);
        }

        String regla = num == 3 ? "Conecta el esquema y activa AMBAS palancas (A y B) por 5s"
                : (num == 4 ? "Conecta el esquema y activa AL MENOS UNA palanca (A o B) por 5s"
                : (num == 2 ? "Conecta en serie y cierra el Switch (ON) por 5s" : "Conecta en serie usando la resistencia de 220 Ohm por 5s"));
        font.setColor(Color.WHITE);
        glyphLayout.setText(font, regla);
        font.draw(batch, regla, mx + (mw - glyphLayout.width) / 2f, my + 204f);

        if (num == 3 || num == 4) {
            boolean esOr = (num == 4);
            boolean[][] filas = {{false, false, false}, {true, false, esOr}, {false, true, esOr}, {true, true, true}};
            float pw = 198f, ph = 62f, gap = (vw - 4f * pw) / 3f, py = my + 102f;
            for (int r = 0; r < 4; r++) {
                float px = vx + r * (pw + gap), ty = py + ph * 0.5f + 5f;
                fontPequena.setColor(COLOR_GRIS_CLARO);
                fontPequena.draw(batch, "A", px + 16f, ty);
                fontPequena.draw(batch, "B", px + 62f, ty);
                fontPequena.setColor(filas[r][2] ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
                fontPequena.draw(batch, filas[r][2] ? "ON" : "OFF", px + 160f, ty);
            }
        } else {
            float pw = 408f, py = my + 106f;
            fontPequena.setColor(COLOR_CELESTE_BRILLANTE);
            String t1 = "Polaridad LED: entra por (+) y sale por (-)";
            glyphLayout.setText(fontPequena, t1);
            fontPequena.draw(batch, t1, vx + (pw - glyphLayout.width) / 2f, py + 33f);
            String t2 = "Protege siempre el LED con 220 Ohm";
            glyphLayout.setText(fontPequena, t2);
            fontPequena.draw(batch, t2, vx + pw + 32f + (pw - glyphLayout.width) / 2f, py + 33f);
        }

        float btnW = 250f, btnH = 46f, btnX = mx + (mw - btnW) / 2f, btnY = my + 24f;
        boolean hovBtn = (ratonMundo.x >= btnX && ratonMundo.x <= btnX + btnW && ratonMundo.y >= btnY && ratonMundo.y <= btnY + btnH);
        font.setColor(hovBtn ? Color.WHITE : COLOR_CELESTE_BRILLANTE);
        glyphLayout.setText(font, "ENTENDIDO");
        font.draw(batch, glyphLayout, btnX + (btnW - glyphLayout.width) / 2f, btnY + (btnH + glyphLayout.height) / 2f);
    }

    private void dibujarFondoModal() {
        shapeRenderer.setColor(0.04f, 0.05f, 0.07f, 0.80f);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        float mx = 560f, my = 320f, mw = 800f, mh = 440f;
        dibujarCajaBorde(mx, my, mw, mh, new Color(0.12f, 0.15f, 0.20f, 0.98f),
                (nivel != null && nivel.estaGanado()) ? new Color(0.15f, 0.85f, 0.45f, 1f) : new Color(0.95f, 0.25f, 0.25f, 1f), 4f);
        shapeRenderer.setColor(0.20f, 0.26f, 0.36f, 1f);
        shapeRenderer.rect(630f, 360f, 300f, 60f);
        shapeRenderer.rect(970f, 360f, 300f, 60f);
    }

    private void dibujarContenidoModal() {
        boolean ganado = (nivel != null && nivel.estaGanado());
        if (ganado) {
            fontTitulo.setColor(0.20f, 1.00f, 0.50f, 1f);
            glyphLayout.setText(fontTitulo, "¡NIVEL COMPLETADO!");
            fontTitulo.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 660f);
            font.setColor(0.95f, 0.95f, 1f, 1f);
            glyphLayout.setText(font, "¡Circuito completado con éxito!");
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 560f);
            font.setColor(0.20f, 0.95f, 0.45f, 1f);
            glyphLayout.setText(font, "SIGUIENTE");
            font.draw(batch, glyphLayout, 630f + (300f - glyphLayout.width) / 2f, 398f);
        } else {
            fontTitulo.setColor(1.00f, 0.25f, 0.25f, 1f);
            String tit = derrotaPorTiempo ? "¡TIEMPO AGOTADO!" : "¡CIRCUITO QUEMADO!";
            glyphLayout.setText(fontTitulo, tit);
            fontTitulo.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 660f);
            font.setColor(0.95f, 0.95f, 1f, 1f);
            String desc = derrotaPorTiempo ? "Se acabó el tiempo." : "El LED se quemó sin resistencia.";
            glyphLayout.setText(font, desc);
            font.draw(batch, glyphLayout, (ANCHO_VIRTUAL - glyphLayout.width) / 2f, 560f);
            font.setColor(1.00f, 0.40f, 0.40f, 1f);
            glyphLayout.setText(font, "REINTENTAR");
            font.draw(batch, glyphLayout, 630f + (300f - glyphLayout.width) / 2f, 398f);
        }
        font.setColor(ganado ? new Color(0.95f, 0.85f, 0.30f, 1f) : new Color(0.85f, 0.90f, 0.95f, 1f));
        glyphLayout.setText(font, "MENÚ");
        font.draw(batch, glyphLayout, 970f + (300f - glyphLayout.width) / 2f, 398f);
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
            float ang = MathUtils.random(0f, MathUtils.PI2), vel = MathUtils.random(60f, 260f);
            particulas.add(new ParticulaChispa(cx, cy, MathUtils.cos(ang) * vel, MathUtils.sin(ang) * vel,
                    MathUtils.random(0.3f, 0.8f), 0.1f, 0.9f, 1f));
        }
    }

    public Terminal obtenerTerminalBajoRaton(float mx, float my, float radio) {
        if (nivel == null) return null;
        Terminal masCercano = null;
        float menorDist = radio;
        for (Componente comp : nivel.getComponentes()) {
            for (Terminal t : comp.getTerminales()) {
                float dist = t.getPosicionAbsoluta().dst(mx, my);
                if (dist <= menorDist) {
                    menorDist = dist;
                    masCercano = t;
                }
            }
        }
        return masCercano;
    }

    public Componente obtenerComponenteBajoRaton(float mx, float my) {
        if (nivel == null) return null;
        List<Componente> lista = nivel.getComponentes();
        for (int i = lista.size() - 1; i >= 0; i--) {
            Componente comp = lista.get(i);
            if (mx >= comp.getPosicionX() && mx <= comp.getPosicionX() + comp.getAncho()
                    && my >= comp.getPosicionY() && my <= comp.getPosicionY() + comp.getAlto()) {
                return comp;
            }
        }
        return null;
    }

    private float distanciaPuntoASegmento(Vector2 a, Vector2 b, float px, float py) {
        float l2 = a.dst2(b);
        if (l2 == 0f) return a.dst(px, py);
        float t = Math.max(0f, Math.min(1f, ((px - a.x) * (b.x - a.x) + (py - a.y) * (b.y - a.y)) / l2));
        float projX = a.x + t * (b.x - a.x), projY = a.y + t * (b.y - a.y);
        return Vector2.dst(px, py, projX, projY);
    }

    public Cable obtenerCableBajoRaton(float mx, float my, float tolerancia) {
        if (nivel == null) return null;
        Cable mejorCable = null;
        float menorDist = tolerancia;
        for (Cable cable : nivel.getCables()) {
            Terminal t1 = cable.getTerminalOrigen(), t2 = cable.getTerminalDestino();
            if (t1 == null || t2 == null) continue;
            List<Vector2> ruta = calcularRutaOrtogonal(cable, t1, t2);
            for (int i = 0; i < ruta.size() - 1; i++) {
                float d = distanciaPuntoASegmento(ruta.get(i), ruta.get(i + 1), mx, my);
                if (d <= menorDist) {
                    menorDist = d;
                    mejorCable = cable;
                }
            }
        }
        return mejorCable;
    }

    private void sonar(TipoEfecto efecto) {
        if (gestorAudio != null) gestorAudio.reproducirEfecto(efecto);
    }

    public boolean estaEnModalDerrota() {
        return faseDerrota == FASE_DERROTA_MODAL;
    }

    public boolean estaMostrandoGuiaNivel() {
        return mostrandoGuiaNivel;
    }

    public void alternarGuiaNivel() {
        this.mostrandoGuiaNivel = !this.mostrandoGuiaNivel;
        if (!this.mostrandoGuiaNivel) saltarIntro();
        cancelarCable();
        sonar(TipoEfecto.CLICK_UI);
    }

    public void cerrarGuiaNivel() {
        if (this.mostrandoGuiaNivel) {
            this.mostrandoGuiaNivel = false;
            saltarIntro();
            sonar(TipoEfecto.CLICK_UI);
        }
    }

    public boolean manejarClicUI(float mx, float my) {
        boolean modalActivo = (nivel != null && ((nivel.estaGanado() && tiempoExitoAcumulado >= 1.2f) || faseDerrota == FASE_DERROTA_MODAL));
        if (modalActivo) {
            if (mx >= 630f && mx <= 930f && my >= 360f && my <= 420f) {
                sonar(TipoEfecto.CLICK_UI);
                if (nivel != null && nivel.estaGanado()) siguienteNivel();
                else reiniciarNivel();
                return true;
            }
            if (mx >= 970f && mx <= 1270f && my >= 360f && my <= 420f) {
                volverAlMenu();
                return true;
            }
            return true;
        }

        if (mostrandoGuiaNivel) {
            float gmx = 500f, gmy = 270f, gmw = 920f, gmh = 540f;
            float btnW = 250f, btnH = 46f, btnX = gmx + (gmw - btnW) / 2f, btnY = gmy + 24f;
            boolean clicBoton = (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH);
            boolean clicFuera = (mx < gmx || mx > gmx + gmw || my < gmy || my > gmy + gmh);
            if (clicBoton || clicFuera) cerrarGuiaNivel();
            return true;
        }

        if (mx >= 1430f && mx <= 1590f && my >= ALTO_VIRTUAL - 66f && my <= ALTO_VIRTUAL - 28f) {
            alternarGuiaNivel();
            return true;
        }
        return false;
    }

    public void iniciarCable(Terminal term, float mx, float my) {
        this.cableEnProgreso = true;
        this.terminalOrigenCable = term;
        this.ratonMundo.set(mx, my);
        sonar(TipoEfecto.CLICK_UI);
    }

    public void actualizarCableEnProgreso(float mx, float my) {
        this.ratonMundo.set(mx, my);
    }

    public void finalizarCable(Terminal termDestino) {
        if (cableEnProgreso && terminalOrigenCable != null && termDestino != null) {
            if (termDestino != terminalOrigenCable && termDestino.getComponentePadre() != terminalOrigenCable.getComponentePadre()) {
                boolean existe = false;
                for (Cable c : nivel.getCables()) {
                    if (c.conectaTerminal(terminalOrigenCable) && c.conectaTerminal(termDestino)) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    nivel.agregarCable(new Cable(terminalOrigenCable, termDestino));
                    generarChispasImpacto(termDestino.getPosicionAbsoluta().x, termDestino.getPosicionAbsoluta().y, 12);
                    sonar(TipoEfecto.CONECTAR_CABLE);
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
        this.terminalBajoRaton = mostrandoGuiaNivel ? null : obtenerTerminalBajoRaton(mx, my, 30f);

        boolean modalActivo = (nivel != null && ((nivel.estaGanado() && tiempoExitoAcumulado >= 1.2f) || faseDerrota == FASE_DERROTA_MODAL));
        if (modalActivo) {
            int btnHover = (mx >= 630f && mx <= 930f && my >= 360f && my <= 420f) ? 1 : ((mx >= 970f && mx <= 1270f && my >= 360f && my <= 420f) ? 2 : -1);
            if (btnHover != ultimoUIHover) {
                if (btnHover > 0) sonar(TipoEfecto.HOVER_UI);
                ultimoUIHover = btnHover;
            }
            return;
        }

        if (mostrandoGuiaNivel) return;

        if (terminalBajoRaton != ultimoTerminalHover) {
            if (terminalBajoRaton != null) sonar(TipoEfecto.HOVER_UI);
            ultimoTerminalHover = terminalBajoRaton;
        }

        int slotHover = -1;
        if (mx >= 1645f && mx <= 1895f) {
            for (int i = 0; i < SLOTS_Y.length; i++) {
                if (my >= SLOTS_Y[i] && my <= SLOTS_Y[i] + 185f) { slotHover = 10 + i; break; }
            }
        } else if (mx >= 1430f && mx <= 1590f && my >= ALTO_VIRTUAL - 66f && my <= ALTO_VIRTUAL - 28f) {
            slotHover = 14;
        }
        if (slotHover != ultimoUIHover) {
            if (slotHover > 0) sonar(TipoEfecto.HOVER_UI);
            ultimoUIHover = slotHover;
        }
    }

    public void reiniciarNivel() {
        resetearParametrosNivel();
        if (nivel != null) nivel.reiniciarNivelActual();
        cancelarCable();
    }

    public void siguienteNivel() {
        resetearParametrosNivel();
        if (nivel != null) {
            int siguiente = nivel.getNumeroNivel() + 1;
            if (siguiente > GestorNiveles.obtenerTotalNiveles()) siguiente = 1;
            cargarNivel(GestorNiveles.crearNivel(siguiente));
        }
        cancelarCable();
    }

    private void resetearParametrosNivel() {
        this.derrotaPorTiempo = false;
        this.progresoGuardado = false;
        this.mostrandoGuiaNivel = false;
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
        this.ultimoSegundoSonado = -1;
    }

    public void cargarNivel(Nivel nuevoNivel) {
        this.nivel = nuevoNivel;
        this.mostrandoGuiaNivel = (nuevoNivel != null && (nuevoNivel.getNumeroNivel() == 3 || nuevoNivel.getNumeroNivel() == 4));
        if (this.controlador != null) this.controlador.setNivel(nuevoNivel);
    }

    public void volverAlMenu() {
        sonar(TipoEfecto.CLICK_UI);
        if (juego != null) juego.setScreen(new VistaSeleccionNivel(juego));
    }

    public void saltarIntro() {
        this.tiempoIntro = 3f;
        this.introCompletada = true;
        this.voltajeMedidorDC = 5.0f;
    }

    public GestorAudio getGestorAudio() { return gestorAudio; }
    public boolean estaCableEnProgreso() { return cableEnProgreso; }
    public boolean isCableEnProgreso() { return cableEnProgreso; }

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
