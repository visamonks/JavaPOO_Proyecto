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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.schematic.JuegoPrincipal;
import com.schematic.audio.GestorAudio;
import com.schematic.constants.Constantes;
import com.schematic.model.niveles.GestorNiveles;
import com.schematic.model.niveles.Nivel;
import com.schematic.model.progreso.GestorPersistenciaJSON;
import com.schematic.model.progreso.ProgresoJugador;

import java.util.ArrayList;
import java.util.List;

public class VistaSeleccionNivel extends ScreenAdapter {

    private final Game juego;
    private final GestorAudio gestorAudio;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout layout;

    private final OrthographicCamera camara;
    private final Viewport viewport;

    private final BitmapFont fontTitulo;
    private final BitmapFont fontSubtitulo;
    private final BitmapFont fontCardTitulo;
    private final BitmapFont fontCardDetalle;
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

    private static final int CANTIDAD_NIVELES = GestorNiveles.TOTAL_NIVELES;
    private static final float CARD_ANCHO = 310f;
    private static final float CARD_ALTO = 460f;
    private static final float CARD_ESPACIADO = 28f;
    private static final float CARDS_TOTAL_ANCHO = (CANTIDAD_NIVELES * CARD_ANCHO) + ((CANTIDAD_NIVELES - 1) * CARD_ESPACIADO);
    private static final float CARDS_INICIO_X = (ANCHO_VIRTUAL - CARDS_TOTAL_ANCHO) / 2f;
    private static final float CARDS_BASE_Y = 250f;

    public static class InfoNivel {
        public final int numero;
        public final String titulo;
        public final String categoria;
        public final String tiempo;
        public final String componentes;
        public final String descripcion;

        public InfoNivel(int numero, String titulo, String categoria, String tiempo, String componentes, String descripcion) {
            this.numero = numero;
            this.titulo = titulo;
            this.categoria = categoria;
            this.tiempo = tiempo;
            this.componentes = componentes;
            this.descripcion = descripcion;
        }
    }

    private final InfoNivel[] datosNiveles = new InfoNivel[CANTIDAD_NIVELES];
    private final float[] animHoverCards = new float[CANTIDAD_NIVELES];

    private ProgresoJugador progreso;
    private int nivelSeleccionado = 0;
    private int indiceHover = -1;
    private boolean modoLibreDesbloqueado = false;

    private float tiempoTotal = 0f;
    private float offsetGrid = 0f;
    private float animVolver = 0f;
    private float animIniciar = 0f;
    private float animModoLibre = 0f;

    private float electronX;
    private float electronY;
    private float targetElectronX;
    private float targetElectronY;
    private float saltoElectron = 0f;
    private float giroElectron = 0f;
    private float tiempoParpadeo = 0f;
    private boolean parpadeando = false;
    private int clicsElectron = 0;
    private static final float RADIO_INTERACCION_ELECTRON = 65f;

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

    private boolean enTransicion = false;
    private float tiempoTransicion = 0f;
    private static final float DURACION_TRANSICION = 0.70f;
    private int nivelParaIniciar = 1;

    public VistaSeleccionNivel() {
        this((Game) Gdx.app.getApplicationListener());
    }

    public VistaSeleccionNivel(Game juego) {
        this.juego = juego;
        this.gestorAudio = GestorAudio.obtenerDesde(juego);
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
        this.fontCardTitulo = crearFuente(1.35f);
        this.fontCardDetalle = crearFuente(0.98f);
        this.fontPequena = crearFuente(0.85f);
        this.fontBotones = crearFuente(1.30f);

        inicializarDatosNiveles();
        cargarProgresoJugador();

        this.electronX = CARDS_INICIO_X + CARD_ANCHO / 2f;
        this.electronY = CARDS_BASE_Y + CARD_ALTO + 70f;
        this.targetElectronX = this.electronX;
        this.targetElectronY = this.electronY;
    }

    private BitmapFont crearFuente(float escala) {
        BitmapFont f = new BitmapFont();
        f.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        f.getData().setScale(escala);
        return f;
    }

    private void inicializarDatosNiveles() {
        datosNiveles[0] = new InfoNivel(
                1,
                "ENCIENDE EL LED",
                "BASICO",
                "30s",
                "Fuente DC - Resistencia 220 Ohm - LED",
                "La base de la electronica. Conecta la fuente al LED intercalando una resistencia para protegerlo."
        );
        datosNiveles[1] = new InfoNivel(
                2,
                "CIRCUITO CON SWITCH",
                "INTERMEDIO",
                "30s",
                "Fuente DC - Interruptor - Resistencia - LED",
                "Aprende a controlar la corriente. Un interruptor en serie permite abrir o cerrar el paso de electrones."
        );
        datosNiveles[2] = new InfoNivel(
                3,
                "DOBLE SWITCH (AND)",
                "LOGICA I",
                "30s",
                "Fuente DC - 2x Interruptores - Resistencia - LED",
                "Logica digital en serie: conecta dos interruptores en serie. Ambos deben cerrarse para encender el LED."
        );
        datosNiveles[3] = new InfoNivel(
                4,
                "COMPUERTA OR",
                "LOGICA II",
                "30s",
                "Fuente - Compuerta OR 7432 - LEDs",
                "Logica inclusiva: permite el flujo de energia con que AL MENOS UNA de las ramas este activa."
        );
        datosNiveles[4] = new InfoNivel(
                5,
                "CIRCUITO FINAL",
                "MAESTRO",
                "35s",
                "Sistema Integral - Compuertas - Diodos",
                "El examen definitivo de esquematicos. Integra compuertas, interruptores y protecciones contrarreloj."
        );
    }

    private void cargarProgresoJugador() {
        if (juego instanceof JuegoPrincipal) {
            this.progreso = ((JuegoPrincipal) juego).getProgreso();
        } else {
            this.progreso = GestorPersistenciaJSON.cargar();
        }
        if (this.progreso == null) {
            this.progreso = new ProgresoJugador(1);
        }
    }

    private boolean estaDesbloqueado(int index) {
        if (modoLibreDesbloqueado) return true;
        int nivelNumero = index + 1;
        return progreso != null && nivelNumero <= progreso.getNivelMaximoDesbloqueado();
    }

    private boolean estaCompletado(int index) {
        int nivelNumero = index + 1;
        return progreso != null && nivelNumero < progreso.getNivelMaximoDesbloqueado();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT || enTransicion) return false;

                Vector3 mundo = viewport.unproject(new Vector3(screenX, screenY, 0));
                float mx = mundo.x;
                float my = mundo.y;

                float distElectron = Vector2.dst(mx, my, electronX, electronY);
                if (distElectron <= RADIO_INTERACCION_ELECTRON) {
                    acariciarElectron();
                    return true;
                }

                for (int i = 0; i < CANTIDAD_NIVELES; i++) {
                    float bx = CARDS_INICIO_X + i * (CARD_ANCHO + CARD_ESPACIADO);
                    float by = CARDS_BASE_Y + (animHoverCards[i] * 12f);
                    if (estaDentro(mx, my, bx, by, CARD_ANCHO, CARD_ALTO)) {
                        if (nivelSeleccionado == i && estaDesbloqueado(i)) {
                            iniciarNivel(i + 1);
                        } else {
                            seleccionarNivel(i);
                        }
                        return true;
                    }
                }

                float volverX = 70f;
                float volverY = 60f;
                float volverW = 280f;
                float volverH = 64f;
                if (estaDentro(mx, my, volverX, volverY, volverW, volverH)) {
                    volverAlMenu();
                    return true;
                }

                float iniciarW = 460f;
                float iniciarH = 74f;
                float iniciarX = (ANCHO_VIRTUAL - iniciarW) / 2f;
                float iniciarY = 60f;
                if (estaDentro(mx, my, iniciarX, iniciarY, iniciarW, iniciarH)) {
                    if (estaDesbloqueado(nivelSeleccionado)) {
                        iniciarNivel(nivelSeleccionado + 1);
                    } else {
                        reaccionarNivelBloqueado();
                    }
                    return true;
                }

                float modoW = 260f;
                float modoH = 50f;
                float modoX = ANCHO_VIRTUAL - modoW - 70f;
                float modoY = ALTO_VIRTUAL - 80f;
                if (estaDentro(mx, my, modoX, modoY, modoW, modoH)) {
                    modoLibreDesbloqueado = !modoLibreDesbloqueado;
                    generarChispas(electronX, electronY, 15, modoLibreDesbloqueado ? COLOR_VERDE : COLOR_AMBAR);
                    return true;
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (enTransicion) return false;

                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
                    int nuevo = (nivelSeleccionado - 1 + CANTIDAD_NIVELES) % CANTIDAD_NIVELES;
                    seleccionarNivel(nuevo);
                    return true;
                }
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
                    int nuevo = (nivelSeleccionado + 1) % CANTIDAD_NIVELES;
                    seleccionarNivel(nuevo);
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    if (estaDesbloqueado(nivelSeleccionado)) {
                        iniciarNivel(nivelSeleccionado + 1);
                    } else {
                        reaccionarNivelBloqueado();
                    }
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                    volverAlMenu();
                    return true;
                }
                if (keycode == Input.Keys.U) {
                    modoLibreDesbloqueado = !modoLibreDesbloqueado;
                    generarChispas(electronX, electronY, 15, modoLibreDesbloqueado ? COLOR_VERDE : COLOR_AMBAR);
                    return true;
                }

                return false;
            }
        });
    }

    private void seleccionarNivel(int index) {
        this.nivelSeleccionado = MathUtils.clamp(index, 0, CANTIDAD_NIVELES - 1);
        saltoElectron = 10f;
        generarChispas(electronX, electronY, 8, COLOR_NEON_CYAN);
    }

    private void acariciarElectron() {
        clicsElectron++;
        saltoElectron = 24f;
        giroElectron = 360f;
        generarChispas(electronX, electronY, 16, COLOR_CELESTE_BRILLANTE);
    }

    private void reaccionarNivelBloqueado() {
        saltoElectron = 12f;
        generarChispas(electronX, electronY, 12, COLOR_ROJO);
    }

    private void iniciarNivel(int nivelNum) {
        if (enTransicion) return;
        enTransicion = true;
        tiempoTransicion = 0f;
        nivelParaIniciar = nivelNum;
        saltoElectron = 35f;
        generarChispas(electronX, electronY, 30, COLOR_CELESTE_BRILLANTE);
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
        offsetGrid = (offsetGrid + delta * 30f) % 60f;

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
        dibujarBasesTarjetas(mx, my);
        dibujarEsquemasEnTarjetas();
        dibujarBotonVolverShape(mx, my);
        dibujarBotonIniciarShape(mx, my);
        dibujarBotonModoLibreShape(mx, my);
        dibujarChispas();
        dibujarElectronMascota(mx, my);
        if (enTransicion) {
            dibujarEfectoTransicion();
        }
        shapeRenderer.end();

        batch.begin();
        dibujarEncabezado();
        dibujarTextosTarjetas();
        dibujarTextosBotones();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (enTransicion && tiempoTransicion >= DURACION_TRANSICION) {
            Nivel nivelInstancia = GestorNiveles.crearNivel(nivelParaIniciar);
            juego.setScreen(new VistaNivel(nivelInstancia, juego));
        }
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

        indiceHover = -1;
        for (int i = 0; i < CANTIDAD_NIVELES; i++) {
            float bx = CARDS_INICIO_X + i * (CARD_ANCHO + CARD_ESPACIADO);
            float by = CARDS_BASE_Y;
            boolean hovered = estaDentro(mx, my, bx, by, CARD_ANCHO, CARD_ALTO);
            if (hovered) indiceHover = i;

            float targetAnim = (hovered || nivelSeleccionado == i) ? 1.0f : 0.0f;
            animHoverCards[i] = MathUtils.lerp(animHoverCards[i], targetAnim, delta * 10f);
        }

        int indexDestino = (indiceHover >= 0) ? indiceHover : nivelSeleccionado;
        float cardCentroX = CARDS_INICIO_X + indexDestino * (CARD_ANCHO + CARD_ESPACIADO) + (CARD_ANCHO / 2f);
        float cardTopY = CARDS_BASE_Y + CARD_ALTO + 72f;

        float floatOffset = MathUtils.sin(tiempoTotal * 4.2f) * 10f;
        targetElectronX = cardCentroX;
        targetElectronY = cardTopY + floatOffset + saltoElectron;

        electronX = MathUtils.lerp(electronX, targetElectronX, delta * 7.5f);
        electronY = MathUtils.lerp(electronY, targetElectronY, delta * 7.5f);

        boolean hoverVolver = estaDentro(mx, my, 70f, 60f, 280f, 64f);
        animVolver = MathUtils.lerp(animVolver, hoverVolver ? 1f : 0f, delta * 9f);

        float iniciarW = 460f;
        float iniciarH = 74f;
        float iniciarX = (ANCHO_VIRTUAL - iniciarW) / 2f;
        boolean hoverIniciar = estaDentro(mx, my, iniciarX, 60f, iniciarW, iniciarH);
        animIniciar = MathUtils.lerp(animIniciar, hoverIniciar ? 1f : 0f, delta * 9f);

        float modoW = 260f;
        float modoH = 50f;
        float modoX = ANCHO_VIRTUAL - modoW - 70f;
        float modoY = ALTO_VIRTUAL - 80f;
        boolean hoverModo = estaDentro(mx, my, modoX, modoY, modoW, modoH);
        animModoLibre = MathUtils.lerp(animModoLibre, hoverModo ? 1f : 0f, delta * 9f);

        for (int i = chispas.size() - 1; i >= 0; i--) {
            ChispaParticula p = chispas.get(i);
            p.vida -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            if (p.vida <= 0f) {
                chispas.remove(i);
            }
        }

        if (enTransicion) {
            tiempoTransicion += delta;
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
        float yConductor = CARDS_BASE_Y - 24f;
        shapeRenderer.setColor(0.08f, 0.18f, 0.28f, 0.85f);
        shapeRenderer.rectLine(CARDS_INICIO_X - 40f, yConductor, CARDS_INICIO_X + CARDS_TOTAL_ANCHO + 40f, yConductor, 6f);

        float pulsoX = CARDS_INICIO_X - 40f + ((tiempoTotal * 380f) % (CARDS_TOTAL_ANCHO + 80f));
        shapeRenderer.setColor(COLOR_NEON_CYAN.r, COLOR_NEON_CYAN.g, COLOR_NEON_CYAN.b, 0.9f);
        shapeRenderer.rectLine(pulsoX - 25f, yConductor, pulsoX + 25f, yConductor, 4f);
        shapeRenderer.circle(pulsoX, yConductor, 5f);
    }

    private void dibujarBasesTarjetas(float mx, float my) {
        for (int i = 0; i < CANTIDAD_NIVELES; i++) {
            float x = CARDS_INICIO_X + i * (CARD_ANCHO + CARD_ESPACIADO);
            float y = CARDS_BASE_Y + (animHoverCards[i] * 14f);
            boolean sel = (nivelSeleccionado == i);
            boolean desb = estaDesbloqueado(i);
            boolean comp = estaCompletado(i);

            if (sel || animHoverCards[i] > 0.05f) {
                Color colAura = desb ? (comp ? COLOR_AMBAR : COLOR_NEON_CYAN) : COLOR_ROJO;
                float opac = 0.22f + (animHoverCards[i] * 0.18f);
                shapeRenderer.setColor(colAura.r, colAura.g, colAura.b, opac);
                shapeRenderer.rect(x - 8f, y - 8f, CARD_ANCHO + 16f, CARD_ALTO + 16f);
            }

            shapeRenderer.setColor(COLOR_CARD_FONDO);
            shapeRenderer.rect(x, y, CARD_ANCHO, CARD_ALTO);

            Color colHeader = desb ? (comp ? new Color(0.25f, 0.20f, 0.05f, 0.9f) : new Color(0.04f, 0.18f, 0.26f, 0.9f))
                                   : new Color(0.12f, 0.06f, 0.06f, 0.9f);
            shapeRenderer.setColor(colHeader);
            shapeRenderer.rect(x, y + CARD_ALTO - 68f, CARD_ANCHO, 68f);

            Color colBorde = desb ? (sel ? COLOR_NEON_CYAN : (comp ? COLOR_AMBAR : COLOR_CARD_BORDE))
                                  : new Color(0.24f, 0.26f, 0.32f, 0.6f);
            float grosor = (sel || animHoverCards[i] > 0.1f) ? 3.5f : 2.0f;
            shapeRenderer.setColor(colBorde);
            dibujarRectContorno(x, y, CARD_ANCHO, CARD_ALTO, grosor);

            float diagX = x + 18f;
            float diagY = y + 175f;
            float diagW = CARD_ANCHO - 36f;
            float diagH = 155f;

            shapeRenderer.setColor(0.02f, 0.04f, 0.08f, 0.96f);
            shapeRenderer.rect(diagX, diagY, diagW, diagH);
            shapeRenderer.setColor(colBorde.r, colBorde.g, colBorde.b, 0.45f);
            dibujarRectContorno(diagX, diagY, diagW, diagH, 1.5f);

            float infoX = x + 18f;
            float infoY = y + 68f;
            float infoW = CARD_ANCHO - 36f;
            float infoH = 92f;
            shapeRenderer.setColor(0.03f, 0.06f, 0.10f, 0.70f);
            shapeRenderer.rect(infoX, infoY, infoW, infoH);

            float btnPieX = x + 18f;
            float btnPieY = y + 14f;
            float btnPieW = CARD_ANCHO - 36f;
            float btnPieH = 42f;

            if (desb) {
                Color colBtnPie = sel ? COLOR_NEON_CYAN : new Color(0.08f, 0.22f, 0.32f, 0.9f);
                shapeRenderer.setColor(colBtnPie);
                shapeRenderer.rect(btnPieX, btnPieY, btnPieW, btnPieH);
            } else {
                shapeRenderer.setColor(0.14f, 0.16f, 0.20f, 0.8f);
                shapeRenderer.rect(btnPieX, btnPieY, btnPieW, btnPieH);
            }
        }
    }

    private void dibujarRectContorno(float rx, float ry, float rw, float rh, float grosor) {
        shapeRenderer.rectLine(rx, ry, rx + rw, ry, grosor);
        shapeRenderer.rectLine(rx + rw, ry, rx + rw, ry + rh, grosor);
        shapeRenderer.rectLine(rx + rw, ry + rh, rx, ry + rh, grosor);
        shapeRenderer.rectLine(rx, ry + rh, rx, ry, grosor);
    }

    private void dibujarEsquemasEnTarjetas() {
        for (int i = 0; i < CANTIDAD_NIVELES; i++) {
            float x = CARDS_INICIO_X + i * (CARD_ANCHO + CARD_ESPACIADO);
            float y = CARDS_BASE_Y + (animHoverCards[i] * 14f);
            float cx = x + (CARD_ANCHO / 2f);
            float cy = y + 175f + (155f / 2f);
            boolean sel = (nivelSeleccionado == i);
            boolean desb = estaDesbloqueado(i);

            if (!desb) {
                dibujarIconoCandado(cx, cy);
                continue;
            }

            switch (i) {
                case 0:
                    dibujarEsquemaLedResistencia(cx, cy, sel);
                    break;
                case 1:
                    dibujarEsquemaSwitch(cx, cy, sel);
                    break;
                case 2:
                    dibujarEsquemaCompuertaAND(cx, cy, sel);
                    break;
                case 3:
                    dibujarEsquemaCompuertaOR(cx, cy, sel);
                    break;
                case 4:
                    dibujarEsquemaCircuitoCombinado(cx, cy, sel);
                    break;
            }
        }
    }

    private void dibujarIconoCandado(float cx, float cy) {
        shapeRenderer.setColor(0.35f, 0.40f, 0.48f, 0.9f);
        shapeRenderer.rect(cx - 24f, cy - 28f, 48f, 38f);

        shapeRenderer.setColor(0.50f, 0.58f, 0.68f, 0.9f);
        shapeRenderer.rectLine(cx - 15f, cy + 10f, cx - 15f, cy + 26f, 5f);
        shapeRenderer.rectLine(cx + 15f, cy + 10f, cx + 15f, cy + 26f, 5f);
        shapeRenderer.rectLine(cx - 15f, cy + 26f, cx + 15f, cy + 26f, 5f);

        shapeRenderer.setColor(0.08f, 0.10f, 0.15f, 1f);
        shapeRenderer.circle(cx, cy - 6f, 5f);
        shapeRenderer.rect(cx - 2.5f, cy - 18f, 5f, 12f);
    }

    private void dibujarEsquemaLedResistencia(float cx, float cy, boolean sel) {
        Color colLinea = sel ? COLOR_NEON_CYAN : COLOR_CELESTE_BRILLANTE;
        shapeRenderer.setColor(colLinea);

        shapeRenderer.rectLine(cx - 95f, cy, cx - 45f, cy, 3f);

        shapeRenderer.setColor(COLOR_AMBAR);
        float rx = cx - 45f;
        shapeRenderer.rectLine(rx, cy, rx + 10f, cy + 16f, 3f);
        shapeRenderer.rectLine(rx + 10f, cy + 16f, rx + 20f, cy - 16f, 3f);
        shapeRenderer.rectLine(rx + 20f, cy - 16f, rx + 30f, cy + 16f, 3f);
        shapeRenderer.rectLine(rx + 30f, cy + 16f, rx + 40f, cy, 3f);

        shapeRenderer.setColor(colLinea);
        shapeRenderer.rectLine(cx - 5f, cy, cx + 25f, cy, 3f);

        shapeRenderer.setColor(COLOR_VERDE);
        float lx = cx + 25f;
        shapeRenderer.triangle(lx, cy - 18f, lx, cy + 18f, lx + 30f, cy);
        shapeRenderer.rectLine(lx + 30f, cy - 18f, lx + 30f, cy + 18f, 4f);

        float pulsoLuz = 1f + MathUtils.sin(tiempoTotal * 8f) * 0.2f;
        shapeRenderer.rectLine(lx + 18f, cy + 18f, lx + 30f * pulsoLuz, cy + 30f * pulsoLuz, 2.5f);
        shapeRenderer.rectLine(lx + 26f, cy + 12f, lx + 38f * pulsoLuz, cy + 24f * pulsoLuz, 2.5f);

        shapeRenderer.setColor(colLinea);
        shapeRenderer.rectLine(lx + 30f, cy, cx + 95f, cy, 3f);
    }

    private void dibujarEsquemaSwitch(float cx, float cy, boolean sel) {
        Color colLinea = sel ? COLOR_NEON_CYAN : COLOR_CELESTE_BRILLANTE;
        shapeRenderer.setColor(colLinea);

        shapeRenderer.rectLine(cx - 95f, cy, cx - 40f, cy, 3f);

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(cx - 40f, cy, 6f);
        shapeRenderer.circle(cx + 25f, cy, 6f);

        float osc = MathUtils.sin(tiempoTotal * 4.5f) * 6f;
        shapeRenderer.setColor(COLOR_AMBAR);
        shapeRenderer.rectLine(cx - 40f, cy, cx + 20f, cy + 26f + osc, 4f);

        shapeRenderer.setColor(colLinea);
        shapeRenderer.rectLine(cx + 25f, cy, cx + 95f, cy, 3f);

        shapeRenderer.setColor(COLOR_VERDE);
        shapeRenderer.circle(cx + 65f, cy, 8f);
    }

    private void dibujarEsquemaCompuertaAND(float cx, float cy, boolean sel) {
        Color colLinea = sel ? COLOR_NEON_CYAN : COLOR_CELESTE_BRILLANTE;

        shapeRenderer.setColor(COLOR_VERDE);
        shapeRenderer.rectLine(cx - 85f, cy + 18f, cx - 35f, cy + 18f, 3.5f);
        shapeRenderer.rectLine(cx - 85f, cy - 18f, cx - 35f, cy - 18f, 3.5f);

        shapeRenderer.circle(cx - 85f, cy + 18f, 5f);
        shapeRenderer.circle(cx - 85f, cy - 18f, 5f);

        shapeRenderer.setColor(0.10f, 0.35f, 0.55f, 0.95f);
        shapeRenderer.rect(cx - 35f, cy - 32f, 35f, 64f);
        shapeRenderer.arc(cx, cy, 32f, -90f, 180f);

        shapeRenderer.setColor(colLinea);
        shapeRenderer.rectLine(cx - 35f, cy - 32f, cx - 35f, cy + 32f, 3.5f);

        shapeRenderer.setColor(colLinea);
        shapeRenderer.rectLine(cx + 32f, cy, cx + 85f, cy, 3.5f);
        shapeRenderer.circle(cx + 85f, cy, 5f);
    }

    private void dibujarEsquemaCompuertaOR(float cx, float cy, boolean sel) {
        Color colLinea = sel ? COLOR_NEON_CYAN : COLOR_CELESTE_BRILLANTE;

        shapeRenderer.setColor(COLOR_AMBAR);
        shapeRenderer.rectLine(cx - 85f, cy + 18f, cx - 25f, cy + 18f, 3.5f);
        shapeRenderer.rectLine(cx - 85f, cy - 18f, cx - 25f, cy - 18f, 3.5f);
        shapeRenderer.circle(cx - 85f, cy + 18f, 5f);
        shapeRenderer.circle(cx - 85f, cy - 18f, 5f);

        shapeRenderer.setColor(0.20f, 0.28f, 0.45f, 0.95f);
        shapeRenderer.triangle(cx - 30f, cy + 34f, cx - 30f, cy - 34f, cx + 38f, cy);

        shapeRenderer.setColor(colLinea);
        shapeRenderer.rectLine(cx - 30f, cy - 34f, cx - 30f, cy + 34f, 3.5f);

        shapeRenderer.rectLine(cx + 38f, cy, cx + 85f, cy, 3.5f);
        shapeRenderer.circle(cx + 85f, cy, 5f);
    }

    private void dibujarEsquemaCircuitoCombinado(float cx, float cy, boolean sel) {
        shapeRenderer.setColor(0.12f, 0.16f, 0.24f, 0.95f);
        shapeRenderer.rect(cx - 48f, cy - 34f, 96f, 68f);

        Color colPin = sel ? COLOR_NEON_CYAN : COLOR_AMBAR;
        shapeRenderer.setColor(colPin);
        dibujarRectContorno(cx - 48f, cy - 34f, 96f, 68f, 2.5f);

        for (int p = 0; p < 4; p++) {
            float py = cy - 24f + (p * 16f);
            shapeRenderer.rectLine(cx - 68f, py, cx - 48f, py, 3f);
            shapeRenderer.rectLine(cx + 48f, py, cx + 68f, py, 3f);
        }

        float pulsoChip = 1f + MathUtils.sin(tiempoTotal * 10f) * 0.15f;
        shapeRenderer.setColor(COLOR_VERDE.r, COLOR_VERDE.g, COLOR_VERDE.b, 0.9f);
        shapeRenderer.circle(cx, cy, 14f * pulsoChip);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(cx, cy, 6f);
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
        } else if (saltoElectron > 6f) {
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

    private void dibujarBotonVolverShape(float mx, float my) {
        float vx = 70f;
        float vy = 60f;
        float vw = 280f;
        float vh = 64f;

        shapeRenderer.setColor(0.04f, 0.08f, 0.14f, 0.92f);
        shapeRenderer.rect(vx, vy, vw, vh);

        Color colBorde = (animVolver > 0.05f) ? COLOR_NEON_CYAN : COLOR_CARD_BORDE;
        shapeRenderer.setColor(colBorde);
        dibujarRectContorno(vx, vy, vw, vh, (animVolver > 0.05f) ? 3.0f : 1.8f);
    }

    private void dibujarBotonIniciarShape(float mx, float my) {
        float iw = 460f;
        float ih = 74f;
        float ix = (ANCHO_VIRTUAL - iw) / 2f;
        float iy = 60f;

        boolean desb = estaDesbloqueado(nivelSeleccionado);
        if (desb) {
            Color colBase = (animIniciar > 0.05f) ? new Color(0.00f, 0.70f, 0.85f, 1f) : new Color(0.00f, 0.50f, 0.65f, 1f);
            shapeRenderer.setColor(colBase);
            shapeRenderer.rect(ix, iy, iw, ih);

            shapeRenderer.setColor(COLOR_CELESTE_BRILLANTE);
            dibujarRectContorno(ix, iy, iw, ih, (animIniciar > 0.05f) ? 3.5f : 2.2f);
        } else {
            shapeRenderer.setColor(0.12f, 0.15f, 0.20f, 0.85f);
            shapeRenderer.rect(ix, iy, iw, ih);

            shapeRenderer.setColor(0.35f, 0.40f, 0.48f, 0.7f);
            dibujarRectContorno(ix, iy, iw, ih, 1.8f);
        }
    }

    private void dibujarBotonModoLibreShape(float mx, float my) {
        float mw = 260f;
        float mh = 50f;
        float mxPos = ANCHO_VIRTUAL - mw - 70f;
        float myPos = ALTO_VIRTUAL - 80f;

        shapeRenderer.setColor(0.04f, 0.08f, 0.14f, 0.85f);
        shapeRenderer.rect(mxPos, myPos, mw, mh);

        Color colBorde = modoLibreDesbloqueado ? COLOR_VERDE : COLOR_CARD_BORDE;
        shapeRenderer.setColor(colBorde);
        dibujarRectContorno(mxPos, myPos, mw, mh, modoLibreDesbloqueado ? 2.5f : 1.5f);
    }

    private void dibujarChispas() {
        for (ChispaParticula p : chispas) {
            float alfa = p.vida / p.vidaMax;
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, alfa);
            shapeRenderer.circle(p.x, p.y, 3.5f * alfa + 1.2f);
        }
    }

    private void dibujarEfectoTransicion() {
        float progreso = MathUtils.clamp(tiempoTransicion / DURACION_TRANSICION, 0f, 1f);

        float alfa = (progreso < 0.5f) ? (progreso * 2f) : (1.0f - (progreso - 0.5f) * 2f);
        shapeRenderer.setColor(COLOR_NEON_CYAN.r, COLOR_NEON_CYAN.g, COLOR_NEON_CYAN.b, alfa * 0.85f);
        shapeRenderer.rect(0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        shapeRenderer.setColor(1f, 1f, 1f, alfa);
        shapeRenderer.rectLine(0, ALTO_VIRTUAL / 2f, ANCHO_VIRTUAL, ALTO_VIRTUAL / 2f, 20f * alfa);
    }

    private void dibujarEncabezado() {
        fontTitulo.setColor(COLOR_NEON_CYAN);
        layout.setText(fontTitulo, "SELECCION DE NIVEL");
        float tituloX = (ANCHO_VIRTUAL - layout.width) / 2f;
        fontTitulo.draw(batch, "SELECCION DE NIVEL", tituloX, ALTO_VIRTUAL - 75f);

        fontSubtitulo.setColor(COLOR_TEXTO_MUTED);
        String subTexto = "Diagramas Esquematicos y Circuitos - Elige tu reto";
        layout.setText(fontSubtitulo, subTexto);
        float subX = (ANCHO_VIRTUAL - layout.width) / 2f;
        fontSubtitulo.draw(batch, subTexto, subX, ALTO_VIRTUAL - 135f);

        int maxDesbloqueado = (progreso != null) ? progreso.getNivelMaximoDesbloqueado() : 1;
        int completados = Math.max(0, maxDesbloqueado - 1);
        String txtProgreso = "COMPLETADOS: " + completados + " / " + CANTIDAD_NIVELES;
        fontPequena.setColor(COLOR_AMBAR);
        fontPequena.draw(batch, txtProgreso, 70f, ALTO_VIRTUAL - 80f);
    }

    private void dibujarTextosTarjetas() {
        for (int i = 0; i < CANTIDAD_NIVELES; i++) {
            float x = CARDS_INICIO_X + i * (CARD_ANCHO + CARD_ESPACIADO);
            float y = CARDS_BASE_Y + (animHoverCards[i] * 14f);
            InfoNivel info = datosNiveles[i];
            boolean desb = estaDesbloqueado(i);
            boolean comp = estaCompletado(i);
            boolean sel = (nivelSeleccionado == i);

            fontPequena.setColor(desb ? COLOR_NEON_CYAN : COLOR_TEXTO_MUTED);
            fontPequena.draw(batch, "NIVEL 0" + info.numero, x + 18f, y + CARD_ALTO - 20f);

            String badgeTexto = comp ? "LISTO" : (desb ? "ACTIVO" : "BLOQ");
            Color colBadge = comp ? COLOR_AMBAR : (desb ? COLOR_VERDE : COLOR_ROJO);
            layout.setText(fontPequena, badgeTexto);
            fontPequena.setColor(colBadge);
            fontPequena.draw(batch, badgeTexto, x + CARD_ANCHO - layout.width - 18f, y + CARD_ALTO - 20f);

            fontCardTitulo.setColor(desb ? Color.WHITE : COLOR_TEXTO_MUTED);
            layout.setText(fontCardTitulo, info.titulo);
            fontCardTitulo.draw(batch, info.titulo, x + 18f, y + CARD_ALTO - 46f);

            float infoX = x + 24f;
            float infoY = y + 148f;

            fontPequena.setColor(COLOR_AMBAR);
            fontPequena.draw(batch, "DIFICULTAD: " + info.categoria + "  (" + info.tiempo + ")", infoX, infoY);

            fontCardDetalle.setColor(desb ? COLOR_TEXTO_MUTED : new Color(0.38f, 0.42f, 0.50f, 1f));
            fontCardDetalle.draw(batch, info.descripcion, infoX, infoY - 20f, CARD_ANCHO - 48f, Align.left, true);

            String txtBoton = desb ? (sel ? "SELECCIONADO" : "ELEGIR") : "BLOQUEADO";
            Color colTxtBoton = desb ? (sel ? Color.BLACK : COLOR_CELESTE_BRILLANTE) : COLOR_TEXTO_MUTED;
            layout.setText(fontPequena, txtBoton);
            fontPequena.setColor(colTxtBoton);
            fontPequena.draw(batch, txtBoton, x + (CARD_ANCHO - layout.width) / 2f, y + 40f);
        }
    }

    private void dibujarTextosBotones() {
        layout.setText(fontBotones, "VOLVER");
        fontBotones.setColor((animVolver > 0.05f) ? COLOR_NEON_CYAN : COLOR_TEXTO_BLANCO);
        fontBotones.draw(batch, "VOLVER", 70f + (280f - layout.width) / 2f, 60f + 42f);

        float iniciarW = 460f;
        float iniciarX = (ANCHO_VIRTUAL - iniciarW) / 2f;
        boolean desb = estaDesbloqueado(nivelSeleccionado);
        String txtIniciar = desb ? "INICIAR NIVEL " + (nivelSeleccionado + 1) : "NIVEL BLOQUEADO";
        layout.setText(fontBotones, txtIniciar);
        fontBotones.setColor(desb ? Color.WHITE : COLOR_TEXTO_MUTED);
        fontBotones.draw(batch, txtIniciar, iniciarX + (iniciarW - layout.width) / 2f, 60f + 47f);

        float modoW = 260f;
        float modoX = ANCHO_VIRTUAL - modoW - 70f;
        float modoY = ALTO_VIRTUAL - 80f;
        String txtModo = modoLibreDesbloqueado ? "MODO LIBRE: [ON]" : "MODO LIBRE: [OFF]";
        layout.setText(fontPequena, txtModo);
        fontPequena.setColor(modoLibreDesbloqueado ? COLOR_VERDE : COLOR_TEXTO_MUTED);
        fontPequena.draw(batch, txtModo, modoX + (modoW - layout.width) / 2f, modoY + 32f);

        fontPequena.setColor(new Color(0.40f, 0.50f, 0.65f, 0.8f));
        String ayuda = "A / D o Flechas Cambiar nivel   |   [Enter / Clic] Iniciar   |   [Clic en Electron] Interactuar   |   [U] Modo Libre";
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
        fontCardTitulo.dispose();
        fontCardDetalle.dispose();
        fontPequena.dispose();
        fontBotones.dispose();
    }
}
