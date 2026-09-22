package com.schematic.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.schematic.constants.Constantes;

import java.util.ArrayList;
import java.util.List;

public class VistaGuia extends ScreenAdapter {

    private Game juego;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private OrthographicCamera camara;
    private Viewport viewport;

    private BitmapFont fontTitulo;
    private BitmapFont fontSubtitulo;
    private BitmapFont fontCardTitulo;
    private BitmapFont fontTexto;
    private BitmapFont fontPequena;
    private BitmapFont fontBotones;

    private List<Texture> texturas;
    private Animation<TextureRegion> animAnd;
    private Animation<TextureRegion> animLedNormal;
    private Animation<TextureRegion> animLedPrendiendo;
    private Animation<TextureRegion> animLedMuriendo;
    private Animation<TextureRegion> animResistenciaNormal;
    private Animation<TextureRegion> animResistenciaFallo;

    private Color colorFondo = new Color(0.02f, 0.03f, 0.06f, 1f);
    private Color colorCaja = new Color(0.05f, 0.07f, 0.11f, 0.96f);
    private Color colorSlate = new Color(0.06f, 0.09f, 0.14f, 1f);
    private Color colorNeon = new Color(0.0f, 0.88f, 1.0f, 1f);
    private Color colorBrillante = new Color(0.60f, 0.95f, 1.0f, 1f);
    private Color colorMetal = new Color(0.30f, 0.38f, 0.48f, 1f);
    private Color colorClaro = new Color(0.65f, 0.76f, 0.88f, 1f);
    private Color colorAmbar = new Color(1.0f, 0.76f, 0.20f, 1f);
    private Color colorVerde = new Color(0.20f, 0.95f, 0.45f, 1f);
    private Color colorRojo = new Color(0.95f, 0.25f, 0.25f, 1f);

    private float tiempo = 0f;
    private float offsetGrid = 0f;

    private int pagina = 0;
    private float hoverVolver = 0f;
    private float hoverTab0 = 1f;
    private float hoverTab1 = 0f;
    private float hoverPrev = 0f;
    private float hoverNext = 0f;

    private ArrayList<ElementoGuia> listaPagina0;
    private ArrayList<ElementoGuia> listaPagina1;

    public VistaGuia() {
        this((Game) Gdx.app.getApplicationListener());
    }

    public VistaGuia(Game juego) {
        this.juego = juego;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.layout = new GlyphLayout();

        this.camara = new OrthographicCamera();
        this.viewport = new FitViewport(Constantes.ANCHO_VENTANA, Constantes.ALTO_VENTANA, camara);
        this.viewport.apply();
        this.camara.position.set(Constantes.ANCHO_VENTANA / 2f, Constantes.ALTO_VENTANA / 2f, 0);
        this.camara.update();

        this.fontTitulo = new BitmapFont();
        this.fontTitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontTitulo.getData().setScale(2.4f);

        this.fontSubtitulo = new BitmapFont();
        this.fontSubtitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontSubtitulo.getData().setScale(1.15f);

        this.fontCardTitulo = new BitmapFont();
        this.fontCardTitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontCardTitulo.getData().setScale(1.45f);

        this.fontTexto = new BitmapFont();
        this.fontTexto.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontTexto.getData().setScale(0.98f);

        this.fontPequena = new BitmapFont();
        this.fontPequena.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontPequena.getData().setScale(0.85f);

        this.fontBotones = new BitmapFont();
        this.fontBotones.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontBotones.getData().setScale(1.15f);

        this.texturas = new ArrayList<>();
        cargarAnimaciones();

        this.listaPagina0 = new ArrayList<>();
        this.listaPagina1 = new ArrayList<>();
        cargarElementos();
    }

    private void cargarAnimaciones() {
        animAnd = cargarAnimacion("and", 12, 0.12f);
        animLedNormal = cargarAnimacion("led_normal", 10, 0.12f);
        animLedPrendiendo = cargarAnimacion("led_prendiendo", 8, 0.10f);
        animLedMuriendo = cargarAnimacion("led_muriendo", 10, 0.14f);
        animResistenciaNormal = cargarAnimacion("resistencia_normal", 8, 0.22f);
        animResistenciaFallo = cargarAnimacion("resistencia_fail", 15, 0.15f);
    }

    private Animation<TextureRegion> cargarAnimacion(String nombreCarpeta, int framesTotal, float velocidad) {
        Array<TextureRegion> listaFrames = new Array<>();
        for (int i = 0; i < framesTotal; i++) {
            String ruta = String.format("sprites/%s/frame_%05d.png", nombreCarpeta, i);
            FileHandle archivo = Gdx.files.internal(ruta);
            if (archivo.exists()) {
                Texture tex = new Texture(archivo);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                texturas.add(tex);
                listaFrames.add(new TextureRegion(tex));
            }
        }
        if (listaFrames.size == 0) {
            Texture blanco = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            texturas.add(blanco);
            listaFrames.add(new TextureRegion(blanco));
        }
        return new Animation<>(velocidad, listaFrames);
    }

    private void cargarElementos() {
        listaPagina0.add(new ElementoGuia(
                "DIODO LED",
                "LUZ",
                "2V | 20mA",
                "Se enciende cuando la corriente lo atraviesa. Tiene patita positiva (ánodo) y negativa (cátodo).",
                "• Conecta el ánodo (+) a la corriente y el cátodo (-) a tierra.\n" +
                "• Ponle siempre una resistencia para no quemarlo.",
                new String[]{"APAGADO", "ENCENDIDO", "QUEMADO"},
                0
        ));

        listaPagina0.add(new ElementoGuia(
                "RESISTENCIA",
                "PROTECCIÓN",
                "220 Ω",
                "Frena el exceso de corriente para proteger los componentes y evitar que se quemen.",
                "• Conéctala en serie antes o después del LED.\n" +
                "• No tiene polaridad: funciona igual en cualquier sentido.",
                new String[]{"NORMAL", "QUEMADA"},
                1
        ));

        listaPagina0.add(new ElementoGuia(
                "FUENTE DE PODER",
                "ENERGÍA",
                "5V DC",
                "Suministra la energía para que todo el circuito cobre vida.",
                "• La energía sale por el (+) y debe regresar al (-).\n" +
                "• Nunca unas el (+) directo al (-) o harás un cortocircuito.",
                new String[]{"ON (5V)", "OFF (0V)"},
                2
        ));

        listaPagina0.add(new ElementoGuia(
                "INTERRUPTOR",
                "CONTROL",
                "ON / OFF",
                "Abre o cierra el paso de corriente a tu gusto.",
                "• Dale clic al centro para abrirlo o cerrarlo.\n" +
                "• En ON deja pasar la energía; en OFF corta el circuito.",
                new String[]{"CERRADO", "ABIERTO"},
                3
        ));

        listaPagina1.add(new ElementoGuia(
                "COMPUERTA AND",
                "LÓGICA",
                "Y = A · B",
                "Solo activa su salida si recibe corriente en sus dos entradas al mismo tiempo.",
                "• Activa la entrada A y la entrada B para encender la salida.\n" +
                "• Si falta alguna de las dos, la salida se queda en 0.",
                new String[]{"0 · 0 = 0", "1 · 0 = 0", "1 · 1 = 1"},
                4
        ));

        listaPagina1.add(new ElementoGuia(
                "COMPUERTA OR",
                "PRÓXIMAMENTE",
                "EN DESARROLLO",
                "Compuerta de suma lógica. Estará disponible en los próximos niveles.",
                "• Próximamente disponible en el simulador.",
                new String[]{},
                5
        ));

        listaPagina1.add(new ElementoGuia(
                "CABLES",
                "CONEXIÓN",
                "LÍNEA CONDUCTORA",
                "Unen los componentes y llevan la electricidad por donde decidas.",
                "• Clic en un pin y arrastra hasta otro para conectarlos.\n" +
                "• Clic derecho sobre un cable para desconectarlo.",
                new String[]{"ACTIVO", "REPOSO"},
                6
        ));

        listaPagina1.add(new ElementoGuia(
                "ELECTRONES",
                "ENERGÍA",
                "CARGA (-)",
                "Pequeños amigos que viajan por el cable transportando la energía.",
                "• Fluyen en cuanto el circuito está cerrado de punta a punta.\n" +
                "• Si abres el interruptor o cortas un cable, se detienen.",
                new String[]{"FLUYENDO", "PAUSADO"},
                7
        ));
    }

    private static class ElementoGuia {
        String nombre;
        String categoria;
        String especificacion;
        String descripcion;
        String queHacer;
        String[] nombresEstados;
        int tipoVisual;
        int estadoSeleccionado = 0;
        float animHover = 0f;

        ElementoGuia(String nombre, String categoria, String especificacion, String descripcion, String queHacer, String[] nombresEstados, int tipoVisual) {
            this.nombre = nombre;
            this.categoria = categoria;
            this.especificacion = especificacion;
            this.descripcion = descripcion;
            this.queHacer = queHacer;
            this.nombresEstados = nombresEstados;
            this.tipoVisual = tipoVisual;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) return false;

                Vector3 clickMundo = viewport.unproject(new Vector3(screenX, screenY, 0));
                float mx = clickMundo.x;
                float my = clickMundo.y;

                if (estaAdentro(mx, my, 60f, 20f, 270f, 50f)) {
                    volverAlMenu();
                    return true;
                }

                if (estaAdentro(mx, my, 400f, 960f, 520f, 44f)) {
                    pagina = 0;
                    return true;
                }
                if (estaAdentro(mx, my, 950f, 960f, 520f, 44f)) {
                    pagina = 1;
                    return true;
                }

                if (estaAdentro(mx, my, 340f, 960f, 48f, 44f)) {
                    pagina = (pagina - 1 + 2) % 2;
                    return true;
                }
                if (estaAdentro(mx, my, 1485f, 960f, 48f, 44f)) {
                    pagina = (pagina + 1) % 2;
                    return true;
                }

                ArrayList<ElementoGuia> lista = (pagina == 0) ? listaPagina0 : listaPagina1;
                float[][] coords = getCoordenadasTarjetas();

                for (int i = 0; i < lista.size(); i++) {
                    ElementoGuia item = lista.get(i);
                    float cx = coords[i][0];
                    float cy = coords[i][1];

                    if (item.nombresEstados.length > 0) {
                        float btnY = cy + 22f;
                        float cant = item.nombresEstados.length;
                        float btnW = (220f - (cant - 1) * 6f) / cant;

                        for (int b = 0; b < cant; b++) {
                            float bx = cx + 22f + b * (btnW + 6f);
                            if (estaAdentro(mx, my, bx, btnY, btnW, 36f)) {
                                item.estadoSeleccionado = b;
                                return true;
                            }
                        }

                        if (estaAdentro(mx, my, cx + 22f, cy + 68f, 220f, 230f)) {
                            item.estadoSeleccionado = (item.estadoSeleccionado + 1) % item.nombresEstados.length;
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                    volverAlMenu();
                    return true;
                }
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
                    pagina = (pagina - 1 + 2) % 2;
                    return true;
                }
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D || keycode == Input.Keys.TAB) {
                    pagina = (pagina + 1) % 2;
                    return true;
                }
                if (keycode == Input.Keys.NUM_1) {
                    pagina = 0;
                    return true;
                }
                if (keycode == Input.Keys.NUM_2) {
                    pagina = 1;
                    return true;
                }
                return false;
            }
        });
    }

    private void volverAlMenu() {
        if (juego != null) {
            juego.setScreen(new VistaMenu(juego));
        }
    }

    private boolean estaAdentro(float x, float y, float bx, float by, float bw, float bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    private float[][] getCoordenadasTarjetas() {
        return new float[][]{
                {60f, 500f},
                {980f, 500f},
                {60f, 85f},
                {980f, 85f}
        };
    }

    @Override
    public void render(float delta) {
        tiempo += delta;
        offsetGrid = (offsetGrid + delta * 95f) % 70f;

        viewport.apply();
        shapeRenderer.setProjectionMatrix(camara.combined);
        batch.setProjectionMatrix(camara.combined);

        Vector3 posRaton = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float mx = posRaton.x;
        float my = posRaton.y;

        actualizarAnimaciones(delta, mx, my);

        Gdx.gl.glClearColor(colorFondo.r, colorFondo.g, colorFondo.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarFondo();
        dibujarOsciloscopio();
        dibujarRejillaInferior();
        dibujarTabsShapes();
        dibujarTarjetasShapes(mx, my);
        dibujarBotonVolverShape();
        shapeRenderer.end();

        batch.begin();
        dibujarCabecera();
        dibujarContenido(mx, my);
        dibujarBotonVolverTexto();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void actualizarAnimaciones(float delta, float mx, float my) {
        hoverVolver = MathUtils.lerp(hoverVolver, estaAdentro(mx, my, 60f, 20f, 270f, 50f) ? 1f : 0f, delta * 18f);

        boolean hTab0 = estaAdentro(mx, my, 400f, 960f, 520f, 44f);
        boolean hTab1 = estaAdentro(mx, my, 950f, 960f, 520f, 44f);
        hoverTab0 = MathUtils.lerp(hoverTab0, (pagina == 0 || hTab0) ? 1f : 0f, delta * 18f);
        hoverTab1 = MathUtils.lerp(hoverTab1, (pagina == 1 || hTab1) ? 1f : 0f, delta * 18f);

        hoverPrev = MathUtils.lerp(hoverPrev, estaAdentro(mx, my, 340f, 960f, 48f, 44f) ? 1f : 0f, delta * 18f);
        hoverNext = MathUtils.lerp(hoverNext, estaAdentro(mx, my, 1485f, 960f, 48f, 44f) ? 1f : 0f, delta * 18f);

        ArrayList<ElementoGuia> lista = (pagina == 0) ? listaPagina0 : listaPagina1;
        float[][] coords = getCoordenadasTarjetas();
        for (int i = 0; i < lista.size(); i++) {
            boolean hCard = estaAdentro(mx, my, coords[i][0], coords[i][1], 880f, 390f);
            lista.get(i).animHover = MathUtils.lerp(lista.get(i).animHover, hCard ? 1f : 0f, delta * 16f);
        }
    }

    private void dibujarFondo() {
        for (float y = 0; y < Constantes.ALTO_VENTANA; y += 35f) {
            float t = y / Constantes.ALTO_VENTANA;
            shapeRenderer.setColor(MathUtils.lerp(0.015f, 0.045f, t), MathUtils.lerp(0.025f, 0.065f, t), MathUtils.lerp(0.05f, 0.12f, t), 1f);
            shapeRenderer.rect(0, y, Constantes.ANCHO_VENTANA, 36f);
        }

        shapeRenderer.setColor(colorMetal.r, colorMetal.g, colorMetal.b, 0.15f);
        float[] lineasY = {80f, 220f, 480f, 740f, 940f, 1030f};
        for (int i = 0; i < lineasY.length; i++) {
            float py = lineasY[i];
            shapeRenderer.rectLine(0, py, Constantes.ANCHO_VENTANA, py, 2.0f);

            float velX = 220f + (i * 60f);
            float pulsoX = ((tiempo * velX) + i * 340f) % (Constantes.ANCHO_VENTANA + 300f) - 150f;
            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.82f);
            shapeRenderer.rectLine(pulsoX - 45f, py, pulsoX + 45f, py, 3.8f);

            shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
            shapeRenderer.circle(pulsoX, py, 4.0f);
            shapeRenderer.setColor(colorMetal.r, colorMetal.g, colorMetal.b, 0.15f);
        }
    }

    private void dibujarOsciloscopio() {
        float centroY = 1010f;
        float prevX = 0f;
        float prevY = centroY + MathUtils.sin(tiempo * 6f) * 16f;

        for (float x = 18f; x <= Constantes.ANCHO_VENTANA; x += 18f) {
            float onda = MathUtils.sin(x * 0.015f + tiempo * 7.0f) * 18f + MathUtils.cos(x * 0.032f - tiempo * 4.5f) * 10f;
            float y = centroY + onda;
            float alpha = 0.22f + MathUtils.sin(tiempo * 3.5f + x * 0.01f) * 0.12f;

            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, alpha);
            shapeRenderer.rectLine(prevX, prevY, x, y, 2.4f);
            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.75f);
            shapeRenderer.rectLine(prevX, prevY, x, y, 1.0f);

            prevX = x;
            prevY = y;
        }
    }

    private void dibujarRejillaInferior() {
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.18f);

        for (float x = 0; x <= Constantes.ANCHO_VENTANA; x += 110f) {
            float ondaX = MathUtils.sin(tiempo * 3.2f + x * 0.012f) * 12f;
            shapeRenderer.rectLine(x + ondaX, 0, x - (x - Constantes.ANCHO_VENTANA / 2f) * 0.40f, 200f, 1.8f);
        }

        for (float y = 0; y < 200f; y += 22f) {
            float animY = (y + offsetGrid) % 200f;
            float alpha = (1f - (animY / 200f)) * 0.35f;
            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, alpha);
            shapeRenderer.rectLine(0, animY, Constantes.ANCHO_VENTANA, animY, 1.8f);
        }
    }

    private void dibujarTabsShapes() {
        shapeRenderer.setColor(colorCaja);
        shapeRenderer.rect(400f, 960f, 520f, 44f);
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.3f + hoverTab0 * 0.65f);
        shapeRenderer.rectLine(400f, 960f, 920f, 960f, 3.2f);
        shapeRenderer.rectLine(400f, 1004f, 920f, 1004f, 3.2f);
        shapeRenderer.rectLine(400f, 960f, 400f, 1004f, 3.2f);
        shapeRenderer.rectLine(920f, 960f, 920f, 1004f, 3.2f);
        if (pagina == 0) {
            shapeRenderer.setColor(colorBrillante);
            shapeRenderer.rect(400f, 960f, 520f, 4f);
        }

        shapeRenderer.setColor(colorCaja);
        shapeRenderer.rect(950f, 960f, 520f, 44f);
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.3f + hoverTab1 * 0.65f);
        shapeRenderer.rectLine(950f, 960f, 1470f, 960f, 3.2f);
        shapeRenderer.rectLine(950f, 1004f, 1470f, 1004f, 3.2f);
        shapeRenderer.rectLine(950f, 960f, 950f, 1004f, 3.2f);
        shapeRenderer.rectLine(1470f, 960f, 1470f, 1004f, 3.2f);
        if (pagina == 1) {
            shapeRenderer.setColor(colorBrillante);
            shapeRenderer.rect(950f, 960f, 520f, 4f);
        }

        shapeRenderer.setColor(colorCaja);
        shapeRenderer.rect(340f, 960f, 48f, 44f);
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.4f + hoverPrev * 0.6f);
        shapeRenderer.rectLine(340f, 960f, 388f, 960f, 2.5f);
        shapeRenderer.rectLine(388f, 960f, 388f, 1004f, 2.5f);
        shapeRenderer.rectLine(388f, 1004f, 340f, 1004f, 2.5f);
        shapeRenderer.rectLine(340f, 1004f, 340f, 960f, 2.5f);

        shapeRenderer.setColor(colorCaja);
        shapeRenderer.rect(1485f, 960f, 48f, 44f);
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.4f + hoverNext * 0.6f);
        shapeRenderer.rectLine(1485f, 960f, 1533f, 960f, 2.5f);
        shapeRenderer.rectLine(1533f, 960f, 1533f, 1004f, 2.5f);
        shapeRenderer.rectLine(1533f, 1004f, 1485f, 1004f, 2.5f);
        shapeRenderer.rectLine(1485f, 1004f, 1485f, 960f, 2.5f);
    }

    private void dibujarCabecera() {
        String titulo = "GUÍA DE COMPONENTES";
        layout.setText(fontTitulo, titulo);
        float tx = (Constantes.ANCHO_VENTANA - layout.width) / 2f;

        fontTitulo.setColor(0f, 0f, 0f, 0.9f);
        fontTitulo.draw(batch, titulo, tx + 3f, 1052f);
        fontTitulo.setColor(colorBrillante);
        fontTitulo.draw(batch, titulo, tx, 1055f);

        String sub = "Conoce las piezas del circuito y aprende cómo utilizarlas";
        layout.setText(fontSubtitulo, sub);
        fontSubtitulo.setColor(colorClaro);
        fontSubtitulo.draw(batch, sub, (Constantes.ANCHO_VENTANA - layout.width) / 2f, 1022f);

        String t0 = "1. COMPONENTES BÁSICOS";
        layout.setText(fontBotones, t0);
        fontBotones.setColor(pagina == 0 ? colorBrillante : colorMetal);
        fontBotones.draw(batch, t0, 400f + (520f - layout.width) / 2f, 988f);

        String t1 = "2. LÓGICA Y CONEXIONES";
        layout.setText(fontBotones, t1);
        fontBotones.setColor(pagina == 1 ? colorBrillante : colorMetal);
        fontBotones.draw(batch, t1, 950f + (520f - layout.width) / 2f, 988f);

        fontBotones.setColor(hoverPrev > 0.5f ? Color.WHITE : colorNeon);
        fontBotones.draw(batch, "<", 356f, 988f);

        fontBotones.setColor(hoverNext > 0.5f ? Color.WHITE : colorNeon);
        fontBotones.draw(batch, ">", 1502f, 988f);
    }

    private void dibujarTarjetasShapes(float mx, float my) {
        ArrayList<ElementoGuia> lista = (pagina == 0) ? listaPagina0 : listaPagina1;
        float[][] coords = getCoordenadasTarjetas();

        for (int i = 0; i < lista.size(); i++) {
            ElementoGuia item = lista.get(i);
            float x = coords[i][0];
            float y = coords[i][1];
            float w = 880f;
            float h = 390f;
            float hov = item.animHover;

            float sombra = 10f + hov * 6f;
            shapeRenderer.setColor(0f, 0f, 0f, 0.88f);
            shapeRenderer.rect(x + sombra, y - sombra, w, h);

            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.18f + hov * 0.28f);
            shapeRenderer.rect(x + sombra * 0.5f, y - sombra * 0.5f, w, h);

            shapeRenderer.setColor(MathUtils.lerp(colorCaja.r, colorSlate.r, hov * 0.4f), MathUtils.lerp(colorCaja.g, 0.11f, hov * 0.4f), MathUtils.lerp(colorCaja.b, 0.18f, hov * 0.4f), 0.98f);
            shapeRenderer.rect(x, y, w, h);

            for (float lx = x + 20f; lx < x + w; lx += 45f) {
                shapeRenderer.setColor(colorSlate.r, colorSlate.g, colorSlate.b, 0.22f);
                shapeRenderer.rectLine(lx, y, lx + 20f, y + h, 1.5f);
            }

            Color colB = (hov > 0.5f) ? colorBrillante : colorNeon;
            float grosor = 3.0f + hov * 1.5f;
            shapeRenderer.setColor(colB);
            shapeRenderer.rectLine(x, y, x + w, y, grosor);
            shapeRenderer.rectLine(x + w, y, x + w, y + h, grosor);
            shapeRenderer.rectLine(x + w, y + h, x, y + h, grosor);
            shapeRenderer.rectLine(x, y + h, x, y, grosor);

            shapeRenderer.setColor(colorBrillante);
            float brk = 22f + hov * 6f;
            shapeRenderer.rectLine(x, y, x + brk, y, grosor + 2f);
            shapeRenderer.rectLine(x, y, x, y + brk, grosor + 2f);
            shapeRenderer.rectLine(x + w, y, x + w - brk, y, grosor + 2f);
            shapeRenderer.rectLine(x + w, y, x + w, y + brk, grosor + 2f);
            shapeRenderer.rectLine(x, y + h, x + brk, y + h, grosor + 2f);
            shapeRenderer.rectLine(x, y + h, x, y + h - brk, grosor + 2f);
            shapeRenderer.rectLine(x + w, y + h, x + w - brk, y + h, grosor + 2f);
            shapeRenderer.rectLine(x + w, y + h, x + w, y + h - brk, grosor + 2f);

            if (hov > 0.05f) {
                float perim = 2 * (w + h);
                float dist = ((tiempo * 440f) + i * 140f) % perim;
                float pxL, pyL;
                if (dist < w) {
                    pxL = x + dist; pyL = y;
                } else if (dist < w + h) {
                    pxL = x + w; pyL = y + (dist - w);
                } else if (dist < 2 * w + h) {
                    pxL = x + w - (dist - (w + h)); pyL = y + h;
                } else {
                    pxL = x; pyL = y + h - (dist - (2 * w + h));
                }
                shapeRenderer.setColor(1f, 1f, 1f, 0.95f * hov);
                shapeRenderer.circle(pxL, pyL, 3.8f);
                shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.75f * hov);
                shapeRenderer.circle(pxL, pyL, 7.0f);
            }

            float vx = x + 22f;
            float vy = y + 68f;
            float vw = 220f;
            float vh = 230f;

            shapeRenderer.setColor(0.015f, 0.025f, 0.045f, 0.98f);
            shapeRenderer.rect(vx, vy, vw, vh);

            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.12f);
            for (float gy = vy + 25f; gy < vy + vh; gy += 25f) {
                shapeRenderer.rectLine(vx, gy, vx + vw, gy, 1.0f);
            }
            for (float gx = vx + 25f; gx < vx + vw; gx += 25f) {
                shapeRenderer.rectLine(gx, vy, gx, vy + vh, 1.0f);
            }

            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.55f);
            shapeRenderer.rectLine(vx, vy, vx + vw, vy, 2.0f);
            shapeRenderer.rectLine(vx + vw, vy, vx + vw, vy + vh, 2.0f);
            shapeRenderer.rectLine(vx + vw, vy + vh, vx, vy + vh, 2.0f);
            shapeRenderer.rectLine(vx, vy + vh, vx, vy, 2.0f);

            if (item.nombresEstados.length > 0) {
                float btnY = y + 22f;
                float cant = item.nombresEstados.length;
                float btnW = (vw - (cant - 1) * 6f) / cant;

                for (int b = 0; b < cant; b++) {
                    float bx = vx + b * (btnW + 6f);
                    boolean sel = (item.estadoSeleccionado == b);
                    boolean hBtn = estaAdentro(mx, my, bx, btnY, btnW, 36f);

                    shapeRenderer.setColor(sel ? colorSlate : colorCaja);
                    shapeRenderer.rect(bx, btnY, btnW, 36f);

                    Color cB = sel ? colorBrillante : (hBtn ? colorNeon : colorMetal);
                    shapeRenderer.setColor(cB);
                    shapeRenderer.rectLine(bx, btnY, bx + btnW, btnY, sel ? 2.8f : 1.5f);
                    shapeRenderer.rectLine(bx + btnW, btnY, bx + btnW, btnY + 36f, sel ? 2.8f : 1.5f);
                    shapeRenderer.rectLine(bx + btnW, btnY + 36f, bx, btnY + 36f, sel ? 2.8f : 1.5f);
                    shapeRenderer.rectLine(bx, btnY + 36f, bx, btnY, sel ? 2.8f : 1.5f);
                }
            }

            float qx = x + 265f;
            float qy = y + 20f;
            float qw = w - 285f;
            float qh = 168f;

            shapeRenderer.setColor(0.035f, 0.055f, 0.085f, 0.95f);
            shapeRenderer.rect(qx, qy, qw, qh);

            shapeRenderer.setColor(colorAmbar.r, colorAmbar.g, colorAmbar.b, 0.65f);
            shapeRenderer.rectLine(qx, qy, qx + qw, qy, 2.2f);
            shapeRenderer.rectLine(qx + qw, qy, qx + qw, qy + qh, 2.2f);
            shapeRenderer.rectLine(qx + qw, qy + qh, qx, qy + qh, 2.2f);
            shapeRenderer.rectLine(qx, qy + qh, qx, qy, 2.2f);

            shapeRenderer.setColor(colorAmbar.r, colorAmbar.g, colorAmbar.b, 0.18f);
            shapeRenderer.rect(qx, qy + qh - 30f, qw, 30f);

            dibujarProceduralShapes(item, vx, vy, vw, vh);
        }
    }

    private void dibujarProceduralShapes(ElementoGuia item, float vx, float vy, float vw, float vh) {
        float cx = vx + vw / 2f;
        float cy = vy + vh / 2f;

        if (item.tipoVisual == 2) {
            float fx = cx - 75f;
            float fy = cy - 70f;

            shapeRenderer.setColor(0.12f, 0.16f, 0.22f, 1f);
            shapeRenderer.rect(fx, fy, 150f, 140f);
            shapeRenderer.setColor(colorClaro);
            shapeRenderer.rectLine(fx, fy, fx + 150f, fy, 2f);
            shapeRenderer.rectLine(fx + 150f, fy, fx + 150f, fy + 140f, 2f);
            shapeRenderer.rectLine(fx + 150f, fy + 140f, fx, fy + 140f, 2f);
            shapeRenderer.rectLine(fx, fy + 140f, fx, fy, 2f);

            shapeRenderer.setColor(0.03f, 0.05f, 0.08f, 1f);
            shapeRenderer.rect(fx + 18f, fy + 140f - 52f, 114f, 38f);

            shapeRenderer.setColor(0.95f, 0.20f, 0.20f, 1f);
            shapeRenderer.circle(fx + 38f, fy + 36f, 14f);
            shapeRenderer.setColor(0.20f, 0.55f, 0.95f, 1f);
            shapeRenderer.circle(fx + 112f, fy + 36f, 14f);

            shapeRenderer.setColor(item.estadoSeleccionado == 0 ? colorVerde : colorRojo);
            shapeRenderer.circle(cx, fy + 36f, 7f);

        } else if (item.tipoVisual == 3) {
            float sx = cx - 70f;
            float sy = cy - 45f;

            shapeRenderer.setColor(0.08f, 0.12f, 0.18f, 1f);
            shapeRenderer.rect(sx, sy, 140f, 90f);
            shapeRenderer.setColor(colorMetal);
            shapeRenderer.rectLine(sx, sy, sx + 140f, sy, 2f);
            shapeRenderer.rectLine(sx + 140f, sy, sx + 140f, sy + 90f, 2f);
            shapeRenderer.rectLine(sx + 140f, sy + 90f, sx, sy + 90f, 2f);
            shapeRenderer.rectLine(sx, sy + 90f, sx, sy, 2f);

            shapeRenderer.setColor(0.7f, 0.75f, 0.85f, 1f);
            shapeRenderer.rect(sx - 16f, cy - 6f, 16f, 12f);
            shapeRenderer.rect(sx + 140f, cy - 6f, 16f, 12f);

            boolean on = (item.estadoSeleccionado == 0);
            shapeRenderer.setColor(on ? colorVerde : colorRojo);
            if (on) {
                shapeRenderer.rectLine(sx + 25f, cy, sx + 115f, cy, 6f);
            } else {
                shapeRenderer.rectLine(sx + 25f, cy, sx + 100f, cy + 30f, 6f);
            }
            shapeRenderer.circle(sx + 25f, cy, 7f);
            shapeRenderer.circle(sx + 115f, cy, 7f);

        } else if (item.tipoVisual == 6) {
            boolean run = (item.estadoSeleccionado == 0);
            float p1x = vx + 35f; float p1y = vy + 45f;
            float p2x = vx + 35f; float p2y = vy + vh - 55f;
            float p3x = vx + vw - 35f; float p3y = vy + vh - 55f;
            float p4x = vx + vw - 35f; float p4y = vy + 45f;

            shapeRenderer.setColor(colorFondo);
            shapeRenderer.rectLine(p1x, p1y, p2x, p2y, 8f);
            shapeRenderer.rectLine(p2x, p2y, p3x, p3y, 8f);
            shapeRenderer.rectLine(p3x, p3y, p4x, p4y, 8f);

            shapeRenderer.setColor(run ? colorNeon : colorMetal);
            shapeRenderer.rectLine(p1x, p1y, p2x, p2y, 4f);
            shapeRenderer.rectLine(p2x, p2y, p3x, p3y, 4f);
            shapeRenderer.rectLine(p3x, p3y, p4x, p4y, 4f);

            shapeRenderer.setColor(0.95f, 0.20f, 0.20f, 1f);
            shapeRenderer.circle(p1x, p1y, 11f);
            shapeRenderer.setColor(0.20f, 0.55f, 0.95f, 1f);
            shapeRenderer.circle(p4x, p4y, 11f);

            if (run) {
                for (int e = 0; e < 5; e++) {
                    float d = ((tiempo * 130f) + e * 70f) % (vw + vh);
                    float ex, ey;
                    if (d < (vh - 100f)) {
                        ex = p1x; ey = p1y + d;
                    } else if (d < (vh - 100f + vw - 70f)) {
                        ex = p2x + (d - (vh - 100f)); ey = p2y;
                    } else {
                        ex = p3x; ey = p3y - (d - (vh - 100f + vw - 70f));
                    }
                    shapeRenderer.setColor(1f, 1f, 1f, 0.95f);
                    shapeRenderer.circle(ex, ey, 4.5f);
                    shapeRenderer.setColor(colorBrillante.r, colorBrillante.g, colorBrillante.b, 0.65f);
                    shapeRenderer.circle(ex, ey, 8.5f);
                }
            }

        } else if (item.tipoVisual == 7) {
            float vel = (item.estadoSeleccionado == 0) ? 7f : 1.5f;
            float pulso = 1f + MathUtils.sin(tiempo * vel) * 0.12f;

            shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.28f);
            shapeRenderer.circle(cx, cy, 45f * pulso);

            shapeRenderer.setColor(0.10f, 0.65f, 1.0f, 0.85f);
            shapeRenderer.circle(cx, cy, 26f * pulso);
            shapeRenderer.setColor(colorBrillante);
            shapeRenderer.circle(cx, cy, 18f * pulso);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(cx - 6f, cy + 6f, 7f * pulso);

            shapeRenderer.setColor(0.02f, 0.08f, 0.18f, 1f);
            shapeRenderer.rect(cx - 10f, cy - 3f, 20f, 6f);
        }
    }

    private void dibujarContenido(float mx, float my) {
        ArrayList<ElementoGuia> lista = (pagina == 0) ? listaPagina0 : listaPagina1;
        float[][] coords = getCoordenadasTarjetas();

        for (int i = 0; i < lista.size(); i++) {
            ElementoGuia item = lista.get(i);
            float x = coords[i][0];
            float y = coords[i][1];
            float w = 880f;
            float h = 390f;

            fontCardTitulo.setColor(Color.WHITE);
            fontCardTitulo.draw(batch, item.nombre, x + 24f, y + h - 18f);

            layout.setText(fontPequena, item.categoria);
            fontPequena.setColor(colorAmbar);
            fontPequena.draw(batch, item.categoria, x + w - layout.width - 24f, y + h - 18f);

            fontPequena.setColor(colorBrillante);
            fontPequena.draw(batch, item.especificacion, x + 265f, y + h - 48f);

            float vx = x + 22f;
            float vy = y + 68f;
            float vw = 220f;
            float vh = 230f;

            if (item.tipoVisual == 0) {
                TextureRegion frameLed;
                if (item.estadoSeleccionado == 2) {
                    frameLed = animLedMuriendo.getKeyFrame(tiempo, true);
                } else if (item.estadoSeleccionado == 1) {
                    frameLed = animLedPrendiendo.getKeyFrame(tiempo, true);
                } else {
                    frameLed = animLedNormal.getKeyFrame(tiempo, true);
                }
                batch.draw(frameLed, vx + 20f, vy + 25f, 180f, 180f);

            } else if (item.tipoVisual == 1) {
                TextureRegion frameRes;
                if (item.estadoSeleccionado == 1) {
                    frameRes = animResistenciaFallo.getKeyFrame(tiempo, true);
                } else {
                    frameRes = animResistenciaNormal.getKeyFrame(tiempo, true);
                }
                batch.draw(frameRes, vx + 10f, vy + 50f, 200f, 130f);

            } else if (item.tipoVisual == 4) {
                TextureRegion frameAnd = animAnd.getKeyFrame(tiempo, true);
                batch.draw(frameAnd, vx + 15f, vy + 20f, 190f, 190f);

            } else if (item.tipoVisual == 2) {
                boolean on = (item.estadoSeleccionado == 0);
                fontBotones.setColor(on ? colorNeon : colorRojo);
                String txtV = on ? "5.00 V" : "0.00 V";
                layout.setText(fontBotones, txtV);
                fontBotones.draw(batch, txtV, vx + (vw - layout.width) / 2f, vy + vh - 26f);
            }

            if (item.nombresEstados.length > 0) {
                float btnY = y + 22f;
                float cant = item.nombresEstados.length;
                float btnW = (vw - (cant - 1) * 6f) / cant;

                for (int b = 0; b < cant; b++) {
                    float bx = vx + b * (btnW + 6f);
                    boolean sel = (item.estadoSeleccionado == b);
                    String nom = item.nombresEstados[b];

                    layout.setText(fontPequena, nom);
                    fontPequena.setColor(sel ? Color.WHITE : colorClaro);
                    fontPequena.draw(batch, nom, bx + (btnW - layout.width) / 2f, btnY + 23f);
                }
            }

            float descX = x + 265f;
            float descY = y + h - 72f;
            float descW = w - 290f;

            fontPequena.setColor(colorNeon);
            fontPequena.draw(batch, "¿QUÉ HACE?", descX, descY);

            fontTexto.setColor(Color.WHITE);
            fontTexto.draw(batch, item.descripcion, descX, descY - 18f, descW, Align.left, true);

            float qx = x + 265f;
            float qy = y + 20f;
            float qw = w - 285f;
            float qh = 168f;

            fontPequena.setColor(colorAmbar);
            fontPequena.draw(batch, "¿CÓMO CONECTARLO?", qx + 14f, qy + qh - 10f);

            fontPequena.setColor(colorClaro);
            fontPequena.draw(batch, item.queHacer, qx + 14f, qy + qh - 38f, qw - 28f, Align.left, true);
        }
    }

    private void dibujarBotonVolverShape() {
        float bx = 60f;
        float by = 20f;
        float bw = 270f;
        float bh = 50f;

        float sombra = 8f + hoverVolver * 6f;
        shapeRenderer.setColor(0f, 0f, 0f, 0.85f);
        shapeRenderer.rect(bx + sombra, by - sombra, bw, bh);

        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.25f + hoverVolver * 0.45f);
        shapeRenderer.rect(bx + sombra * 0.5f, by - sombra * 0.5f, bw, bh);

        shapeRenderer.setColor(MathUtils.lerp(colorCaja.r, colorSlate.r, hoverVolver), MathUtils.lerp(colorCaja.g, 0.16f, hoverVolver), MathUtils.lerp(colorCaja.b, 0.26f, hoverVolver), 0.98f);
        shapeRenderer.rect(bx, by, bw, bh);

        Color colB = (hoverVolver > 0.5f) ? colorBrillante : colorNeon;
        shapeRenderer.setColor(colB);
        float grosor = 3.0f + hoverVolver * 1.5f;
        shapeRenderer.rectLine(bx, by, bx + bw, by, grosor);
        shapeRenderer.rectLine(bx + bw, by, bx + bw, by + bh, grosor);
        shapeRenderer.rectLine(bx + bw, by + bh, bx, by + bh, grosor);
        shapeRenderer.rectLine(bx, by + bh, bx, by, grosor);

        if (hoverVolver > 0.05f) {
            float ptr = 12f * hoverVolver;
            shapeRenderer.setColor(colorNeon);
            shapeRenderer.triangle(bx + 18f, by + bh / 2f, bx + 6f, by + bh / 2f + ptr, bx + 6f, by + bh / 2f - ptr);
        }
    }

    private void dibujarBotonVolverTexto() {
        float bx = 60f;
        float by = 20f;
        float bw = 270f;
        float bh = 50f;

        String txt = "VOLVER AL MENÚ";
        layout.setText(fontBotones, txt);
        float tx = bx + (bw - layout.width) / 2f;
        float ty = by + (bh + layout.height) / 2f;

        fontBotones.setColor(0f, 0f, 0f, 0.9f);
        fontBotones.draw(batch, txt, tx + 2f, ty - 2f);

        fontBotones.setColor(hoverVolver > 0.5f ? Color.WHITE : colorBrillante);
        fontBotones.draw(batch, txt, tx, ty);

        String atajos = "ESC: Volver   |   Flechas: Cambiar página";
        layout.setText(fontPequena, atajos);
        fontPequena.setColor(colorMetal);
        fontPequena.draw(batch, atajos, (Constantes.ANCHO_VENTANA - layout.width) / 2f + 120f, 48f);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontSubtitulo != null) fontSubtitulo.dispose();
        if (fontCardTitulo != null) fontCardTitulo.dispose();
        if (fontTexto != null) fontTexto.dispose();
        if (fontPequena != null) fontPequena.dispose();
        if (fontBotones != null) fontBotones.dispose();

        for (Texture tex : texturas) {
            if (tex != null) tex.dispose();
        }
        texturas.clear();
    }
}
