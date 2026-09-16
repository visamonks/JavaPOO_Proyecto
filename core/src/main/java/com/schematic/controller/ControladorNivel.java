package com.schematic.controller;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector3;
import com.schematic.model.Cable;
import com.schematic.model.Componente;
import com.schematic.model.Minijuego;
import com.schematic.model.Terminal;
import com.schematic.view.VistaMenu;
import java.util.ArrayList;

public class ControladorNivel extends InputAdapter {

    public static final float RADIO_TERMINAL_CLICK = 30f;
    private Minijuego minijuego;
    private Game juego;
    private OrthographicCamera camara;

    private Componente componenteSeleccionado;
    private float dragOffsetX;
    private float dragOffsetY;

    private Terminal terminalInicioCable;
    private float mouseActualX;
    private float mouseActualY;

    public ControladorNivel(Minijuego minijuego) {
        this(minijuego, null, null);
    }

    public ControladorNivel(Minijuego minijuego, OrthographicCamera camara) {
        this(minijuego, null, camara);
    }

    public ControladorNivel(Minijuego minijuego, Game juego, OrthographicCamera camara) {
        this.minijuego = minijuego;
        this.juego = juego;
        this.camara = camara;
    }

    private Vector3 obtenerCoordenadasMundo(int screenX, int screenY) {
        Vector3 vec = new Vector3(screenX, screenY, 0);
        if (camara != null) {
            camara.unproject(vec);
        }
        return vec;
    }

    private Terminal buscarTerminalEn(float mx, float my) {
        if (minijuego == null) return null;
        for (Componente comp : minijuego.getComponentes()) {
            for (Terminal t : comp.getTerminales()) {
                Vector2 pos = t.getPosicionAbsoluta();
                if (pos.dst(mx, my) <= RADIO_TERMINAL_CLICK) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;
        mouseActualX = mx;
        mouseActualY = my;

        if (button == Buttons.LEFT) {
            Terminal term = buscarTerminalEn(mx, my);
            if (term != null) {
                terminalInicioCable = term;
                return true;
            }

            if (minijuego != null) {
                for (int i = minijuego.getComponentes().size() - 1; i >= 0; i--) {
                    Componente comp = minijuego.getComponentes().get(i);
                    float cx = comp.getPosicionX();
                    float cy = comp.getPosicionY();
                    float cw = comp.getAncho();
                    float ch = comp.getAlto();

                    if (mx >= cx && mx <= cx + cw && my >= cy && my <= cy + ch) {
                        componenteSeleccionado = comp;
                        dragOffsetX = mx - cx;
                        dragOffsetY = my - cy;
                        return true;
                    }
                }
            }
        } else if (button == Buttons.RIGHT) {
            Terminal term = buscarTerminalEn(mx, my);
            if (term != null && term.estaConectado()) {
                minijuego.eliminarCable(term.getCableConectado());
                return true;
            }

            if (minijuego != null) {
                for (Cable c : new ArrayList<>(minijuego.getCables())) {
                    if (c.getTerminalOrigen() != null && c.getTerminalDestino() != null) {
                        Vector2 p1 = c.getTerminalOrigen().getPosicionAbsoluta();
                        Vector2 p2 = c.getTerminalDestino().getPosicionAbsoluta();
                        if (distanciaPuntoASegmento(mx, my, p1.x, p1.y, p2.x, p2.y) < 18f) {
                            minijuego.eliminarCable(c);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private float distanciaPuntoASegmento(float px, float py, float x1, float y1, float x2, float y2) {
        float l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (l2 == 0) return Vector2.dst(px, py, x1, y1);
        float t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2));
        float projX = x1 + t * (x2 - x1);
        float projY = y1 + t * (y2 - y1);
        return Vector2.dst(px, py, projX, projY);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 mundo = obtenerCoordenadasMundo(screenX, screenY);
        mouseActualX = mundo.x;
        mouseActualY = mundo.y;

        if (componenteSeleccionado != null) {
            componenteSeleccionado.actualizarPosicion(mouseActualX - dragOffsetX, mouseActualY - dragOffsetY);
            if (minijuego != null) {
                minijuego.evaluarCircuito();
            }
            return true;
        }

        if (terminalInicioCable != null) {
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;

        if (button == Buttons.LEFT) {
            if (terminalInicioCable != null) {
                Terminal termDestino = buscarTerminalEn(mx, my);
                if (termDestino != null && termDestino != terminalInicioCable && termDestino.getComponentePadre() != terminalInicioCable.getComponentePadre()) {
                    if (minijuego != null) {
                        if (terminalInicioCable.estaConectado()) {
                            minijuego.eliminarCable(terminalInicioCable.getCableConectado());
                        }
                        if (termDestino.estaConectado()) {
                            minijuego.eliminarCable(termDestino.getCableConectado());
                        }
                        Cable nuevoCable = new Cable(terminalInicioCable, termDestino);
                        minijuego.agregarCable(nuevoCable);
                    }
                }
                terminalInicioCable = null;
                return true;
            }

            if (componenteSeleccionado != null) {
                componenteSeleccionado = null;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.R) {
            if (minijuego != null) {
                minijuego.inicializarNivel1();
            }
            return true;
        }
        if (keycode == Keys.ESCAPE) {
            if (juego != null) {
                juego.setScreen(new VistaMenu(juego));
            }
            return true;
        }
        return false;
    }

    public Minijuego getMinijuego() {
        return minijuego;
    }

    public void setMinijuego(Minijuego minijuego) {
        this.minijuego = minijuego;
    }

    public Terminal getTerminalInicioCable() {
        return terminalInicioCable;
    }

    public float getMouseActualX() {
        return mouseActualX;
    }

    public float getMouseActualY() {
        return mouseActualY;
    }
}
