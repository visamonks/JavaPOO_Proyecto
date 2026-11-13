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
import com.badlogic.gdx.utils.Align;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VistaNivel extends ScreenAdapter {

    public static final float ANCHO_VIRTUAL = 1920f;
    public static final float ALTO_VIRTUAL = 1080f;

    private static final float ALMACEN_X = 1645f, ALMACEN_ANCHO = 250f;
    private static final float ALMACEN_TOPE = 905f, ALMACEN_PISO = 75f, ALMACEN_SEPARACION = 15f;
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
    private BitmapFont font, fontTitulo, fontPequena, fontTutorial;
    private GlyphLayout glyphLayout;
    private ControladorNivel controlador;

    private List<Texture> texturasCargadas;
    private Animation<TextureRegion> animLedNormal, animLedPrendiendo, animLedMuriendo;
    private Animation<TextureRegion> animResistenciaNormal, animResistenciaFallo, animAnd, animOr;
    private Animation<TextureRegion> animCapacitorVacio, animCapacitorCargado, animCapacitorExplotado, anim555Astable, anim555Monoestable, animPotenciometro;

    private boolean mostrandoGuiaNivel;
    private int paginaTutorial;
    private TutorialNivel tutorialActual;
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

    private final EnrutadorCables enrutador = new EnrutadorCables();
    private final Map<Cable, List<Vector2>> rutasCables = new HashMap<>();
    private String firmaRutas = "";

    private final Color COLOR_FONDO_NEGRO = new Color(0.02f, 0.03f, 0.06f, 1f);
    private final Color COLOR_SLATE_OSCURO = new Color(0.06f, 0.09f, 0.14f, 1f);
    private final Color COLOR_GRIS_METAL = new Color(0.30f, 0.38f, 0.48f, 1f);
    private final Color COLOR_GRIS_CLARO = new Color(0.65f, 0.76f, 0.88f, 1f);
    private final Color COLOR_NEON_CELESTE = new Color(0.0f, 0.88f, 1.0f, 1f);
    private final Color COLOR_CELESTE_BRILLANTE = new Color(0.60f, 0.95f, 1.0f, 1f);
    private final BarraAtajos atajos = new BarraAtajos(COLOR_CELESTE_BRILLANTE, new Color(0.65f, 0.76f, 0.88f, 0.75f));
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
        this.fontTutorial = crearFont(1.45f);

        this.texturasCargadas = new ArrayList<>();
        cargarAnimaciones();

        this.ratonMundo = new Vector2();
        this.particulas = new ArrayList<>();
        this.mostrandoGuiaNivel = (nivel != null);
        this.paginaTutorial = 0;
        this.tutorialActual = (nivel != null) ? nivel.obtenerTutorial() : null;
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
        animCapacitorVacio = cargarAnimacion("capacitor_vacio", 8, 0.14f);
        animCapacitorCargado = cargarAnimacion("capacitor_cargado", 8, 0.10f);
        animCapacitorExplotado = cargarAnimacion("capacitor_explotado", 8, 0.08f);
        anim555Astable = cargarAnimacion("555_astable", 10, 0.07f);
        anim555Monoestable = cargarAnimacion("555_monoestable", 10, 0.20f);
        animPotenciometro = cargarAnimacion("potenciometro", 8, 0.14f);
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
        actualizarRutasSiCambioLaMesa();
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
        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarPerillasPotenciometro();
        dibujarBarraPotenciometroShapes();
        shapeRenderer.end();
        if (potenciometroAbierto != null) {
            batch.begin();
            dibujarBarraPotenciometroTextos();
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
        }

        dibujarEfectosDerrota();

        boolean modalFinActivo = (nivel != null && ((nivel.estaGanado() && tiempoExitoAcumulado >= 1.2f) || faseDerrota == FASE_DERROTA_MODAL));
        if (modalFinActivo) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dibujarFondoModal();
            shapeRenderer.end();
            batch.begin();
            dibujarContenidoModal();
            batch.end();
        } else if (mostrandoGuiaNivel && tutorialActual != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
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

    private float getAltoCasilla() {
        int n = Math.max(1, nivel.getInventario().size());
        return Math.min(185f, (ALMACEN_TOPE - ALMACEN_PISO - (n - 1) * ALMACEN_SEPARACION) / n);
    }

    private float getYCasilla(int i) {
        float alto = getAltoCasilla();
        return ALMACEN_TOPE - alto - i * (alto + ALMACEN_SEPARACION);
    }

    private int obtenerIndiceCasillaEn(float mx, float my) {
        if (nivel == null || mx < ALMACEN_X || mx > ALMACEN_X + ALMACEN_ANCHO) return -1;
        float alto = getAltoCasilla();
        for (int i = 0; i < nivel.getInventario().size(); i++) {
            float y = getYCasilla(i);
            if (my >= y && my <= y + alto) return i;
        }
        return -1;
    }

    public String obtenerTipoCasillaEn(float mx, float my) {
        int i = obtenerIndiceCasillaEn(mx, my);
        return (i >= 0) ? nivel.getInventario().get(i).getTipo() : null;
    }

    private void dibujarBarraLateralAlmacen() {
        float bx = 1620f, by = 60f, bw = 300f, bh = ALTO_VIRTUAL - 150f;
        shapeRenderer.setColor(COLOR_CAJA_OSCURA);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.setColor(COLOR_NEON_CELESTE);
        shapeRenderer.rectLine(bx, by, bx, by + bh, 3.8f);
        if (nivel == null) return;

        List<Nivel.CasillaInventario> inventario = nivel.getInventario();
        float alto = getAltoCasilla();
        for (int i = 0; i < inventario.size(); i++) {
            Nivel.CasillaInventario casilla = inventario.get(i);
            float y = getYCasilla(i);
            dibujarTarjetaAlmacen(ALMACEN_X, y, ALMACEN_ANCHO, alto, casilla.puedeUsar());
            if (casilla.getTipo().startsWith("SWITCH")) {
                dibujarIconoSwitchAlmacen(y, alto, nivel.esSwitchDivisor(casilla.getTipo()));
            }
        }
    }

    private void dibujarIconoSwitchAlmacen(float y, float alto, boolean doble) {
        float s = Math.min(1f, (alto - 60f) / 90f);
        float cx = ALMACEN_X + 123f, cy = y + alto / 2f - 2f;
        shapeRenderer.setColor(0.10f, 0.14f, 0.22f, 1f);
        shapeRenderer.rect(cx - 85f * s, cy - 45f * s, 170f * s, 90f * s);
        if (doble) {
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(cx - 71f * s, cy, cx - 41f * s, cy, 3.5f);
            shapeRenderer.rectLine(cx - 41f * s, cy - 23f * s, cx - 41f * s, cy + 23f * s, 3.5f);
            shapeRenderer.setColor(0.20f, 0.95f, 0.45f, 1f);
            shapeRenderer.rectLine(cx - 41f * s, cy + 23f * s, cx + 65f * s, cy + 23f * s, 4f);
            shapeRenderer.rectLine(cx - 41f * s, cy - 23f * s, cx + 65f * s, cy - 23f * s, 4f);
            shapeRenderer.circle(cx - 71f * s, cy, 5f);
            shapeRenderer.circle(cx + 65f * s, cy + 23f * s, 5f);
            shapeRenderer.circle(cx + 65f * s, cy - 23f * s, 5f);
        } else {
            shapeRenderer.setColor(0.20f, 0.95f, 0.45f, 1f);
            shapeRenderer.rectLine(cx - 63f * s, cy, cx + 62f * s, cy, 5f);
            shapeRenderer.circle(cx - 63f * s, cy, 6f);
            shapeRenderer.circle(cx + 62f * s, cy, 6f);
        }
    }

    private void dibujarContenidoBarraLateral() {
        fontTitulo.setColor(0.00f, 0.85f, 1.00f, 1f);
        glyphLayout.setText(fontTitulo, "COMPONENTES");
        fontTitulo.draw(batch, glyphLayout, 1620f + (300f - glyphLayout.width) / 2f, 955f);
        if (nivel == null) return;

        List<Nivel.CasillaInventario> inventario = nivel.getInventario();
        float alto = getAltoCasilla();
        float escala = Math.min(1f, (alto - 55f) / 130f);
        for (int i = 0; i < inventario.size(); i++) {
            Nivel.CasillaInventario casilla = inventario.get(i);
            String tipo = casilla.getTipo();
            float y = getYCasilla(i);

            TextureRegion frame = null;
            float fw = 0f, fh = 0f;
            if (tipo.startsWith("LED")) {
                frame = animLedNormal.getKeyFrame(tiempoTotal, true); fw = 130f; fh = 130f;
            } else if ("RESISTENCIA_220".equals(tipo)) {
                frame = animResistenciaNormal.getKeyFrame(tiempoTotal, true); fw = 175f; fh = 112f;
            } else if ("AND".equals(tipo)) {
                frame = animAnd.getKeyFrame(tiempoTotal, true); fw = 136f; fh = 136f;
            } else if ("OR".equals(tipo)) {
                frame = animOr.getKeyFrame(tiempoTotal, true); fw = 136f; fh = 136f;
            } else if ("CAPACITOR".equals(tipo)) {
                frame = animCapacitorCargado.getKeyFrame(tiempoTotal, true); fw = 140f; fh = 140f;
            } else if ("555".equals(tipo)) {
                frame = anim555Monoestable.getKeyFrame(tiempoTotal, true); fw = 170f; fh = 170f;
            } else if ("POTENCIOMETRO".equals(tipo)) {
                frame = animPotenciometro.getKeyFrame(tiempoTotal, true); fw = 140f; fh = 140f;
            }
            Color tinte = tipo.startsWith("LED_") ? colorDeLed(tipo.substring(4)) : Color.WHITE;
            dibujarSlotInventario(casilla, y, alto, frame, fw * escala, fh * escala, tinte);
        }
    }

    private void dibujarSlotInventario(Nivel.CasillaInventario casilla, float y, float alto, TextureRegion frame, float fw, float fh, Color tinte) {
        int cant = casilla.getCantidadDisponible();
        if (frame != null) {
            batch.setColor(tinte);
            if (cant == 0) batch.setColor(tinte.r * 0.5f, tinte.g * 0.5f, tinte.b * 0.5f, 0.45f);
            batch.draw(frame, ALMACEN_X + 125f - fw / 2f, y + alto / 2f - 8f - fh / 2f, fw, fh);
            batch.setColor(Color.WHITE);
        }
        font.setColor(cant > 0 ? Color.WHITE : COLOR_GRIS_METAL);
        font.draw(batch, casilla.getTitulo().toUpperCase(), 1660f, y + alto - 13f);
        fontTitulo.setColor(cant > 0 ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
        fontTitulo.draw(batch, "x" + cant, 1845f, y + 32f);
    }

    private void dibujarVistaPreviaArrastre() {
        if (controlador == null || !controlador.estaArrastrandoDesdeBarra()) return;
        String tipo = controlador.getTipoComponenteArrastrado();
        Vector2 pos = controlador.getPosicionArrastre();
        batch.setColor(1f, 1f, 1f, 0.85f);
        if (tipo.startsWith("LED")) {
            Color tinte = tipo.startsWith("LED_") ? colorDeLed(tipo.substring(4)) : Color.WHITE;
            batch.setColor(tinte.r, tinte.g, tinte.b, 0.85f);
            batch.draw(animLedNormal.getKeyFrame(tiempoTotal, true), pos.x - 120f, pos.y - 120f, 240f, 240f);
        }
        else if ("RESISTENCIA_220".equals(tipo)) batch.draw(animResistenciaNormal.getKeyFrame(tiempoTotal, true), pos.x - 135f, pos.y - 86f, 270f, 172f);
        else if ("AND".equals(tipo)) batch.draw(animAnd.getKeyFrame(tiempoTotal, true), pos.x - 140f, pos.y - 140f, 280f, 280f);
        else if ("OR".equals(tipo)) batch.draw(animOr.getKeyFrame(tiempoTotal, true), pos.x - 140f, pos.y - 140f, 280f, 280f);
        else if ("CAPACITOR".equals(tipo)) batch.draw(animCapacitorVacio.getKeyFrame(tiempoTotal, true), pos.x - 120f, pos.y - 120f, 240f, 240f);
        else if ("555".equals(tipo)) batch.draw(anim555Monoestable.getKeyFrame(tiempoTotal, true), pos.x - 180f, pos.y - 180f, 360f, 360f);
        else if ("POTENCIOMETRO".equals(tipo)) batch.draw(animPotenciometro.getKeyFrame(tiempoTotal, true), pos.x - 140f, pos.y - 140f, 280f, 280f);
        else if (tipo != null && tipo.startsWith("SWITCH")) {
            boolean esDoble = (nivel != null && nivel.esSwitchDivisor(tipo));
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
                if (ratonMundo.dst(codo.x, codo.y) > 22f) continue;
                float pulso = 1f + 0.25f * MathUtils.sin(tiempoTotal * 14f);
                shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.55f);
                shapeRenderer.circle(codo.x, codo.y, 19f * pulso);
                shapeRenderer.setColor(COLOR_CAJA_OSCURA);
                shapeRenderer.circle(codo.x, codo.y, 10f);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.circle(codo.x, codo.y, 6f);
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

    private String calcularFirmaMesa() {
        StringBuilder firma = new StringBuilder();
        for (Componente c : nivel.getComponentes()) {
            firma.append(c.getPosicionX()).append(',').append(c.getPosicionY()).append(';');
        }
        for (Cable c : nivel.getCables()) {
            firma.append(System.identityHashCode(c)).append(':')
                 .append(c.getCodoPersonalizadoX()).append(',').append(c.getCodoPersonalizadoY()).append(';');
        }
        return firma.toString();
    }

    private void actualizarRutasSiCambioLaMesa() {
        if (nivel == null) return;
        String firma = calcularFirmaMesa();
        if (firma.equals(firmaRutas)) return;
        firmaRutas = firma;

        rutasCables.clear();
        enrutador.prepararMesa(nivel.getComponentes());
        for (int vuelta = 0; vuelta < 2; vuelta++) {
            for (Cable cable : nivel.getCables()) {
                Terminal t1 = cable.getTerminalOrigen(), t2 = cable.getTerminalDestino();
                if (t1 == null || t2 == null) continue;
                Vector2 codo = cable.tieneCodoPersonalizado()
                        ? new Vector2(cable.getCodoPersonalizadoX(), cable.getCodoPersonalizadoY()) : null;
                if (vuelta == 1) enrutador.olvidarRuta(rutasCables.get(cable));
                List<Vector2> ruta = enrutador.calcularRuta(t1, t2, codo);
                enrutador.marcarComoUsada(ruta, t1, t2);
                rutasCables.put(cable, ruta);
            }
        }
    }

    public List<Vector2> calcularRutaOrtogonal(Cable cable, Terminal tOrigen, Terminal tDestino) {
        if (tOrigen == null || tDestino == null) return new ArrayList<>();
        actualizarRutasSiCambioLaMesa();
        List<Vector2> ruta = rutasCables.get(cable);
        return (ruta != null) ? ruta : enrutador.calcularRuta(tOrigen, tDestino, null);
    }

    public List<Vector2> calcularRutaOrtogonalArrastre(Terminal tOrigen, Vector2 pDestino) {
        if (tOrigen == null || pDestino == null) return new ArrayList<>();
        actualizarRutasSiCambioLaMesa();
        if (terminalBajoRaton != null && terminalBajoRaton.getComponentePadre() != tOrigen.getComponentePadre()) {
            return enrutador.calcularRuta(tOrigen, terminalBajoRaton, null);
        }
        return enrutador.calcularRutaHaciaPunto(tOrigen, pDestino);
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
                Color tinte = colorDeLed(led.getColor());
                if ("EXITO".equals(led.getEstadoActual()) && led.getBrillo() < 1f) {
                    batch.setColor(tinte);
                    batch.draw(animLedNormal.getKeyFrame(tiempoTotal, true), x, y, w, h);
                    batch.setColor(tinte.r, tinte.g, tinte.b, led.getBrillo());
                } else {
                    batch.setColor(tinte);
                }
                batch.draw(f, x, y, w, h);
                batch.setColor(Color.WHITE);
                fontPequena.setColor(1f, 0.42f, 0.42f, 0.90f); fontPequena.draw(batch, "+", x + 54f, y + 88f);
                fontPequena.setColor(0.42f, 0.78f, 1f, 0.90f); fontPequena.draw(batch, "-", x + 174f, y + 88f);
                fontPequena.setColor(COLOR_GRIS_CLARO); glyphLayout.setText(fontPequena, nombreDeLed(led.getColor()));
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h - 6f);
            } else if (comp instanceof Potenciometro) {
                Potenciometro pot = (Potenciometro) comp;
                batch.draw(animPotenciometro.getKeyFrame(tiempoTotal, true), x, y, w, h);
                String texto = "INTENSIDAD " + pot.getPorcentaje() + "%";
                fontPequena.setColor(COLOR_CELESTE_BRILLANTE);
                glyphLayout.setText(fontPequena, texto);
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h - 18f);
                fontPequena.setColor(COLOR_GRIS_METAL);
                fontPequena.draw(batch, "-", x + 54f, y + 154f);
                fontPequena.draw(batch, "+", x + w - 66f, y + 154f);
            } else if (comp instanceof Temporizador555) {
                Temporizador555 chip = (Temporizador555) comp;
                Animation<TextureRegion> anim = chip.isAstable() ? anim555Astable : anim555Monoestable;
                batch.draw(anim.getKeyFrame(tiempoTotal, true), x, y, w, h);
                fontPequena.setColor(COLOR_GRIS_CLARO);
                fontPequena.draw(batch, "VCC", x + 20f, y + 236f);
                fontPequena.draw(batch, "TRIG", x + 14f, y + 201f);
                fontPequena.draw(batch, "GND", x + 18f, y + 166f);
                fontPequena.draw(batch, "OUT", x + w - 66f, y + 236f);
                fontPequena.draw(batch, "C", x + w - 60f, y + 166f);
                String modo = chip.isAstable() ? "MODO ASTABLE" : "MODO MONOESTABLE";
                fontPequena.setColor(chip.isSalidaAlta() ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO);
                glyphLayout.setText(fontPequena, modo);
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + 92f);
                fontPequena.setColor(COLOR_GRIS_METAL);
                glyphLayout.setText(fontPequena, "(clic para cambiar)");
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + 70f);
            } else if (comp instanceof Capacitor) {
                Capacitor cap = (Capacitor) comp;
                Animation<TextureRegion> anim = cap.tieneCarga() ? animCapacitorCargado : animCapacitorVacio;
                if (cap.isExplotado()) anim = animCapacitorExplotado;
                batch.draw(anim.getKeyFrame(tiempoTotal, true), x, y, w, h);
                fontPequena.setColor(1f, 0.42f, 0.42f, 0.90f); fontPequena.draw(batch, "+", x + 64f, y + 46f);
                fontPequena.setColor(0.42f, 0.78f, 1f, 0.90f); fontPequena.draw(batch, "-", x + 170f, y + 52f);
                String texto = cap.isExplotado() ? "CAPACITOR EXPLOTADO" : "CAPACITOR " + Math.round(cap.getCarga() * 100f) + "%";
                fontPequena.setColor(cap.tieneCarga() ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO);
                glyphLayout.setText(fontPequena, texto);
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h + 4f);
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
                String nombreSwitch = obtenerNombreSwitch(sw);
                glyphLayout.setText(fontPequena, nombreSwitch);
                fontPequena.draw(batch, glyphLayout, x + (w - glyphLayout.width) / 2f, y + h + 18f);
            }
        }
    }

    private Color colorDeLed(String color) {
        if ("ROJO".equals(color)) return new Color(1f, 0.42f, 0.38f, 1f);
        if ("VERDE".equals(color)) return new Color(0.45f, 1f, 0.45f, 1f);
        return Color.WHITE;
    }

    private String nombreDeLed(String color) {
        if ("ROJO".equals(color) || "VERDE".equals(color) || "AMARILLO".equals(color)) return "LED " + color;
        return "LED";
    }

    private String obtenerNombreSwitch(Switch sw) {
        Nivel.CasillaInventario casilla = sw.isDoble() ? nivel.buscarCasilla("SWITCH_DIVISOR") : null;
        if (casilla == null) casilla = nivel.buscarCasilla("SWITCH");
        return (casilla != null) ? casilla.getTitulo().toUpperCase() : "SWITCH";
    }

    private static final float BARRA_POT_ANCHO = 400f, BARRA_POT_ALTO = 96f;
    private Potenciometro potenciometroAbierto;

    public void alternarBarraPotenciometro(Potenciometro pot) {
        potenciometroAbierto = (potenciometroAbierto == pot) ? null : pot;
    }

    public void cerrarBarraPotenciometro() {
        potenciometroAbierto = null;
    }

    private float getBarraPotX() {
        float x = potenciometroAbierto.getPosicionX() + potenciometroAbierto.getAncho() / 2f - BARRA_POT_ANCHO / 2f;
        return MathUtils.clamp(x, 310f, 1600f - BARRA_POT_ANCHO);
    }

    private float getBarraPotY() {
        float y = potenciometroAbierto.getPosicionY() + potenciometroAbierto.getAlto() + 6f;
        if (y + BARRA_POT_ALTO > 975f) y = potenciometroAbierto.getPosicionY() - BARRA_POT_ALTO - 6f;
        return y;
    }

    public boolean estaSobreBarraPotenciometro(float mx, float my) {
        if (potenciometroAbierto == null) return false;
        float x = getBarraPotX(), y = getBarraPotY();
        return mx >= x && mx <= x + BARRA_POT_ANCHO && my >= y && my <= y + BARRA_POT_ALTO;
    }

    public void moverBarraPotenciometro(float mx) {
        if (potenciometroAbierto == null) return;
        float inicio = getBarraPotX() + 40f, fin = getBarraPotX() + BARRA_POT_ANCHO - 40f;
        potenciometroAbierto.setValor((mx - inicio) / (fin - inicio));
    }

    private void dibujarBarraPotenciometroShapes() {
        if (potenciometroAbierto != null && !nivel.getComponentes().contains(potenciometroAbierto)) {
            potenciometroAbierto = null;
        }
        if (potenciometroAbierto == null) return;
        float x = getBarraPotX(), y = getBarraPotY();
        float inicio = x + 40f, fin = x + BARRA_POT_ANCHO - 40f, ty = y + 34f;
        float valor = potenciometroAbierto.getValor();
        Color ambar = new Color(1f, 0.80f, 0.20f, 1f);

        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(x + 8f, y - 8f, BARRA_POT_ANCHO, BARRA_POT_ALTO);
        dibujarCajaBorde(x, y, BARRA_POT_ANCHO, BARRA_POT_ALTO, new Color(0.04f, 0.06f, 0.10f, 0.97f), ambar, 2f);

        shapeRenderer.setColor(0.16f, 0.19f, 0.25f, 1f);
        shapeRenderer.rect(inicio, ty - 6f, fin - inicio, 12f);
        shapeRenderer.circle(inicio, ty, 6f);
        shapeRenderer.circle(fin, ty, 6f);
        shapeRenderer.setColor(ambar);
        shapeRenderer.rect(inicio, ty - 6f, (fin - inicio) * valor, 12f);
        shapeRenderer.circle(inicio, ty, 6f);
        for (int k = 0; k <= 10; k++) {
            float tx = inicio + (fin - inicio) * k / 10f;
            shapeRenderer.setColor(0.55f, 0.62f, 0.72f, 0.8f);
            shapeRenderer.rect(tx - 1f, ty - 20f, 2f, (k % 5 == 0) ? 9f : 5f);
        }

        float px = inicio + (fin - inicio) * valor;
        shapeRenderer.setColor(COLOR_FONDO_NEGRO);
        shapeRenderer.circle(px, ty, 18f);
        shapeRenderer.setColor(ambar);
        shapeRenderer.circle(px, ty, 14f);
        shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
        shapeRenderer.circle(px, ty, 5f);
    }

    private void dibujarBarraPotenciometroTextos() {
        float x = getBarraPotX(), y = getBarraPotY();
        fontPequena.setColor(1f, 0.80f, 0.20f, 1f);
        fontPequena.draw(batch, "BRILLO", x + 20f, y + BARRA_POT_ALTO - 12f);
        String porcentaje = potenciometroAbierto.getPorcentaje() + "%";
        glyphLayout.setText(fontPequena, porcentaje);
        fontPequena.setColor(Color.WHITE);
        fontPequena.draw(batch, porcentaje, x + BARRA_POT_ANCHO - 20f - glyphLayout.width, y + BARRA_POT_ALTO - 12f);
        String ayuda = "Arrastra la perilla";
        glyphLayout.setText(fontPequena, ayuda);
        fontPequena.setColor(COLOR_GRIS_METAL);
        fontPequena.draw(batch, ayuda, x + (BARRA_POT_ANCHO - glyphLayout.width) / 2f, y + BARRA_POT_ALTO - 12f);
    }

    private void dibujarPerillasPotenciometro() {
        if (nivel == null) return;
        for (Componente comp : nivel.getComponentes()) {
            if (!(comp instanceof Potenciometro)) continue;
            Potenciometro pot = (Potenciometro) comp;
            float cx = pot.getPosicionX() + 140f, cy = pot.getPosicionY() + 149f;
            float angulo = 225f - 270f * pot.getValor();
            float px = cx + 40f * MathUtils.cosDeg(angulo), py = cy + 40f * MathUtils.sinDeg(angulo);
            shapeRenderer.setColor(COLOR_FONDO_NEGRO);
            shapeRenderer.circle(px, py, 8f);
            shapeRenderer.setColor(1f, 0.80f, 0.20f, 1f);
            shapeRenderer.circle(px, py, 5.5f);
            float bx = pot.getPosicionX() + 70f, by = pot.getPosicionY() + 14f, bw = 140f;
            shapeRenderer.setColor(0.08f, 0.10f, 0.14f, 1f);
            shapeRenderer.rect(bx, by, bw, 10f);
            shapeRenderer.setColor(1f, 0.85f, 0.30f, 1f);
            shapeRenderer.rect(bx, by, bw * pot.getValor(), 10f);
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
            fontPequena.draw(batch, nivel.getTextoProgreso(), 312f, ALTO_VIRTUAL - 101f);
        }

        fontPequena.setColor(COLOR_CELESTE_BRILLANTE);
        glyphLayout.setText(fontPequena, "VER TUTORIAL (H)");
        fontPequena.draw(batch, glyphLayout, 1430f + (160f - glyphLayout.width) / 2f, ALTO_VIRTUAL - 66f + (38f + glyphLayout.height) / 2f);
        atajos.dibujar(batch, fontPequena, 40f, 36f,
                "ARRASTRA", "Piezas del almacén", "CLIC EN UN PIN", "Crear cable", "CLIC DERECHO", "Borrar",
                "H", "Tutorial", "R", "Reiniciar", "ESC", "Menú");
    }

    private static final float TUTO_X = 360f, TUTO_Y = 170f, TUTO_ANCHO = 1200f, TUTO_ALTO = 740f;
    private static final float TUTO_BOTON_ANCHO = 260f, TUTO_BOTON_ALTO = 56f;
    private static final float TUTO_BOTON_Y = TUTO_Y + 30f;
    private static final float TUTO_SIGUIENTE_X = TUTO_X + TUTO_ANCHO - 40f - TUTO_BOTON_ANCHO;
    private static final float TUTO_ATRAS_X = TUTO_X + 40f;
    private static final float ESQUEMA_ANCHO = 848f, ESQUEMA_ALTO = 236f;
    private static final float PESTANA_ANCHO = 300f, PESTANA_ALTO = 40f, PESTANA_Y = TUTO_Y + TUTO_ALTO - 126f;
    private static final String[] TITULOS_PAGINAS = {"1   ¿QUÉ ES?", "2   ASÍ SE CONECTA", "3   TU MISIÓN"};
    private static final int TOTAL_PAGINAS = 3;

    private final Color COLOR_VERDE_TUTO = new Color(0.20f, 0.95f, 0.45f, 1f);
    private final Color COLOR_AMBAR_TUTO = new Color(1.0f, 0.76f, 0.20f, 1f);

    private boolean estaSobre(float x, float y, float w, float h) {
        return ratonMundo.x >= x && ratonMundo.x <= x + w && ratonMundo.y >= y && ratonMundo.y <= y + h;
    }

    private float getXPestana(int i) {
        float total = TOTAL_PAGINAS * PESTANA_ANCHO + (TOTAL_PAGINAS - 1) * 20f;
        return TUTO_X + (TUTO_ANCHO - total) / 2f + i * (PESTANA_ANCHO + 20f);
    }

    private void dibujarGuiaNivelShapes() {
        shapeRenderer.setColor(0.01f, 0.02f, 0.04f, 0.86f);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        shapeRenderer.setColor(0f, 0f, 0f, 0.72f);
        shapeRenderer.rect(TUTO_X + 12f, TUTO_Y - 12f, TUTO_ANCHO, TUTO_ALTO);
        dibujarCajaBorde(TUTO_X, TUTO_Y, TUTO_ANCHO, TUTO_ALTO, new Color(0.04f, 0.06f, 0.10f, 0.98f), COLOR_NEON_CELESTE, 2.4f);
        shapeRenderer.setColor(0.06f, 0.10f, 0.16f, 0.95f);
        shapeRenderer.rect(TUTO_X + 2f, TUTO_Y + TUTO_ALTO - 70f, TUTO_ANCHO - 4f, 68f);

        for (int i = 0; i < TOTAL_PAGINAS; i++) {
            float px = getXPestana(i);
            boolean actual = (i == paginaTutorial), vista = (i < paginaTutorial);
            Color fondo = actual ? new Color(0.06f, 0.16f, 0.24f, 1f) : new Color(0.03f, 0.05f, 0.09f, 0.95f);
            boolean hover = estaSobre(px, PESTANA_Y, PESTANA_ANCHO, PESTANA_ALTO);
            Color borde = (actual || hover) ? COLOR_CELESTE_BRILLANTE : (vista ? COLOR_NEON_CELESTE : COLOR_GRIS_METAL);
            dibujarCajaBorde(px, PESTANA_Y, PESTANA_ANCHO, PESTANA_ALTO, fondo, borde, actual ? 2.2f : 1.3f);
            if (actual) {
                shapeRenderer.setColor(COLOR_NEON_CELESTE);
                shapeRenderer.rect(px, PESTANA_Y, PESTANA_ANCHO, 4f);
            }
        }

        if (paginaTutorial == 0) dibujarPaginaConceptoShapes();
        else if (paginaTutorial == 1) dibujarPaginaConexionShapes();
        else dibujarPaginaMisionShapes();

        boolean ultima = (paginaTutorial == TOTAL_PAGINAS - 1);
        boolean hov = estaSobre(TUTO_SIGUIENTE_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO);
        Color colorBoton = ultima ? COLOR_VERDE_TUTO : COLOR_NEON_CELESTE;
        dibujarCajaBorde(TUTO_SIGUIENTE_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO,
                hov ? new Color(0.08f, 0.18f, 0.28f, 1f) : COLOR_SLATE_OSCURO, colorBoton, hov ? 2.8f : 2f);

        boolean hovAtras = estaSobre(TUTO_ATRAS_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO);
        dibujarCajaBorde(TUTO_ATRAS_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO,
                hovAtras ? new Color(0.08f, 0.12f, 0.18f, 1f) : COLOR_CAJA_OSCURA, hovAtras ? COLOR_GRIS_CLARO : COLOR_GRIS_METAL, hovAtras ? 2.4f : 1.6f);
    }

    private void dibujarGuiaNivelTextos() {
        fontTitulo.setColor(COLOR_CELESTE_BRILLANTE);
        String titulo = "NIVEL " + nivel.getNumeroNivel() + ":  " + tutorialActual.getTitulo();
        fontTitulo.draw(batch, titulo, TUTO_X + 32f, TUTO_Y + TUTO_ALTO - 22f);

        fontPequena.setColor(COLOR_GRIS_CLARO.r, COLOR_GRIS_CLARO.g, COLOR_GRIS_CLARO.b, 0.78f);
        String pausa = "EL RELOJ ESTÁ EN PAUSA";
        glyphLayout.setText(fontPequena, pausa);
        fontPequena.draw(batch, pausa, TUTO_X + TUTO_ANCHO - glyphLayout.width - 32f, TUTO_Y + TUTO_ALTO - 27f);

        for (int i = 0; i < TOTAL_PAGINAS; i++) {
            fontPequena.setColor(i == paginaTutorial ? Color.WHITE : COLOR_GRIS_CLARO);
            glyphLayout.setText(fontPequena, TITULOS_PAGINAS[i]);
            fontPequena.draw(batch, TITULOS_PAGINAS[i], getXPestana(i) + (PESTANA_ANCHO - glyphLayout.width) / 2f,
                    PESTANA_Y + (PESTANA_ALTO + glyphLayout.height) / 2f);
        }

        if (paginaTutorial == 0) dibujarPaginaConceptoTextos();
        else if (paginaTutorial == 1) dibujarPaginaConexionTextos();
        else dibujarPaginaMisionTextos();

        boolean ultima = (paginaTutorial == TOTAL_PAGINAS - 1);
        String textoBoton = ultima ? "¡A JUGAR!" : "SIGUIENTE";
        font.setColor(ultima ? COLOR_VERDE_TUTO : COLOR_CELESTE_BRILLANTE);
        glyphLayout.setText(font, textoBoton);
        font.draw(batch, textoBoton, TUTO_SIGUIENTE_X + (TUTO_BOTON_ANCHO - glyphLayout.width) / 2f,
                TUTO_BOTON_Y + (TUTO_BOTON_ALTO + glyphLayout.height) / 2f);

        String textoAtras = (paginaTutorial == 0) ? "SALTAR" : "ATRÁS";
        font.setColor(COLOR_GRIS_CLARO);
        glyphLayout.setText(font, textoAtras);
        font.draw(batch, textoAtras, TUTO_ATRAS_X + (TUTO_BOTON_ANCHO - glyphLayout.width) / 2f,
                TUTO_BOTON_Y + (TUTO_BOTON_ALTO + glyphLayout.height) / 2f);

        glyphLayout.setText(fontPequena, "ENTER");
        atajos.dibujarCentrado(batch, fontPequena, TUTO_X + TUTO_ANCHO / 2f, TUTO_BOTON_Y + (TUTO_BOTON_ALTO + glyphLayout.height) / 2f,
                "ENTER", "Siguiente", "FLECHAS", "Cambiar página", "ESC", "Cerrar");
    }

    private void dibujarBloqueTutorial(float x, float y, float w, float h, Color color) {
        shapeRenderer.setColor(0.035f, 0.055f, 0.085f, 0.95f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, 8f, h);
        dibujarBordeRect(x, y, w, h, new Color(color.r, color.g, color.b, 0.35f), 1.5f);
    }

    private void dibujarTextoBloqueTutorial(float x, float y, float w, float h, String etiqueta, String texto, Color color) {
        fontPequena.setColor(color);
        fontPequena.draw(batch, etiqueta, x + 30f, y + h - 20f);
        fontTutorial.setColor(Color.WHITE);
        fontTutorial.draw(batch, texto, x + 30f, y + h - 58f, w - 60f, Align.left, true);
    }

    private static final float PANEL_X = TUTO_X + 50f, PANEL_Y = TUTO_Y + 130f, PANEL_ANCHO = 380f, PANEL_ALTO = 450f;
    private static final float BLOQUE_X = TUTO_X + 470f, BLOQUE_ANCHO = 680f;

    private void dibujarPaginaConceptoShapes() {
        dibujarCajaBorde(PANEL_X, PANEL_Y, PANEL_ANCHO, PANEL_ALTO, new Color(0.018f, 0.030f, 0.055f, 0.98f),
                new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.55f), 1.5f);
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.06f);
        for (float g = 30f; g < PANEL_ANCHO; g += 30f) shapeRenderer.rectLine(PANEL_X + g, PANEL_Y, PANEL_X + g, PANEL_Y + PANEL_ALTO, 1f);
        for (float g = 30f; g < PANEL_ALTO; g += 30f) shapeRenderer.rectLine(PANEL_X, PANEL_Y + g, PANEL_X + PANEL_ANCHO, PANEL_Y + g, 1f);

        if ("SEMAFORO".equals(tutorialActual.getIcono())) {
            float cx = PANEL_X + PANEL_ANCHO / 2f, cy = PANEL_Y + PANEL_ALTO / 2f;
            dibujarCajaBorde(cx - 80f, cy - 175f, 160f, 375f, new Color(0.10f, 0.11f, 0.14f, 1f), COLOR_GRIS_METAL, 3f);
            shapeRenderer.setColor(0.10f, 0.11f, 0.14f, 1f);
            shapeRenderer.rect(cx - 12f, cy - 225f, 24f, 50f);
        }
        if (tutorialActual.getEsquema() == TutorialNivel.Esquema.SERIE_CON_SWITCH) {
            float sx = PANEL_X + 70f, sy = PANEL_Y + 170f, sw = 240f, sh = 120f;
            boolean cerrado = (tiempoTotal % 2f) < 1f;
            dibujarCajaBorde(sx, sy, sw, sh, new Color(0.08f, 0.11f, 0.16f, 1f), cerrado ? COLOR_VERDE_TUTO : COLOR_GRIS_METAL, 3f);
            dibujarPalancaSwitch(sx + 40f, sy + sh / 2f, sx + sw - 40f, sy + sh / 2f, cerrado, 40f);
        }

        dibujarBloqueTutorial(BLOQUE_X, TUTO_Y + 380f, BLOQUE_ANCHO, 200f, COLOR_NEON_CELESTE);
        dibujarBloqueTutorial(BLOQUE_X, TUTO_Y + 130f, BLOQUE_ANCHO, 220f, COLOR_VERDE_TUTO);
    }

    private void dibujarIconoPrincipalTutorial(String icono) {
        float cx = PANEL_X + PANEL_ANCHO / 2f, cy = PANEL_Y + PANEL_ALTO / 2f;
        String nombre = "";
        if (icono.equals("LED_DOS")) {
            boolean izquierda = (tiempoTotal % 1.2f) < 0.6f;
            for (int i = 0; i < 2; i++) {
                boolean prendido = (i == 0) == izquierda;
                TextureRegion f = (prendido ? animLedPrendiendo : animLedNormal).getKeyFrame(tiempoTotal, true);
                if (!prendido) batch.setColor(0.6f, 0.6f, 0.6f, 1f);
                batch.draw(f, cx - 175f + i * 180f, cy - 70f, 170f, 170f);
                batch.setColor(Color.WHITE);
            }
            font.setColor(COLOR_GRIS_CLARO);
            font.draw(batch, "IZQ", cx - 125f, cy - 80f);
            font.draw(batch, "DER", cx + 55f, cy - 80f);
            nombre = "DOS LEDs EN PARALELO";
        }
        if (icono.equals("POT")) {
            float valor = 0.5f + 0.5f * MathUtils.sin(tiempoTotal * 1.2f);
            batch.draw(animPotenciometro.getKeyFrame(tiempoTotal, true), cx - 150f, cy - 60f, 300f, 300f);
            batch.setColor(1f, 1f, 1f, 0.25f);
            batch.draw(animLedNormal.getKeyFrame(tiempoTotal, true), cx - 60f, cy - 215f, 120f, 120f);
            batch.setColor(1f, 1f, 1f, valor);
            batch.draw(animLedPrendiendo.getKeyFrame(tiempoTotal, true), cx - 60f, cy - 215f, 120f, 120f);
            batch.setColor(Color.WHITE);
            font.setColor(COLOR_CELESTE_BRILLANTE);
            String texto = Math.round(valor * 100f) + "%";
            glyphLayout.setText(font, texto);
            font.draw(batch, texto, cx + 70f, cy - 150f);
            nombre = "POTENCIÓMETRO";
        }
        if (icono.equals("555A") || icono.equals("555M")) {
            boolean astable = icono.equals("555A");
            batch.draw((astable ? anim555Astable : anim555Monoestable).getKeyFrame(tiempoTotal, true), cx - 170f, cy - 150f, 340f, 340f);
            if (astable) {
                boolean prendido = (tiempoTotal % 1f) < 0.5f;
                batch.setColor(colorDeLed("ROJO"));
                batch.draw((prendido ? animLedPrendiendo : animLedNormal).getKeyFrame(tiempoTotal, true), cx - 60f, cy - 215f, 120f, 120f);
                batch.setColor(Color.WHITE);
            }
            nombre = astable ? "555 ASTABLE: SE PRENDE Y APAGA SOLO" : "555 MONOESTABLE: UN TOQUE, UN PULSO";
        }
        if (icono.equals("CAP")) {
            float ciclo = (tiempoTotal % 6f) / 6f;
            float carga = ciclo < 0.4f ? ciclo / 0.4f : Math.max(0f, 1f - (ciclo - 0.4f) / 0.6f);
            Animation<TextureRegion> anim = carga > 0.05f ? animCapacitorCargado : animCapacitorVacio;
            batch.draw(anim.getKeyFrame(tiempoTotal, true), cx - 150f, cy - 120f, 300f, 300f);
            font.setColor(carga > 0.05f ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_CLARO);
            String texto = (ciclo < 0.4f ? "CARGANDO " : "SOLTANDO ") + Math.round(carga * 100f) + "%";
            glyphLayout.setText(font, texto);
            font.draw(batch, texto, cx - glyphLayout.width / 2f, cy - 140f);
            nombre = "CAPACITOR";
        }
        if (icono.equals("SEMAFORO")) {
            int prendida = ((int) (tiempoTotal / 0.9f)) % 3;
            String[] colores = {"ROJO", "AMARILLO", "VERDE"};
            for (int i = 0; i < 3; i++) {
                boolean on = (2 - prendida) == i;
                Color c = colorDeLed(colores[i]);
                float brillo = on ? 1f : 0.35f;
                batch.setColor(c.r * brillo, c.g * brillo, c.b * brillo, 1f);
                TextureRegion f = (on ? animLedPrendiendo : animLedNormal).getKeyFrame(tiempoTotal, true);
                batch.draw(f, cx - 65f, cy + 70f - i * 115f, 130f, 130f);
            }
            batch.setColor(Color.WHITE);
            nombre = "PRIMERO VERDE, LUEGO AMARILLO Y AL FINAL ROJO";
        }
        fontPequena.setColor(COLOR_AMBAR_TUTO);
        glyphLayout.setText(fontPequena, nombre);
        fontPequena.draw(batch, nombre, PANEL_X + (PANEL_ANCHO - glyphLayout.width) / 2f, PANEL_Y + PANEL_ALTO - 22f);
    }

    private void dibujarPaginaConceptoTextos() {
        if (tutorialActual.getIcono() != null) {
            dibujarIconoPrincipalTutorial(tutorialActual.getIcono());
            dibujarTextoBloqueTutorial(BLOQUE_X, TUTO_Y + 380f, BLOQUE_ANCHO, 200f, "¿QUÉ ES?", tutorialActual.getConcepto(), COLOR_NEON_CELESTE);
            dibujarTextoBloqueTutorial(BLOQUE_X, TUTO_Y + 130f, BLOQUE_ANCHO, 220f, "EN LA VIDA REAL", tutorialActual.getVidaReal(), COLOR_VERDE_TUTO);
            return;
        }
        TutorialNivel.Esquema e = tutorialActual.getEsquema();
        String pieza;
        if (e == TutorialNivel.Esquema.DIVISOR_AND) {
            batch.draw(animAnd.getKeyFrame(tiempoTotal, true), PANEL_X + 20f, PANEL_Y + 60f, 340f, 340f);
            pieza = "COMPUERTA AND";
        } else if (e == TutorialNivel.Esquema.DIVISOR_OR) {
            batch.draw(animOr.getKeyFrame(tiempoTotal, true), PANEL_X + 20f, PANEL_Y + 60f, 340f, 340f);
            pieza = "COMPUERTA OR";
        } else if (e == TutorialNivel.Esquema.SERIE_CON_SWITCH) {
            boolean cerrado = (tiempoTotal % 2f) < 1f;
            font.setColor(cerrado ? COLOR_VERDE_TUTO : COLOR_GRIS_CLARO);
            String estado = cerrado ? "ON: la corriente pasa" : "OFF: el camino se corta";
            glyphLayout.setText(font, estado);
            font.draw(batch, estado, PANEL_X + (PANEL_ANCHO - glyphLayout.width) / 2f, PANEL_Y + 130f);
            pieza = "INTERRUPTOR";
        } else {
            batch.draw(animLedPrendiendo.getKeyFrame(tiempoTotal, true), PANEL_X + 30f, PANEL_Y + 70f, 320f, 320f);
            pieza = "LED + RESISTENCIA";
        }
        fontPequena.setColor(COLOR_AMBAR_TUTO);
        glyphLayout.setText(fontPequena, pieza);
        fontPequena.draw(batch, pieza, PANEL_X + (PANEL_ANCHO - glyphLayout.width) / 2f, PANEL_Y + PANEL_ALTO - 22f);

        dibujarTextoBloqueTutorial(BLOQUE_X, TUTO_Y + 380f, BLOQUE_ANCHO, 200f, "¿QUÉ ES?", tutorialActual.getConcepto(), COLOR_NEON_CELESTE);
        dibujarTextoBloqueTutorial(BLOQUE_X, TUTO_Y + 130f, BLOQUE_ANCHO, 220f, "EN LA VIDA REAL", tutorialActual.getVidaReal(), COLOR_VERDE_TUTO);
    }

    private static final float ESQUEMA_X = TUTO_X + (TUTO_ANCHO - ESQUEMA_ANCHO) / 2f, ESQUEMA_Y = TUTO_Y + 340f;
    private static final float PASOS_Y = TUTO_Y + 140f, PASOS_ALTO = 150f;

    private float getAnchoPaso() {
        int n = tutorialActual.getPasos().length;
        return (TUTO_ANCHO - 100f - (n - 1) * 16f) / n;
    }

    private void dibujarPaginaConexionShapes() {
        dibujarEsquemaShapes(ESQUEMA_X, ESQUEMA_Y, tutorialActual.getEsquema());
        String[] pasos = tutorialActual.getPasos();
        float ancho = getAnchoPaso();
        for (int i = 0; i < pasos.length; i++) {
            float px = TUTO_X + 50f + i * (ancho + 16f);
            dibujarBloqueTutorial(px, PASOS_Y, ancho, PASOS_ALTO, COLOR_NEON_CELESTE);
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.circle(px + 40f, PASOS_Y + PASOS_ALTO - 34f, 18f);
        }
    }

    private void dibujarPaginaConexionTextos() {
        dibujarEsquemaTextos(ESQUEMA_X, ESQUEMA_Y, tutorialActual.getEsquema());
        String[] pasos = tutorialActual.getPasos();
        float ancho = getAnchoPaso();
        for (int i = 0; i < pasos.length; i++) {
            float px = TUTO_X + 50f + i * (ancho + 16f);
            String numero = String.valueOf(i + 1);
            font.setColor(COLOR_FONDO_NEGRO);
            glyphLayout.setText(font, numero);
            font.draw(batch, numero, px + 40f - glyphLayout.width / 2f, PASOS_Y + PASOS_ALTO - 34f + glyphLayout.height / 2f);
            font.setColor(Color.WHITE);
            font.draw(batch, pasos[i], px + 24f, PASOS_Y + PASOS_ALTO - 68f, ancho - 40f, Align.left, true);
        }
    }

    private static final float TABLA_Y = TUTO_Y + 330f, TABLA_ALTO = 90f;
    private static final float CONSEJO_Y = TUTO_Y + 200f, CONSEJO_ALTO = 90f;

    private void dibujarPaginaMisionShapes() {
        if (tutorialActual.tieneTablaVerdad()) {
            boolean[] tabla = tutorialActual.getTablaVerdad();
            boolean[][] entradas = {{false, false}, {true, false}, {false, true}, {true, true}};
            float pw = 198f, gap = (ESQUEMA_ANCHO - 4f * pw) / 3f;
            for (int r = 0; r < 4; r++) {
                float px = ESQUEMA_X + r * (pw + gap), dotY = TABLA_Y + TABLA_ALTO * 0.42f;
                boolean a = entradas[r][0], b = entradas[r][1], enc = tabla[r];
                dibujarCajaBorde(px, TABLA_Y, pw, TABLA_ALTO, enc ? new Color(0.05f, 0.12f, 0.19f, 0.96f) : new Color(0.025f, 0.04f, 0.07f, 0.92f),
                        enc ? COLOR_NEON_CELESTE : new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.45f), enc ? 1.9f : 1.1f);
                shapeRenderer.setColor(a ? new Color(0.15f, 0.95f, 0.72f, 1f) : new Color(0.22f, 0.28f, 0.38f, 1f)); shapeRenderer.circle(px + 38f, dotY, 7f);
                shapeRenderer.setColor(b ? new Color(0.15f, 0.95f, 0.72f, 1f) : new Color(0.22f, 0.28f, 0.38f, 1f)); shapeRenderer.circle(px + 84f, dotY, 7f);
                dibujarFlechaHorizontal(px + 104f, px + 128f, dotY, enc ? COLOR_CELESTE_BRILLANTE : new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.65f));
                if (enc) {
                    shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.28f); shapeRenderer.circle(px + 155f, dotY, 12f);
                    shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE); shapeRenderer.circle(px + 155f, dotY, 7f);
                } else {
                    shapeRenderer.setColor(0.20f, 0.26f, 0.35f, 1f); shapeRenderer.circle(px + 155f, dotY, 6f);
                }
            }
        } else {
            float pw = (ESQUEMA_ANCHO - 32f) / 2f;
            dibujarBloqueTutorial(ESQUEMA_X, TABLA_Y, pw, TABLA_ALTO, COLOR_NEON_CELESTE);
            dibujarBloqueTutorial(ESQUEMA_X + pw + 32f, TABLA_Y, pw, TABLA_ALTO, COLOR_NEON_CELESTE);
        }
        if (!tutorialActual.getConsejo().isEmpty()) {
            dibujarBloqueTutorial(ESQUEMA_X, CONSEJO_Y, ESQUEMA_ANCHO, CONSEJO_ALTO, COLOR_AMBAR_TUTO);
        }
    }

    private void dibujarPaginaMisionTextos() {
        fontTitulo.setColor(Color.WHITE);
        fontTitulo.draw(batch, tutorialActual.getMision(), TUTO_X + 100f, TUTO_Y + 560f, TUTO_ANCHO - 200f, Align.center, true);

        if (tutorialActual.tieneTablaVerdad()) {
            boolean[] tabla = tutorialActual.getTablaVerdad();
            float pw = 198f, gap = (ESQUEMA_ANCHO - 4f * pw) / 3f;
            fontPequena.setColor(COLOR_GRIS_CLARO);
            fontPequena.draw(batch, "Así responde la compuerta según las palancas A y B:", ESQUEMA_X, TABLA_Y + TABLA_ALTO + 30f);
            for (int r = 0; r < 4; r++) {
                float px = ESQUEMA_X + r * (pw + gap), ty = TABLA_Y + 24f;
                fontPequena.setColor(COLOR_GRIS_CLARO);
                fontPequena.draw(batch, "A", px + 33f, ty);
                fontPequena.draw(batch, "B", px + 79f, ty);
                fontPequena.setColor(tabla[r] ? COLOR_CELESTE_BRILLANTE : COLOR_GRIS_METAL);
                fontPequena.draw(batch, tabla[r] ? "LED ON" : "LED OFF", px + 128f, ty);
            }
        } else {
            float pw = (ESQUEMA_ANCHO - 32f) / 2f;
            dibujarTextoReglaTutorial(ESQUEMA_X, pw, "Entra por el (+) del LED y sale por el (-)");
            dibujarTextoReglaTutorial(ESQUEMA_X + pw + 32f, pw, "La resistencia va en la pata (-) del LED");
        }

        if (!tutorialActual.getConsejo().isEmpty()) {
            fontPequena.setColor(COLOR_AMBAR_TUTO);
            fontPequena.draw(batch, "CONSEJO", ESQUEMA_X + 30f, CONSEJO_Y + CONSEJO_ALTO - 18f);
            font.setColor(Color.WHITE);
            font.draw(batch, tutorialActual.getConsejo(), ESQUEMA_X + 30f, CONSEJO_Y + CONSEJO_ALTO - 46f, ESQUEMA_ANCHO - 60f, Align.left, true);
        }

        fontPequena.setColor(COLOR_GRIS_CLARO);
        String reloj = String.format("Tienes %.0f segundos. El circuito debe quedar encendido 5 segundos seguidos.", nivel.getTiempoLimite());
        glyphLayout.setText(fontPequena, reloj);
        fontPequena.draw(batch, reloj, TUTO_X + (TUTO_ANCHO - glyphLayout.width) / 2f, TUTO_Y + 160f);
    }

    private void dibujarTextoReglaTutorial(float x, float ancho, String texto) {
        font.setColor(COLOR_CELESTE_BRILLANTE);
        glyphLayout.setText(font, texto);
        font.draw(batch, texto, x + (ancho - glyphLayout.width) / 2f, TABLA_Y + (TABLA_ALTO + glyphLayout.height) / 2f);
    }

    private static final float RAMA_TRONCO_X = 140f, RAMA_INICIO_X = 220f, RAMA_FIN_X = 800f;

    private float[] calcularYRamas(float vy, int cantidad) {
        float[] ys = new float[cantidad];
        float paso = (ESQUEMA_ALTO - 60f) / cantidad;
        for (int i = 0; i < cantidad; i++) {
            ys[i] = vy + ESQUEMA_ALTO - 22f - paso * (i + 0.5f);
        }
        return ys;
    }

    private int contarColumnas(String[][] ramas) {
        int columnas = 1;
        for (String[] rama : ramas) columnas = Math.max(columnas, rama.length);
        return columnas;
    }

    private float xColumna(float vx, int col, int columnas) {
        if (columnas == 1) return vx + (RAMA_INICIO_X + RAMA_FIN_X) / 2f - 60f;
        return vx + RAMA_INICIO_X + col * (RAMA_FIN_X - 80f - RAMA_INICIO_X) / (columnas - 1);
    }

    private String tipoToken(String token) {
        int i = token.indexOf(':');
        return (i >= 0) ? token.substring(0, i) : token;
    }

    private String etiquetaToken(String token) {
        int i = token.indexOf(':');
        return (i >= 0) ? token.substring(i + 1) : "";
    }

    private void dibujarRamasShapes(float vx, float vy, String[][] ramas) {
        dibujarCajaBorde(vx, vy, ESQUEMA_ANCHO, ESQUEMA_ALTO, new Color(0.018f, 0.030f, 0.055f, 0.98f),
                new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.55f), 1.5f);
        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.06f);
        for (float gx = vx + 28f; gx < vx + ESQUEMA_ANCHO; gx += 28f) shapeRenderer.rectLine(gx, vy, gx, vy + ESQUEMA_ALTO, 1f);
        for (float gy = vy + 28f; gy < vy + ESQUEMA_ALTO; gy += 28f) shapeRenderer.rectLine(vx, gy, vx + ESQUEMA_ANCHO, gy, 1f);

        float fx = vx + 44f, fy = vy + 46f, fw = 68f, fh = 148f;
        dibujarCajaBorde(fx, fy, fw, fh, new Color(0.11f, 0.15f, 0.22f, 1f), COLOR_GRIS_METAL, 1.8f);
        float posPinX = fx + fw, posPinY = vy + 152f, negPinY = vy + 66f, yRet = vy + 22f;

        float[] ys = calcularYRamas(vy, ramas.length);
        int columnas = contarColumnas(ramas);
        float troncoX = vx + RAMA_TRONCO_X, finX = vx + RAMA_FIN_X;

        float yTroncoMin = posPinY, yTroncoMax = posPinY;
        for (int r = 0; r < ramas.length; r++) {
            String primero = tipoToken(ramas[r][0]);
            if (!primero.isEmpty() && !primero.equals("|")) {
                yTroncoMin = Math.min(yTroncoMin, ys[r]);
                yTroncoMax = Math.max(yTroncoMax, ys[r]);
            }
        }
        shapeRenderer.setColor(COLOR_NEON_CELESTE);
        shapeRenderer.rectLine(posPinX, posPinY, troncoX, posPinY, 3.2f);
        shapeRenderer.rectLine(troncoX, yTroncoMin, troncoX, yTroncoMax, 3.2f);

        float yFinMax = yRet;
        for (int r = 0; r < ramas.length; r++) {
            String[] rama = ramas[r];
            float y = ys[r];
            float xAnterior = -1f;
            int primeraCol = -1;
            for (int c = 0; c < rama.length; c++) {
                String tipo = tipoToken(rama[c]);
                if (tipo.isEmpty()) continue;
                float x = xColumna(vx, c, columnas);
                shapeRenderer.setColor(COLOR_NEON_CELESTE);
                if (tipo.equals("|")) {
                    shapeRenderer.rectLine(x, ys[r - 1], x, y, 3.0f);
                    shapeRenderer.circle(x, ys[r - 1], 5f);
                    if (xAnterior >= 0) shapeRenderer.rectLine(xAnterior, y, x, y, 3.0f);
                    xAnterior = x;
                    if (primeraCol < 0) primeraCol = c;
                    continue;
                }
                float medio = tipo.startsWith("LED") ? 6f : 34f;
                if (primeraCol < 0) {
                    primeraCol = c;
                    shapeRenderer.rectLine(troncoX, y, x - medio, y, 3.0f);
                } else {
                    shapeRenderer.rectLine(xAnterior, y, x - medio, y, 3.0f);
                }
                if (tipo.equals("SW")) {
                    dibujarCajaBorde(x - 34f, y - 16f, 68f, 32f, new Color(0.06f, 0.09f, 0.14f, 1f), COLOR_GRIS_METAL, 1.4f);
                    dibujarPalancaSwitch(x - 22f, y, x + 22f, y, true, 10f);
                }
                xAnterior = x + medio;
            }
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(xAnterior, y, finX, y, 3.0f);
            yFinMax = Math.max(yFinMax, y);
            if (r == 0) dibujarPulsoCableGuia(troncoX, y, finX, y, 0.0f);
        }

        shapeRenderer.setColor(0.20f, 0.68f, 1.0f, 0.90f);
        shapeRenderer.rectLine(finX, yFinMax, finX, yRet, 3.0f);
        shapeRenderer.rectLine(finX, yRet, troncoX - 10f, yRet, 3.0f);
        shapeRenderer.rectLine(troncoX - 10f, yRet, troncoX - 10f, negPinY, 3.0f);
        shapeRenderer.rectLine(troncoX - 10f, negPinY, posPinX, negPinY, 3.0f);
        dibujarPulsoCableGuia(finX, yRet, troncoX - 10f, yRet, 0.5f);

        shapeRenderer.setColor(0.95f, 0.25f, 0.25f, 1f); shapeRenderer.circle(posPinX, posPinY, 5.5f);
        shapeRenderer.setColor(0.25f, 0.62f, 1.0f, 1f); shapeRenderer.circle(posPinX, negPinY, 5.5f);
    }

    private void dibujarRamasTextos(float vx, float vy, String[][] ramas) {
        fontPequena.setColor(COLOR_CELESTE_BRILLANTE); fontPequena.draw(batch, "5V", vx + 64f, vy + 172f);
        fontPequena.setColor(1f, 0.45f, 0.45f, 1f); fontPequena.draw(batch, "+", vx + 94f, vy + 166f);
        fontPequena.setColor(0.45f, 0.78f, 1f, 1f); fontPequena.draw(batch, "-", vx + 96f, vy + 78f);

        float[] ys = calcularYRamas(vy, ramas.length);
        int columnas = contarColumnas(ramas);
        float tam = Math.min(104f, (ESQUEMA_ALTO - 60f) / ramas.length + 10f);
        for (int r = 0; r < ramas.length; r++) {
            for (int c = 0; c < ramas[r].length; c++) {
                String tipo = tipoToken(ramas[r][c]);
                if (tipo.isEmpty() || tipo.equals("|")) continue;
                float x = xColumna(vx, c, columnas), y = ys[r];
                dibujarIconoToken(tipo, x, y, tam);
                String etiqueta = etiquetaToken(ramas[r][c]);
                if (!etiqueta.isEmpty()) {
                    fontPequena.setColor(COLOR_GRIS_CLARO);
                    glyphLayout.setText(fontPequena, etiqueta);
                    fontPequena.draw(batch, etiqueta, x - glyphLayout.width / 2f, y + tam / 2f + 6f);
                }
            }
        }
    }

    private void dibujarIconoToken(String tipo, float x, float y, float tam) {
        if (tipo.equals("R")) {
            batch.draw(animResistenciaNormal.getKeyFrame(tiempoTotal, true), x - tam * 0.7f, y - tam * 0.45f, tam * 1.4f, tam * 0.9f);
        } else if (tipo.equals("POT")) {
            batch.draw(animPotenciometro.getKeyFrame(tiempoTotal, true), x - tam * 0.6f, y - tam * 0.5f, tam * 1.2f, tam * 1.2f);
        } else if (tipo.equals("555A") || tipo.equals("555M")) {
            Animation<TextureRegion> anim = tipo.equals("555A") ? anim555Astable : anim555Monoestable;
            batch.draw(anim.getKeyFrame(tiempoTotal, true), x - tam * 0.75f, y - tam * 0.75f, tam * 1.5f, tam * 1.5f);
        } else if (tipo.equals("CAP")) {
            batch.draw(animCapacitorCargado.getKeyFrame(tiempoTotal, true), x - tam * 0.55f, y - tam * 0.42f, tam * 1.1f, tam * 1.1f);
        } else if (tipo.startsWith("LED")) {
            batch.setColor(colorDeLed(tipo.startsWith("LED_") ? tipo.substring(4) : "AMARILLO"));
            batch.draw(animLedPrendiendo.getKeyFrame(tiempoTotal, true), x - tam / 2f, y - tam * 0.35f, tam, tam);
            batch.setColor(Color.WHITE);
        }
    }

    private void dibujarEsquemaShapes(float vx, float vy, TutorialNivel.Esquema esquema) {
        if (esquema == TutorialNivel.Esquema.RAMAS) {
            dibujarRamasShapes(vx, vy, tutorialActual.getRamas());
            return;
        }
        float vw = ESQUEMA_ANCHO, vh = ESQUEMA_ALTO;
        boolean esCompuerta = (esquema == TutorialNivel.Esquema.DIVISOR_AND || esquema == TutorialNivel.Esquema.DIVISOR_OR);
        boolean esAnd = (esquema == TutorialNivel.Esquema.DIVISOR_AND);
        boolean conSwitch = (esquema == TutorialNivel.Esquema.SERIE_CON_SWITCH);
        dibujarCajaBorde(vx, vy, vw, vh, new Color(0.018f, 0.030f, 0.055f, 0.98f),
                new Color(COLOR_GRIS_METAL.r, COLOR_GRIS_METAL.g, COLOR_GRIS_METAL.b, 0.55f), 1.5f);

        shapeRenderer.setColor(COLOR_NEON_CELESTE.r, COLOR_NEON_CELESTE.g, COLOR_NEON_CELESTE.b, 0.06f);
        for (float gx = vx + 28f; gx < vx + vw; gx += 28f) shapeRenderer.rectLine(gx, vy, gx, vy + vh, 1f);
        for (float gy = vy + 28f; gy < vy + vh; gy += 28f) shapeRenderer.rectLine(vx, gy, vx + vw, gy, 1f);

        float cy = vy + 128f, yRet = vy + 28f;
        float fx = vx + 44f, fy = vy + 46f, fw = 68f, fh = 148f;
        dibujarCajaBorde(fx, fy, fw, fh, new Color(0.11f, 0.15f, 0.22f, 1f), COLOR_GRIS_METAL, 1.8f);
        shapeRenderer.setColor(0.04f, 0.06f, 0.09f, 1f);
        shapeRenderer.rect(fx + 10f, fy + fh - 40f, fw - 20f, 26f);

        float posPinX = fx + fw, posPinY = cy + 24f, negPinY = vy + 66f;

        if (esCompuerta) {
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
            boolean cerrBGuia = esAnd;
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

            float ledX = vx + 450f, ledAnodoX = ledX + 56f, ledCatodoX = ledX + 68f, ledLegY = vy + 108f, pieY = vy + 90f;
            float resX = vx + 600f, resInX = resX + 28f, resOutX = resX + 104f, finX = vx + 790f;
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            shapeRenderer.rectLine(gateOutX, gateOutY, ledX - 22f, gateOutY, 3.2f);
            shapeRenderer.rectLine(ledX - 22f, gateOutY, ledX - 22f, pieY, 3.2f);
            shapeRenderer.rectLine(ledX - 22f, pieY, ledAnodoX, pieY, 3.2f);
            shapeRenderer.rectLine(ledAnodoX, pieY, ledAnodoX, ledLegY, 3.2f);

            shapeRenderer.setColor(0.20f, 0.68f, 1.0f, 0.90f);
            shapeRenderer.rectLine(ledCatodoX, ledLegY, ledCatodoX, pieY, 3.0f);
            shapeRenderer.rectLine(ledCatodoX, pieY, resInX, pieY, 3.0f);
            shapeRenderer.rectLine(resOutX, pieY, finX, pieY, 3.0f);
            shapeRenderer.rectLine(finX, pieY, finX, yRet, 3.0f);
            shapeRenderer.rectLine(finX, yRet, vx + 138f, yRet, 3.0f);
            shapeRenderer.rectLine(vx + 138f, yRet, vx + 138f, negPinY, 3.0f);
            shapeRenderer.rectLine(vx + 138f, negPinY, posPinX, negPinY, 3.0f);

            dibujarPulsoCableGuia(posPinX, posPinY, swX, cy, 0.0f);
            dibujarPulsoCableGuia(swX + swW, yA, gateInX, gateInAY, 0.25f);
            dibujarPulsoCableGuia(gateOutX, gateOutY, ledX - 22f, gateOutY, 0.50f);
            dibujarPulsoCableGuia(ledCatodoX, pieY, resInX, pieY, 0.72f);
            dibujarPulsoCableGuia(finX, yRet, vx + 138f, yRet, 0.40f);
        } else {
            float ledX = conSwitch ? vx + 360f : vx + 330f, ledAnodoX = ledX + 56f, ledCatodoX = ledX + 68f, ledLegY = vy + 108f, pieY = vy + 90f;
            float resX = ledX + 180f, resInX = resX + 28f, resOutX = resX + 104f, finX = resX + 190f;
            float bajadaX = ledX - 22f, inicioX = posPinX, inicioY = posPinY;
            shapeRenderer.setColor(COLOR_NEON_CELESTE);
            if (conSwitch) {
                float swX = vx + 195f, swY = cy - 26f;
                dibujarCajaBorde(swX, swY, 100f, 52f, new Color(0.06f, 0.09f, 0.14f, 1f), COLOR_GRIS_METAL, 1.6f);
                shapeRenderer.setColor(0.15f, 0.95f, 0.72f, 1f);
                shapeRenderer.rectLine(swX + 16f, cy, swX + 84f, cy, 4.0f);
                shapeRenderer.setColor(COLOR_NEON_CELESTE);
                shapeRenderer.rectLine(posPinX, posPinY, vx + 150f, posPinY, 3.2f);
                shapeRenderer.rectLine(vx + 150f, posPinY, vx + 150f, cy, 3.2f);
                shapeRenderer.rectLine(vx + 150f, cy, swX, cy, 3.2f);
                inicioX = swX + 100f;
                inicioY = cy;
            }
            shapeRenderer.rectLine(inicioX, inicioY, bajadaX, inicioY, 3.2f);
            shapeRenderer.rectLine(bajadaX, inicioY, bajadaX, pieY, 3.2f);
            shapeRenderer.rectLine(bajadaX, pieY, ledAnodoX, pieY, 3.2f);
            shapeRenderer.rectLine(ledAnodoX, pieY, ledAnodoX, ledLegY, 3.2f);

            shapeRenderer.setColor(0.20f, 0.68f, 1.0f, 0.90f);
            shapeRenderer.rectLine(ledCatodoX, ledLegY, ledCatodoX, pieY, 3.0f);
            shapeRenderer.rectLine(ledCatodoX, pieY, resInX, pieY, 3.0f);
            shapeRenderer.rectLine(resOutX, pieY, finX, pieY, 3.0f);
            shapeRenderer.rectLine(finX, pieY, finX, yRet, 3.0f);
            shapeRenderer.rectLine(finX, yRet, vx + 138f, yRet, 3.0f);
            shapeRenderer.rectLine(vx + 138f, yRet, vx + 138f, negPinY, 3.0f);
            shapeRenderer.rectLine(vx + 138f, negPinY, posPinX, negPinY, 3.0f);

            dibujarPulsoCableGuia(inicioX, inicioY, bajadaX, inicioY, 0.1f);
            dibujarPulsoCableGuia(ledCatodoX, pieY, resInX, pieY, 0.55f);
            dibujarPulsoCableGuia(finX, yRet, vx + 138f, yRet, 0.35f);
        }

        shapeRenderer.setColor(0.95f, 0.25f, 0.25f, 1f); shapeRenderer.circle(posPinX, posPinY, 5.5f);
        shapeRenderer.setColor(0.25f, 0.62f, 1.0f, 1f); shapeRenderer.circle(posPinX, negPinY, 5.5f);
    }

    private void dibujarEsquemaTextos(float vx, float vy, TutorialNivel.Esquema esquema) {
        if (esquema == TutorialNivel.Esquema.RAMAS) {
            dibujarRamasTextos(vx, vy, tutorialActual.getRamas());
            return;
        }
        boolean esCompuerta = (esquema == TutorialNivel.Esquema.DIVISOR_AND || esquema == TutorialNivel.Esquema.DIVISOR_OR);
        boolean esAnd = (esquema == TutorialNivel.Esquema.DIVISOR_AND);
        boolean conSwitch = (esquema == TutorialNivel.Esquema.SERIE_CON_SWITCH);
        fontPequena.setColor(COLOR_CELESTE_BRILLANTE); fontPequena.draw(batch, "5V", vx + 64f, vy + 172f);
        fontPequena.setColor(1f, 0.45f, 0.45f, 1f); fontPequena.draw(batch, "+", vx + 94f, vy + 166f);
        fontPequena.setColor(0.45f, 0.78f, 1f, 1f); fontPequena.draw(batch, "-", vx + 96f, vy + 78f);

        if (esCompuerta) {
            boolean esOr = !esAnd;
            batch.draw((esOr ? animOr : animAnd).getKeyFrame(tiempoTotal, true), vx + 304f, vy + 64f, 128f, 128f);
            fontPequena.setColor(COLOR_GRIS_CLARO);
            fontPequena.draw(batch, "SWITCH", vx + 183f, vy + 194f);
            fontPequena.draw(batch, esOr ? "OR" : "AND", vx + 354f, vy + 202f);
            fontPequena.setColor(COLOR_CELESTE_BRILLANTE);
            fontPequena.draw(batch, "A", vx + 260f, vy + 165f);
            fontPequena.draw(batch, "B", vx + 260f, vy + 100f);
            dibujarLedYResistenciaGuia(vx + 450f, vx + 600f, vy);
        } else {
            float ledX = conSwitch ? vx + 360f : vx + 330f;
            fontPequena.setColor(COLOR_GRIS_CLARO);
            if (conSwitch) fontPequena.draw(batch, "SWITCH", vx + 216f, vy + 172f);
            dibujarLedYResistenciaGuia(ledX, ledX + 180f, vy);
        }
    }

    private void dibujarLedYResistenciaGuia(float ledX, float resX, float vy) {
        batch.draw(animLedPrendiendo.getKeyFrame(tiempoTotal, true), ledX, vy + 66f, 126f, 126f);
        batch.draw(animResistenciaNormal.getKeyFrame(tiempoTotal, true), resX, vy + 48f, 132f, 84f);
        fontPequena.setColor(COLOR_GRIS_CLARO);
        fontPequena.draw(batch, "LED", ledX + 49f, vy + 202f);
        fontPequena.draw(batch, "220 Ohm", resX + 36f, vy + 150f);
        fontPequena.setColor(1f, 0.45f, 0.45f, 0.9f);
        fontPequena.draw(batch, "+", ledX + 30f, vy + 106f);
        fontPequena.setColor(0.45f, 0.78f, 1f, 0.9f);
        fontPequena.draw(batch, "-", ledX + 80f, vy + 106f);
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
        this.paginaTutorial = 0;
        if (!this.mostrandoGuiaNivel) saltarIntro();
        cancelarCable();
        sonar(TipoEfecto.CLICK_UI);
    }

    public void retrocederTutorial() {
        if (paginaTutorial > 0) {
            paginaTutorial--;
            sonar(TipoEfecto.CLICK_UI);
        }
    }

    private void irAPaginaTutorial(int pagina) {
        if (pagina != paginaTutorial) {
            paginaTutorial = pagina;
            sonar(TipoEfecto.CLICK_UI);
        }
    }

    public void avanzarTutorial() {
        if (paginaTutorial < TOTAL_PAGINAS - 1) {
            paginaTutorial++;
            sonar(TipoEfecto.CLICK_UI);
        } else {
            cerrarGuiaNivel();
        }
    }

    public void cerrarGuiaNivel() {
        if (this.mostrandoGuiaNivel) {
            this.mostrandoGuiaNivel = false;
            saltarIntro();
            sonar(TipoEfecto.CLICK_UI);
        }
    }

    public boolean manejarClicUI(float mx, float my) {
        ratonMundo.set(mx, my);
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
            if (estaSobre(TUTO_SIGUIENTE_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO)) {
                avanzarTutorial();
            } else if (estaSobre(TUTO_ATRAS_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO)) {
                if (paginaTutorial == 0) cerrarGuiaNivel();
                else retrocederTutorial();
            } else {
                for (int i = 0; i < TOTAL_PAGINAS; i++) {
                    if (estaSobre(getXPestana(i), PESTANA_Y, PESTANA_ANCHO, PESTANA_ALTO)) irAPaginaTutorial(i);
                }
            }
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

        if (mostrandoGuiaNivel) {
            int botonHover = -1;
            if (estaSobre(TUTO_SIGUIENTE_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO)) botonHover = 20;
            else if (estaSobre(TUTO_ATRAS_X, TUTO_BOTON_Y, TUTO_BOTON_ANCHO, TUTO_BOTON_ALTO)) botonHover = 21;
            for (int i = 0; i < TOTAL_PAGINAS; i++) {
                if (estaSobre(getXPestana(i), PESTANA_Y, PESTANA_ANCHO, PESTANA_ALTO)) botonHover = 22 + i;
            }
            if (botonHover != ultimoUIHover) {
                if (botonHover > 0) sonar(TipoEfecto.HOVER_UI);
                ultimoUIHover = botonHover;
            }
            return;
        }

        if (terminalBajoRaton != ultimoTerminalHover) {
            if (terminalBajoRaton != null) sonar(TipoEfecto.HOVER_UI);
            ultimoTerminalHover = terminalBajoRaton;
        }

        int slotHover = -1;
        int casilla = obtenerIndiceCasillaEn(mx, my);
        if (casilla >= 0) {
            slotHover = 30 + casilla;
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
        this.mostrandoGuiaNivel = (nuevoNivel != null);
        this.paginaTutorial = 0;
        this.tutorialActual = (nuevoNivel != null) ? nuevoNivel.obtenerTutorial() : null;
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
        if (fontTutorial != null) fontTutorial.dispose();
        if (texturasCargadas != null) {
            for (Texture t : texturasCargadas) {
                if (t != null) t.dispose();
            }
            texturasCargadas.clear();
        }
    }
}
