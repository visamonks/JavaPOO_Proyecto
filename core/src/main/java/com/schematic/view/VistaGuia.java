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
import com.schematic.audio.GestorAudio;
import com.schematic.audio.TipoEfecto;
import com.schematic.constants.Constantes;

import java.util.ArrayList;
import java.util.List;

public class VistaGuia extends ScreenAdapter {

    private Game juego;
    private GestorAudio gestorAudio;
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
    private Animation<TextureRegion> animOr;
    private Animation<TextureRegion> animLedNormal;
    private Animation<TextureRegion> animLedPrendiendo;
    private Animation<TextureRegion> animLedMuriendo;
    private Animation<TextureRegion> animResistenciaNormal;
    private Animation<TextureRegion> animResistenciaFallo;
    private Animation<TextureRegion> animCapacitorVacio;
    private Animation<TextureRegion> animCapacitorCargado;
    private Animation<TextureRegion> animCapacitorExplotado;
    private Animation<TextureRegion> anim555Astable;
    private Animation<TextureRegion> anim555Monoestable;
    private Animation<TextureRegion> animPotenciometro;

    private Color colorFondo = new Color(0.02f, 0.03f, 0.06f, 1f);
    private Color colorCaja = new Color(0.05f, 0.07f, 0.11f, 0.96f);
    private Color colorSlate = new Color(0.06f, 0.09f, 0.14f, 1f);
    private Color colorNeon = new Color(0.0f, 0.88f, 1.0f, 1f);
    private Color colorBrillante = new Color(0.60f, 0.95f, 1.0f, 1f);
    private Color colorMetal = new Color(0.30f, 0.38f, 0.48f, 1f);
    private Color colorClaro = new Color(0.65f, 0.76f, 0.88f, 1f);
    private final BarraAtajos atajos = new BarraAtajos(colorBrillante, colorMetal);
    private Color colorAmbar = new Color(1.0f, 0.76f, 0.20f, 1f);
    private Color colorVerde = new Color(0.20f, 0.95f, 0.45f, 1f);
    private Color colorRojo = new Color(0.95f, 0.25f, 0.25f, 1f);

    private float tiempo = 0f;
    private float offsetGrid = 0f;

    private static final float FLECHA_ANCHO = 100f;
    private static final float FLECHA_ALTO = 140f;
    private static final float FLECHA_Y = 460f;
    private static final float FLECHA_IZQ_X = 120f;
    private static final float FLECHA_DER_X = 1700f;

    private static final float TARJETA_X = 260f;
    private static final float TARJETA_Y = 170f;
    private static final float TARJETA_ANCHO = 1400f;
    private static final float TARJETA_ALTO = 720f;

    private static final float VISUAL_X = TARJETA_X + 50f;
    private static final float VISUAL_Y = TARJETA_Y + 120f;
    private static final float VISUAL_LADO = 480f;

    private static final float BOTON_ESTADO_Y = TARJETA_Y + 46f;
    private static final float BOTON_ESTADO_ALTO = 56f;
    private static final float BOTON_ESTADO_SEPARACION = 10f;

    private static final float TEXTO_X = TARJETA_X + 610f;
    private static final float TEXTO_ANCHO = 740f;
    private static final float BLOQUE_ALTO = 160f;

    private static final float SELECTOR_Y = 96f;
    private static final float SELECTOR_ALTO = 44f;
    private static final float SELECTOR_SEPARACION = 10f;

    private static final int HOVER_NINGUNO = -1;
    private static final int HOVER_VOLVER = 100;
    private static final int HOVER_ANTERIOR = 101;
    private static final int HOVER_SIGUIENTE = 102;

    private int indice = 0;
    private int ultimoHover = HOVER_NINGUNO;
    private float hoverVolver = 0f;
    private float hoverPrev = 0f;
    private float hoverNext = 0f;

    private ArrayList<ElementoGuia> elementos;

    public VistaGuia() {
        this((Game) Gdx.app.getApplicationListener());
    }

    public VistaGuia(Game juego) {
        this.juego = juego;
        this.gestorAudio = GestorAudio.obtenerDesde(juego);
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
        this.fontCardTitulo.getData().setScale(2.3f);

        this.fontTexto = new BitmapFont();
        this.fontTexto.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontTexto.getData().setScale(1.4f);

        this.fontPequena = new BitmapFont();
        this.fontPequena.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontPequena.getData().setScale(1.05f);

        this.fontBotones = new BitmapFont();
        this.fontBotones.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fontBotones.getData().setScale(1.15f);

        this.texturas = new ArrayList<>();
        cargarAnimaciones();

        this.elementos = new ArrayList<>();
        cargarElementos();
    }

    private void cargarAnimaciones() {
        animAnd = cargarAnimacion("and", 12, 0.12f);
        animOr = cargarAnimacion("or", 12, 0.12f);
        animCapacitorVacio = cargarAnimacion("capacitor_vacio", 8, 0.14f);
        animCapacitorCargado = cargarAnimacion("capacitor_cargado", 8, 0.10f);
        animCapacitorExplotado = cargarAnimacion("capacitor_explotado", 8, 0.08f);
        anim555Astable = cargarAnimacion("555_astable", 10, 0.07f);
        anim555Monoestable = cargarAnimacion("555_monoestable", 10, 0.20f);
        animPotenciometro = cargarAnimacion("potenciometro", 8, 0.14f);
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
        elementos.add(new ElementoGuia(
                "DIODO LED", "LUZ",
                "Una pequeña luz que se enciende cuando la corriente pasa a través de ella.",
                "Es la lucecita que te avisa que el cargador del celular está conectado.",
                "Entra por la pata (+) roja y sale por la (-) azul. La resistencia va en la pata (-).",
                new String[]{"APAGADO", "ENCENDIDO", "QUEMADO"}, 0));

        elementos.add(new ElementoGuia(
                "RESISTENCIA", "PROTECCIÓN",
                "Frena el exceso de corriente para que ninguna pieza se queme.",
                "Como la llave del grifo: deja pasar el agua, pero sin que salga a chorro.",
                "Conéctala a la pata (-) del LED. Funciona igual en cualquier sentido.",
                new String[]{"NORMAL", "QUEMADA"}, 1));

        elementos.add(new ElementoGuia(
                "FUENTE DE PODER", "ENERGÍA",
                "Entrega la energía (5 voltios) que hace funcionar todo el circuito.",
                "Es como la batería del control remoto o el tomacorriente de tu casa.",
                "La energía sale por (+) y debe regresar al (-). Nunca los unas directo.",
                new String[]{"ON (5V)", "OFF (0V)"}, 2));

        elementos.add(new ElementoGuia(
                "INTERRUPTOR", "CONTROL",
                "Abre o cierra el camino por donde viaja la corriente.",
                "Es el apagador de la luz de tu cuarto.",
                "Dale clic para cambiarlo: ON deja pasar la corriente, OFF la corta.",
                new String[]{"CERRADO", "ABIERTO"}, 3));

        elementos.add(new ElementoGuia(
                "COMPUERTA AND", "LÓGICA  \"Y\"",
                "Solo enciende su salida si la entrada A Y la entrada B tienen corriente.",
                "El microondas solo calienta si la puerta está cerrada Y presionaste Start.",
                "Si falta una sola entrada, la salida se queda apagada.",
                new String[]{"0 · 0 = 0", "1 · 0 = 0", "1 · 1 = 1"}, 4));

        elementos.add(new ElementoGuia(
                "COMPUERTA OR", "LÓGICA  \"O\"",
                "Enciende su salida si la entrada A O la entrada B (o las dos) tienen corriente.",
                "La luz de la escalera: la prendes desde el apagador de abajo O el de arriba.",
                "Con una sola entrada encendida es suficiente.",
                new String[]{"0 + 0 = 0", "1 + 0 = 1", "1 + 1 = 1"}, 5));

        elementos.add(new ElementoGuia(
                "CAPACITOR", "ALMACÉN",
                "Guarda energía como un tanque pequeño y la suelta poco a poco.",
                "La lucecita del cargador que tarda en apagarse cuando lo desconectas.",
                "La pata larga es (+) y va hacia la corriente. Si lo conectas al revés, explota.",
                new String[]{"VACÍO", "CARGADO", "EXPLOTADO"}, 8));

        ElementoGuia chip = new ElementoGuia(
                "CHIP 555", "TEMPORIZADOR",
                "", "", "",
                new String[]{"LOS PINES", "ASTABLE", "MONOESTABLE"}, 9);
        chip.textosPorEstado = new String[][]{
            {},
            {"¿QUÉ HACE?", "En ASTABLE la salida OUT se prende y se apaga sola, una y otra vez, sin que toques nada.",
             "EN LA VIDA DIARIA", "Las luces de una ambulancia o la direccional del carro.",
             "PINES QUE USAS", "VCC a la Fuente (+), GND a la Fuente (-), C al capacitor (+) y OUT al LED. TRIG se queda libre."},
            {"¿QUÉ HACE?", "En MONOESTABLE espera un toque en TRIG; entonces prende OUT unos segundos y se apaga solo.",
             "EN LA VIDA DIARIA", "La luz del pasillo que se apaga sola o el timbre que suena un rato.",
             "PINES QUE USAS", "VCC, GND, C y OUT igual que en ASTABLE. TRIG va a un botón conectado a la Fuente (+)."}
        };
        elementos.add(chip);

        elementos.add(new ElementoGuia(
                "POTENCIÓMETRO", "AJUSTE",
                "Una resistencia que cambia al girar su perilla: deja pasar más o menos corriente.",
                "El regulador de luz de un cuarto o la perilla del volumen de una bocina.",
                "Haz clic en él y arrastra la perilla de su barra. Deja también la resistencia de 220 Ohm.",
                new String[]{"20%", "50%", "100%"}, 10));

        elementos.add(new ElementoGuia(
                "CABLES", "CONEXIÓN",
                "Unen las piezas y llevan la corriente de una a otra.",
                "Son como las tuberías que llevan el agua por toda la casa.",
                "Arrastra de un punto a otro para conectar. Clic derecho para borrar.",
                new String[]{"ACTIVO", "REPOSO"}, 6));

        elementos.add(new ElementoGuia(
                "ELECTRONES", "ENERGÍA",
                "Partículas diminutas que viajan por el cable llevando la energía.",
                "Como carros en una carretera: solo avanzan si el camino está completo.",
                "Si ves electrones moviéndose, tu circuito está cerrado.",
                new String[]{"FLUYENDO", "PAUSADO"}, 7));
    }

    private static final String[] PINES_555 = {"VCC", "TRIG", "GND", "OUT", "C"};
    private static final float[] PINES_555_Y = {236f, 201f, 166f, 236f, 166f};
    private static final float ESCALA_555 = 460f / 360f;
    private static final float PIN_CAJA_ANCHO = 78f;
    private static final float PIN_CAJA_ALTO = 38f;

    private static final String[][] TEXTOS_PINES_555 = {
        {"PIN VCC: LA ENERGÍA", "Es la entrada de energía del chip. Sin VCC el 555 está apagado y ningún otro pin funciona.",
         "¿A DÓNDE VA?", "Siempre a la Fuente (+). En un chip 555 real es la pata 8.",
         "SI LO OLVIDAS", "El chip no despierta: OUT nunca se prende, ni en ASTABLE ni en MONOESTABLE."},
        {"PIN TRIG: EL GATILLO", "Es el botón de arranque. Cuando recibe un toque de corriente, el chip empieza a contar el tiempo.",
         "¿A DÓNDE VA?", "Solo en MONOESTABLE, a un botón conectado a la Fuente (+). En ASTABLE se queda libre. En un chip real es la pata 2.",
         "SI LO OLVIDAS", "En MONOESTABLE el chip se queda esperando para siempre y la luz nunca se prende."},
        {"PIN GND: EL REGRESO", "Es la salida de la corriente de vuelta a la fuente. Junto con VCC cierra el circuito del chip.",
         "¿A DÓNDE VA?", "Siempre a la Fuente (-). En un chip 555 real es la pata 1.",
         "SI LO OLVIDAS", "La corriente no tiene por dónde regresar y el chip se queda apagado."},
        {"PIN OUT: LA SALIDA", "Es el pin que entrega el resultado: se prende y se apaga según el modo y el tiempo del chip.",
         "¿A DÓNDE VA?", "A la pata (+) del LED. La pata (-) del LED va a la resistencia de 220 Ohm y de ahí a la Fuente (-). En un chip real es la pata 3.",
         "SI LO OLVIDAS", "El chip trabaja por dentro, pero no hay ninguna luz que muestre lo que hace."},
        {"PIN C: EL RELOJ", "Aquí va el capacitor. El chip mide cuánto tarda en llenarse y vaciarse: ese es su tiempo.",
         "¿A DÓNDE VA?", "A la pata (+) del capacitor. La pata (-) del capacitor va a la Fuente (-). En un chip real son las patas 6 y 7.",
         "SI LO OLVIDAS", "El chip no tiene con qué medir el tiempo: OUT no parpadea ni cuenta."}
    };

    private int pinSeleccionado = 0;

    private boolean mostrandoPines555(ElementoGuia item) {
        return item.tipoVisual == 9 && item.estadoSeleccionado == 0;
    }

    private float getPinCajaX(int i) {
        if (i < 3) return VISUAL_X + 10f + 78f * ESCALA_555 - PIN_CAJA_ANCHO + 8f;
        return VISUAL_X + 10f + 284f * ESCALA_555 - 8f;
    }

    private float getPinCajaY(int i) {
        return VISUAL_Y + 10f + PINES_555_Y[i] * ESCALA_555 - 29f;
    }

    private static class ElementoGuia {
        String nombre;
        String categoria;
        String queEs;
        String vidaDiaria;
        String reglaDeOro;
        String[] nombresEstados;
        String[][] textosPorEstado;
        int tipoVisual;
        int estadoSeleccionado = 0;
        float animHover = 0f;

        ElementoGuia(String nombre, String categoria, String queEs, String vidaDiaria, String reglaDeOro,
                     String[] nombresEstados, int tipoVisual) {
            this.nombre = nombre;
            this.categoria = categoria;
            this.queEs = queEs;
            this.vidaDiaria = vidaDiaria;
            this.reglaDeOro = reglaDeOro;
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

                if (estaAdentro(mx, my, FLECHA_IZQ_X, FLECHA_Y, FLECHA_ANCHO, FLECHA_ALTO)) {
                    cambiarTarjeta(-1);
                    return true;
                }
                if (estaAdentro(mx, my, FLECHA_DER_X, FLECHA_Y, FLECHA_ANCHO, FLECHA_ALTO)) {
                    cambiarTarjeta(1);
                    return true;
                }

                for (int i = 0; i < elementos.size(); i++) {
                    if (estaAdentro(mx, my, getSelectorX(i), SELECTOR_Y, getSelectorAncho(), SELECTOR_ALTO)) {
                        irATarjeta(i);
                        return true;
                    }
                }

                ElementoGuia item = elementos.get(indice);
                if (mostrandoPines555(item)) {
                    for (int i = 0; i < PINES_555.length; i++) {
                        if (estaAdentro(mx, my, getPinCajaX(i), getPinCajaY(i), PIN_CAJA_ANCHO, PIN_CAJA_ALTO)) {
                            pinSeleccionado = i;
                            sonar(TipoEfecto.CONMUTAR_SWITCH);
                            return true;
                        }
                    }
                    if (estaAdentro(mx, my, VISUAL_X, VISUAL_Y, VISUAL_LADO, VISUAL_LADO)) {
                        pinSeleccionado = (pinSeleccionado + 1) % PINES_555.length;
                        sonar(TipoEfecto.CONMUTAR_SWITCH);
                        return true;
                    }
                }
                int cantidad = item.nombresEstados.length;
                for (int b = 0; b < cantidad; b++) {
                    if (estaAdentro(mx, my, getBotonEstadoX(b, cantidad), BOTON_ESTADO_Y, getBotonEstadoAncho(cantidad), BOTON_ESTADO_ALTO)) {
                        item.estadoSeleccionado = b;
                        sonar(TipoEfecto.CONMUTAR_SWITCH);
                        return true;
                    }
                }
                if (cantidad > 0 && estaAdentro(mx, my, VISUAL_X, VISUAL_Y, VISUAL_LADO, VISUAL_LADO)) {
                    item.estadoSeleccionado = (item.estadoSeleccionado + 1) % cantidad;
                    sonar(TipoEfecto.CONMUTAR_SWITCH);
                    return true;
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
                    cambiarTarjeta(-1);
                    return true;
                }
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D || keycode == Input.Keys.TAB) {
                    cambiarTarjeta(1);
                    return true;
                }
                int numero = keycode - Input.Keys.NUM_1;
                if (numero >= 0 && numero < elementos.size()) {
                    irATarjeta(numero);
                    return true;
                }
                return false;
            }
        });
    }

    private void sonar(TipoEfecto efecto) {
        if (gestorAudio != null) {
            gestorAudio.reproducirEfecto(efecto);
        }
    }

    private void volverAlMenu() {
        sonar(TipoEfecto.CLICK_UI);
        if (juego != null) {
            juego.setScreen(new VistaMenu(juego));
        }
    }

    private boolean estaAdentro(float x, float y, float bx, float by, float bw, float bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    private float getBotonEstadoAncho(int cantidad) {
        return (VISUAL_LADO - (cantidad - 1) * BOTON_ESTADO_SEPARACION) / cantidad;
    }

    private float getBotonEstadoX(int b, int cantidad) {
        return VISUAL_X + b * (getBotonEstadoAncho(cantidad) + BOTON_ESTADO_SEPARACION);
    }

    private void cambiarTarjeta(int direccion) {
        irATarjeta((indice + direccion + elementos.size()) % elementos.size());
    }

    private void irATarjeta(int nuevoIndice) {
        indice = nuevoIndice;
        sonar(TipoEfecto.CLICK_UI);
    }

    private float getSelectorAncho() {
        int n = elementos.size();
        return (TARJETA_ANCHO - (n - 1) * SELECTOR_SEPARACION) / n;
    }

    private float getSelectorX(int i) {
        return TARJETA_X + i * (getSelectorAncho() + SELECTOR_SEPARACION);
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
        dibujarFlechasShapes();
        dibujarTarjetasShapes(mx, my);
        dibujarSelectorShapes(mx, my);
        dibujarBotonVolverShape();
        shapeRenderer.end();

        batch.begin();
        dibujarCabecera();
        dibujarContenido(mx, my);
        dibujarSelectorTextos();
        dibujarBotonVolverTexto();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void actualizarAnimaciones(float delta, float mx, float my) {
        int hoverActual = HOVER_NINGUNO;
        boolean hVolver = estaAdentro(mx, my, 60f, 20f, 270f, 50f);
        hoverVolver = MathUtils.lerp(hoverVolver, hVolver ? 1f : 0f, delta * 18f);
        if (hVolver) hoverActual = HOVER_VOLVER;

        boolean hPrev = estaAdentro(mx, my, FLECHA_IZQ_X, FLECHA_Y, FLECHA_ANCHO, FLECHA_ALTO);
        boolean hNext = estaAdentro(mx, my, FLECHA_DER_X, FLECHA_Y, FLECHA_ANCHO, FLECHA_ALTO);
        hoverPrev = MathUtils.lerp(hoverPrev, hPrev ? 1f : 0f, delta * 18f);
        hoverNext = MathUtils.lerp(hoverNext, hNext ? 1f : 0f, delta * 18f);
        if (hPrev) hoverActual = HOVER_ANTERIOR;
        else if (hNext) hoverActual = HOVER_SIGUIENTE;

        for (int i = 0; i < elementos.size(); i++) {
            if (estaAdentro(mx, my, getSelectorX(i), SELECTOR_Y, getSelectorAncho(), SELECTOR_ALTO)) {
                hoverActual = i;
            }
        }

        ElementoGuia actual = elementos.get(indice);
        boolean hCard = estaAdentro(mx, my, TARJETA_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO);
        actual.animHover = MathUtils.lerp(actual.animHover, hCard ? 1f : 0f, delta * 16f);

        if (hoverActual != ultimoHover) {
            if (hoverActual != HOVER_NINGUNO) {
                sonar(TipoEfecto.HOVER_UI);
            }
            ultimoHover = hoverActual;
        }
    }

    private void dibujarSelectorShapes(float mx, float my) {
        float ancho = getSelectorAncho();
        for (int i = 0; i < elementos.size(); i++) {
            float x = getSelectorX(i);
            boolean actual = (i == indice);
            boolean hover = estaAdentro(mx, my, x, SELECTOR_Y, ancho, SELECTOR_ALTO);
            shapeRenderer.setColor(actual ? new Color(0.06f, 0.16f, 0.24f, 1f) : colorCaja);
            shapeRenderer.rect(x, SELECTOR_Y, ancho, SELECTOR_ALTO);
            dibujarBorde(x, SELECTOR_Y, ancho, SELECTOR_ALTO, actual ? colorBrillante : (hover ? colorNeon : colorMetal), actual ? 2.6f : 1.4f);
            if (actual) {
                shapeRenderer.setColor(colorNeon);
                shapeRenderer.rect(x, SELECTOR_Y, ancho, 4f);
            }
        }
    }

    private void dibujarSelectorTextos() {
        float ancho = getSelectorAncho();
        for (int i = 0; i < elementos.size(); i++) {
            String nombre = elementos.get(i).nombre;
            layout.setText(fontPequena, nombre);
            if (layout.width > ancho - 10f) {
                fontPequena.getData().setScale(1.05f * (ancho - 10f) / layout.width);
                layout.setText(fontPequena, nombre);
            }
            fontPequena.setColor(i == indice ? Color.WHITE : colorClaro);
            fontPequena.draw(batch, nombre, getSelectorX(i) + (ancho - layout.width) / 2f, SELECTOR_Y + (SELECTOR_ALTO + layout.height) / 2f);
            fontPequena.getData().setScale(1.05f);
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

    private void dibujarFlechasShapes() {
        dibujarFlechaShape(FLECHA_IZQ_X, hoverPrev, true);
        dibujarFlechaShape(FLECHA_DER_X, hoverNext, false);
    }

    private void dibujarFlechaShape(float x, float hover, boolean haciaIzquierda) {
        shapeRenderer.setColor(colorCaja);
        shapeRenderer.rect(x, FLECHA_Y, FLECHA_ANCHO, FLECHA_ALTO);
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.4f + hover * 0.6f);
        shapeRenderer.rectLine(x, FLECHA_Y, x + FLECHA_ANCHO, FLECHA_Y, 2.5f);
        shapeRenderer.rectLine(x + FLECHA_ANCHO, FLECHA_Y, x + FLECHA_ANCHO, FLECHA_Y + FLECHA_ALTO, 2.5f);
        shapeRenderer.rectLine(x + FLECHA_ANCHO, FLECHA_Y + FLECHA_ALTO, x, FLECHA_Y + FLECHA_ALTO, 2.5f);
        shapeRenderer.rectLine(x, FLECHA_Y + FLECHA_ALTO, x, FLECHA_Y, 2.5f);

        float cx = x + FLECHA_ANCHO / 2f;
        float cy = FLECHA_Y + FLECHA_ALTO / 2f;
        float punta = haciaIzquierda ? -22f : 22f;
        shapeRenderer.setColor(hover > 0.5f ? Color.WHITE : colorNeon);
        shapeRenderer.triangle(cx + punta, cy, cx - punta, cy + 26f, cx - punta, cy - 26f);
    }

    private void dibujarCabecera() {
        String titulo = "GUÍA DE COMPONENTES";
        layout.setText(fontTitulo, titulo);
        float tx = (Constantes.ANCHO_VENTANA - layout.width) / 2f;

        fontTitulo.setColor(0f, 0f, 0f, 0.9f);
        fontTitulo.draw(batch, titulo, tx + 3f, 1052f);
        fontTitulo.setColor(colorBrillante);
        fontTitulo.draw(batch, titulo, tx, 1055f);

        String sub = "Una pieza a la vez. Usa las flechas para conocer la siguiente.";
        layout.setText(fontSubtitulo, sub);
        fontSubtitulo.setColor(colorClaro);
        fontSubtitulo.draw(batch, sub, (Constantes.ANCHO_VENTANA - layout.width) / 2f, 1022f);

    }

    private void dibujarTarjetasShapes(float mx, float my) {
        ElementoGuia item = elementos.get(indice);
        float x = TARJETA_X, y = TARJETA_Y, w = TARJETA_ANCHO, h = TARJETA_ALTO;
        float hov = item.animHover;

        shapeRenderer.setColor(0f, 0f, 0f, 0.85f);
        shapeRenderer.rect(x + 14f, y - 14f, w, h);
        shapeRenderer.setColor(colorCaja.r, colorCaja.g, colorCaja.b, 0.98f);
        shapeRenderer.rect(x, y, w, h);

        float grosor = 2.5f + hov * 1.5f;
        dibujarBorde(x, y, w, h, hov > 0.5f ? colorBrillante : colorNeon, grosor);
        dibujarEsquinas(x, y, w, h, 26f, grosor + 2f);

        shapeRenderer.setColor(0.015f, 0.025f, 0.045f, 0.98f);
        shapeRenderer.rect(VISUAL_X, VISUAL_Y, VISUAL_LADO, VISUAL_LADO);
        shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, 0.10f);
        for (float g = 50f; g < VISUAL_LADO; g += 50f) {
            shapeRenderer.rectLine(VISUAL_X, VISUAL_Y + g, VISUAL_X + VISUAL_LADO, VISUAL_Y + g, 1f);
            shapeRenderer.rectLine(VISUAL_X + g, VISUAL_Y, VISUAL_X + g, VISUAL_Y + VISUAL_LADO, 1f);
        }
        dibujarBorde(VISUAL_X, VISUAL_Y, VISUAL_LADO, VISUAL_LADO, new Color(colorNeon.r, colorNeon.g, colorNeon.b, 0.55f), 2f);

        int cantidad = item.nombresEstados.length;
        for (int b = 0; b < cantidad; b++) {
            float bx = getBotonEstadoX(b, cantidad);
            float bw = getBotonEstadoAncho(cantidad);
            boolean sel = (item.estadoSeleccionado == b);
            boolean hBtn = estaAdentro(mx, my, bx, BOTON_ESTADO_Y, bw, BOTON_ESTADO_ALTO);
            shapeRenderer.setColor(sel ? new Color(0.06f, 0.16f, 0.24f, 1f) : colorSlate);
            shapeRenderer.rect(bx, BOTON_ESTADO_Y, bw, BOTON_ESTADO_ALTO);
            dibujarBorde(bx, BOTON_ESTADO_Y, bw, BOTON_ESTADO_ALTO, sel ? colorBrillante : (hBtn ? colorNeon : colorMetal), sel ? 2.8f : 1.5f);
        }

        layout.setText(fontPequena, item.categoria);
        float chipW = layout.width + 36f;
        float chipX = x + w - chipW - 40f;
        float chipY = y + h - 72f;
        shapeRenderer.setColor(colorAmbar.r, colorAmbar.g, colorAmbar.b, 0.16f);
        shapeRenderer.rect(chipX, chipY, chipW, 40f);
        dibujarBorde(chipX, chipY, chipW, 40f, colorAmbar, 1.8f);

        dibujarBloqueShape(getBloqueY(0), colorNeon);
        dibujarBloqueShape(getBloqueY(1), colorVerde);
        dibujarBloqueShape(getBloqueY(2), colorAmbar);

        if (mostrandoPines555(item)) {
            for (int i = 0; i < PINES_555.length; i++) {
                float px = getPinCajaX(i), py = getPinCajaY(i);
                boolean sel = (pinSeleccionado == i);
                boolean hPin = estaAdentro(mx, my, px, py, PIN_CAJA_ANCHO, PIN_CAJA_ALTO);
                if (sel) {
                    float brillo = 0.25f + 0.15f * MathUtils.sin(tiempo * 5f);
                    shapeRenderer.setColor(colorNeon.r, colorNeon.g, colorNeon.b, brillo);
                    shapeRenderer.rect(px - 6f, py - 6f, PIN_CAJA_ANCHO + 12f, PIN_CAJA_ALTO + 12f);
                }
                shapeRenderer.setColor(sel ? new Color(0.06f, 0.16f, 0.24f, 1f) : colorSlate);
                shapeRenderer.rect(px, py, PIN_CAJA_ANCHO, PIN_CAJA_ALTO);
                dibujarBorde(px, py, PIN_CAJA_ANCHO, PIN_CAJA_ALTO, sel ? colorBrillante : (hPin ? colorNeon : colorMetal), sel ? 2.8f : 1.5f);
            }
        }

        shapeRenderer.translate(VISUAL_X + 20f, VISUAL_Y + 10f, 0f);
        shapeRenderer.scale(2f, 2f, 1f);
        dibujarProceduralShapes(item, 0f, 0f, 220f, 230f);
        shapeRenderer.identity();
    }

    private float getBloqueY(int numero) {
        return TARJETA_Y + 440f - numero * (BLOQUE_ALTO + 20f);
    }

    private void dibujarBloqueShape(float by, Color color) {
        shapeRenderer.setColor(0.035f, 0.055f, 0.085f, 0.95f);
        shapeRenderer.rect(TEXTO_X, by, TEXTO_ANCHO, BLOQUE_ALTO);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(TEXTO_X, by, 8f, BLOQUE_ALTO);
        dibujarBorde(TEXTO_X, by, TEXTO_ANCHO, BLOQUE_ALTO, new Color(color.r, color.g, color.b, 0.35f), 1.5f);
    }

    private void dibujarBorde(float x, float y, float w, float h, Color color, float grosor) {
        shapeRenderer.setColor(color);
        shapeRenderer.rectLine(x, y, x + w, y, grosor);
        shapeRenderer.rectLine(x + w, y, x + w, y + h, grosor);
        shapeRenderer.rectLine(x + w, y + h, x, y + h, grosor);
        shapeRenderer.rectLine(x, y + h, x, y, grosor);
    }

    private void dibujarEsquinas(float x, float y, float w, float h, float largo, float grosor) {
        shapeRenderer.setColor(colorBrillante);
        shapeRenderer.rectLine(x, y, x + largo, y, grosor);
        shapeRenderer.rectLine(x, y, x, y + largo, grosor);
        shapeRenderer.rectLine(x + w, y, x + w - largo, y, grosor);
        shapeRenderer.rectLine(x + w, y, x + w, y + largo, grosor);
        shapeRenderer.rectLine(x, y + h, x + largo, y + h, grosor);
        shapeRenderer.rectLine(x, y + h, x, y + h - largo, grosor);
        shapeRenderer.rectLine(x + w, y + h, x + w - largo, y + h, grosor);
        shapeRenderer.rectLine(x + w, y + h, x + w, y + h - largo, grosor);
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
        ElementoGuia item = elementos.get(indice);
        float x = TARJETA_X, y = TARJETA_Y, h = TARJETA_ALTO;

        fontPequena.setColor(colorMetal);
        fontPequena.draw(batch, "PIEZA " + (indice + 1) + " DE " + elementos.size(), x + 50f, y + h - 30f);
        fontCardTitulo.setColor(Color.WHITE);
        fontCardTitulo.draw(batch, item.nombre, x + 50f, y + h - 62f);

        layout.setText(fontPequena, item.categoria);
        fontPequena.setColor(colorAmbar);
        fontPequena.draw(batch, item.categoria, x + TARJETA_ANCHO - 40f - 18f - layout.width, y + h - 42f);

        dibujarSpriteDePieza(item);

        int cantidad = item.nombresEstados.length;
        for (int b = 0; b < cantidad; b++) {
            String nom = item.nombresEstados[b];
            float bx = getBotonEstadoX(b, cantidad);
            float bw = getBotonEstadoAncho(cantidad);
            layout.setText(fontPequena, nom);
            fontPequena.setColor(item.estadoSeleccionado == b ? Color.WHITE : colorClaro);
            fontPequena.draw(batch, nom, bx + (bw - layout.width) / 2f, BOTON_ESTADO_Y + (BOTON_ESTADO_ALTO + layout.height) / 2f);
        }
        fontPequena.setColor(colorMetal);
        String ayuda = mostrandoPines555(item) ? "Toca un pin del chip para ver qué hace" : "Toca la pieza o un botón para ver sus estados";
        fontPequena.draw(batch, ayuda, VISUAL_X, BOTON_ESTADO_Y - 14f);

        if (item.textosPorEstado != null) {
            String[] textos = mostrandoPines555(item) ? TEXTOS_PINES_555[pinSeleccionado] : item.textosPorEstado[item.estadoSeleccionado];
            dibujarBloqueTexto(getBloqueY(0), textos[0], textos[1], colorNeon);
            dibujarBloqueTexto(getBloqueY(1), textos[2], textos[3], colorVerde);
            dibujarBloqueTexto(getBloqueY(2), textos[4], textos[5], colorAmbar);
        } else {
            dibujarBloqueTexto(getBloqueY(0), "¿QUÉ ES?", item.queEs, colorNeon);
            dibujarBloqueTexto(getBloqueY(1), "EN LA VIDA DIARIA", item.vidaDiaria, colorVerde);
            dibujarBloqueTexto(getBloqueY(2), "REGLA DE ORO", item.reglaDeOro, colorAmbar);
        }
    }

    private void dibujarBloqueTexto(float by, String etiqueta, String texto, Color color) {
        fontPequena.setColor(color);
        fontPequena.draw(batch, etiqueta, TEXTO_X + 30f, by + BLOQUE_ALTO - 18f);
        fontTexto.setColor(Color.WHITE);
        fontTexto.draw(batch, texto, TEXTO_X + 30f, by + BLOQUE_ALTO - 56f, TEXTO_ANCHO - 60f, Align.left, true);
    }

    private void dibujarPines555(float x, float y, float escala, int estado) {
        for (int i = 0; i < PINES_555.length; i++) {
            Color color = colorNeon;
            if (estado == 0) {
                color = (pinSeleccionado == i) ? Color.WHITE : colorClaro;
            } else if (PINES_555[i].equals("TRIG")) {
                if (estado == 1) color = colorMetal;
                else color = colorAmbar;
            }
            fontTexto.setColor(color);
            layout.setText(fontTexto, PINES_555[i]);
            float px;
            if (estado == 0) px = getPinCajaX(i) + (PIN_CAJA_ANCHO - layout.width) / 2f;
            else px = (i < 3) ? x + 78f * escala - layout.width : x + 284f * escala;
            fontTexto.draw(batch, PINES_555[i], px, y + PINES_555_Y[i] * escala);
        }
        if (estado == 0) {
            String[] papeles = {"energía", "gatillo", "regreso", "salida", "reloj"};
            for (int i = 0; i < PINES_555.length; i++) {
                float rx = VISUAL_X + ((i < 3) ? 50f : 270f);
                float ry = VISUAL_Y + 130f - ((i < 3) ? i : i - 3) * 38f;
                boolean sel = (pinSeleccionado == i);
                fontPequena.setColor(sel ? colorBrillante : colorNeon);
                fontPequena.draw(batch, PINES_555[i], rx, ry);
                fontPequena.setColor(sel ? Color.WHITE : colorMetal);
                fontPequena.draw(batch, papeles[i], rx + 60f, ry);
            }
        }
    }

    private void dibujarSpriteDePieza(ElementoGuia item) {
        float vx = VISUAL_X, vy = VISUAL_Y;
        if (item.tipoVisual == 0) {
            Animation<TextureRegion> anim = animLedNormal;
            if (item.estadoSeleccionado == 1) anim = animLedPrendiendo;
            else if (item.estadoSeleccionado == 2) anim = animLedMuriendo;
            batch.draw(anim.getKeyFrame(tiempo, true), vx + 40f, vy + 40f, 400f, 400f);
        } else if (item.tipoVisual == 1) {
            Animation<TextureRegion> anim = (item.estadoSeleccionado == 1) ? animResistenciaFallo : animResistenciaNormal;
            batch.draw(anim.getKeyFrame(tiempo, true), vx + 15f, vy + 95f, 450f, 290f);
        } else if (item.tipoVisual == 4) {
            batch.draw(animAnd.getKeyFrame(tiempo, true), vx + 30f, vy + 30f, 420f, 420f);
        } else if (item.tipoVisual == 5) {
            batch.draw(animOr.getKeyFrame(tiempo, true), vx + 30f, vy + 30f, 420f, 420f);
        } else if (item.tipoVisual == 10) {
            float valor = (item.estadoSeleccionado == 0) ? 0.2f : (item.estadoSeleccionado == 1 ? 0.5f : 1f);
            batch.draw(animPotenciometro.getKeyFrame(tiempo, true), vx + 20f, vy + 150f, 300f, 300f);
            batch.setColor(1f, 1f, 1f, 0.3f);
            batch.draw(animLedNormal.getKeyFrame(tiempo, true), vx + 300f, vy + 40f, 170f, 170f);
            batch.setColor(1f, 1f, 1f, valor);
            batch.draw(animLedPrendiendo.getKeyFrame(tiempo, true), vx + 300f, vy + 40f, 170f, 170f);
            batch.setColor(Color.WHITE);
        } else if (item.tipoVisual == 9) {
            Animation<TextureRegion> anim = (item.estadoSeleccionado == 1) ? anim555Astable : anim555Monoestable;
            batch.draw(anim.getKeyFrame(tiempo, true), vx + 10f, vy + 10f, 460f, 460f);
            dibujarPines555(vx + 10f, vy + 10f, 460f / 360f, item.estadoSeleccionado);
        } else if (item.tipoVisual == 8) {
            Animation<TextureRegion> anim = animCapacitorVacio;
            if (item.estadoSeleccionado == 1) anim = animCapacitorCargado;
            else if (item.estadoSeleccionado == 2) anim = animCapacitorExplotado;
            batch.draw(anim.getKeyFrame(tiempo, true), vx + 30f, vy + 30f, 420f, 420f);
        } else if (item.tipoVisual == 2) {
            boolean on = (item.estadoSeleccionado == 0);
            String voltaje = on ? "5.00 V" : "0.00 V";
            fontCardTitulo.setColor(on ? colorNeon : colorRojo);
            layout.setText(fontCardTitulo, voltaje);
            fontCardTitulo.draw(batch, voltaje, vx + (VISUAL_LADO - layout.width) / 2f, vy + 335f);
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

        atajos.dibujarCentrado(batch, fontPequena, Constantes.ANCHO_VENTANA / 2f + 120f, 52f,
                "ESC", "Volver", "FLECHAS", "Cambiar de pieza", "1 A 9", "Ir directo a una pieza");
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
