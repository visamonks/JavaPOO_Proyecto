package com.schematic.controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.schematic.model.Cable;
import com.schematic.model.Componente;
import com.schematic.model.FuenteAlimentacion;
import com.schematic.model.Minijuego;
import com.schematic.model.Terminal;
import com.schematic.view.VistaNivel;

public class ControladorNivel extends InputAdapter {

    public static final float TAMANO_CELDA = 60f;

    private Minijuego minijuego;
    private OrthographicCamera camara;
    private Viewport viewport;
    private VistaNivel vista;
    private Componente componenteSeleccionado;

    private boolean arrastrandoDesdeBarra;
    private String tipoComponenteArrastrado;
    private Vector2 posicionArrastre;

    private float desfaseArrastreX;
    private float desfaseArrastreY;

    private Cable cableArrastrandoCodo;
    private boolean arrastrandoCodo;

    public ControladorNivel(Minijuego minijuego) {
        this(minijuego, null, null, null);
    }

    public ControladorNivel(Minijuego minijuego, OrthographicCamera camara) {
        this(minijuego, camara, null, null);
    }

    public ControladorNivel(Minijuego minijuego, OrthographicCamera camara, Viewport viewport, VistaNivel vista) {
        this.minijuego = minijuego;
        this.camara = camara;
        this.viewport = viewport;
        this.vista = vista;
        this.componenteSeleccionado = null;
        this.arrastrandoDesdeBarra = false;
        this.tipoComponenteArrastrado = null;
        this.posicionArrastre = new Vector2();
        this.cableArrastrandoCodo = null;
        this.arrastrandoCodo = false;
    }

    private Vector3 obtenerCoordenadasMundo(int screenX, int screenY) {
        Vector3 vec = new Vector3(screenX, screenY, 0);
        if (viewport != null) {
            viewport.unproject(vec);
        } else if (camara != null) {
            camara.unproject(vec);
        }
        return vec;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;

        if (button == Buttons.LEFT) {
            if (vista != null && vista.manejarClicUI(mx, my)) {
                return true;
            }

            if (vista != null) {
                Cable cableCodo = vista.obtenerCableCodoBajoRaton(mx, my, 24f);
                if (cableCodo != null) {
                    cableArrastrandoCodo = cableCodo;
                    arrastrandoCodo = true;
                    return true;
                }
            }

            if (minijuego != null && mx >= 1640f && mx <= 1890f) {
                if (my >= 690f && my <= 890f) {
                    Minijuego.CasillaInventario casilla = minijuego.buscarCasilla("LED");
                    if (casilla != null && casilla.puedeUsar()) {
                        arrastrandoDesdeBarra = true;
                        tipoComponenteArrastrado = "LED";
                        posicionArrastre.set(mx, my);
                        return true;
                    }
                } else if (my >= 450f && my <= 650f) {
                    Minijuego.CasillaInventario casilla = minijuego.buscarCasilla("RESISTENCIA_220");
                    if (casilla != null && casilla.puedeUsar()) {
                        arrastrandoDesdeBarra = true;
                        tipoComponenteArrastrado = "RESISTENCIA_220";
                        posicionArrastre.set(mx, my);
                        return true;
                    }
                }
            }

            if (vista != null) {
                Terminal term = vista.obtenerTerminalBajoRaton(mx, my, 34f);
                if (term != null) {
                    vista.iniciarCable(term, mx, my);
                    return true;
                }

                Componente comp = vista.obtenerComponenteBajoRaton(mx, my);
                if (comp != null && !(comp instanceof FuenteAlimentacion)) {
                    componenteSeleccionado = comp;
                    desfaseArrastreX = mx - comp.getPosicionX();
                    desfaseArrastreY = my - comp.getPosicionY();
                    return true;
                }
            }
        } else if (button == Buttons.RIGHT) {
            if (arrastrandoDesdeBarra) {
                arrastrandoDesdeBarra = false;
                tipoComponenteArrastrado = null;
                return true;
            }

            if (vista != null && vista.estaCableEnProgreso()) {
                vista.cancelarCable();
                return true;
            }

            if (vista != null && minijuego != null) {
                Terminal term = vista.obtenerTerminalBajoRaton(mx, my, 34f);
                if (term != null) {
                    Cable c = minijuego.buscarCableConTerminal(term);
                    if (c != null) {
                        minijuego.eliminarCable(c);
                        return true;
                    }
                }

                Cable cableBajoRaton = vista.obtenerCableBajoRaton(mx, my, 24f);
                if (cableBajoRaton != null) {
                    minijuego.eliminarCable(cableBajoRaton);
                    return true;
                }

                Componente comp = vista.obtenerComponenteBajoRaton(mx, my);
                if (comp != null && !(comp instanceof FuenteAlimentacion)) {
                    minijuego.devolverAlInventario(comp);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;

        if (vista != null) {
            vista.actualizarPosicionCursor(mx, my);
        }

        if (arrastrandoCodo && cableArrastrandoCodo != null) {
            cableArrastrandoCodo.setCodoPersonalizado(mx, my);
            return true;
        }

        if (arrastrandoDesdeBarra) {
            posicionArrastre.set(mx, my);
            return true;
        }

        if (vista != null && vista.estaCableEnProgreso()) {
            vista.actualizarCableEnProgreso(mx, my);
            return true;
        }

        if (componenteSeleccionado != null) {
            float nuevaX = mx - desfaseArrastreX;
            float nuevaY = my - desfaseArrastreY;
            if (nuevaX < 320f) nuevaX = 320f;
            if (nuevaX > 1720f) nuevaX = 1720f;
            if (nuevaY < 80f) nuevaY = 80f;
            if (nuevaY > 820f) nuevaY = 820f;
            componenteSeleccionado.actualizarPosicion(nuevaX, nuevaY);
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
            if (arrastrandoCodo) {
                arrastrandoCodo = false;
                cableArrastrandoCodo = null;
                return true;
            }

            if (arrastrandoDesdeBarra) {
                if (mx >= 340f && mx <= 1580f && my >= 100f && my <= 880f) {
                    float posX = mx;
                    float posY = my;
                    if ("LED".equals(tipoComponenteArrastrado)) {
                        posX = mx - 160f;
                        posY = my - 160f;
                    } else if ("RESISTENCIA_220".equals(tipoComponenteArrastrado)) {
                        posX = mx - 180f;
                        posY = my - 115f;
                    }
                    minijuego.crearYColocarComponente(tipoComponenteArrastrado, posX, posY);
                }
                arrastrandoDesdeBarra = false;
                tipoComponenteArrastrado = null;
                return true;
            }

            if (vista != null && vista.estaCableEnProgreso()) {
                Terminal destino = vista.obtenerTerminalBajoRaton(mx, my, 34f);
                vista.finalizarCable(destino);
                return true;
            }

            if (componenteSeleccionado != null) {
                if (mx >= 1620f) {
                    minijuego.devolverAlInventario(componenteSeleccionado);
                }
                componenteSeleccionado = null;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector3 mundo = obtenerCoordenadasMundo(screenX, screenY);
        if (vista != null) {
            vista.actualizarPosicionCursor(mundo.x, mundo.y);
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            if (vista != null) {
                vista.volverAlMenu();
                return true;
            }
        } else if (keycode == Input.Keys.R) {
            if (vista != null) {
                vista.reiniciarNivel();
                return true;
            }
        } else if (keycode == Input.Keys.SPACE) {
            if (vista != null) {
                vista.saltarIntro();
                return true;
            }
        }
        return false;
    }

    public boolean estaArrastrandoDesdeBarra() {
        return arrastrandoDesdeBarra;
    }

    public boolean isArrastrandoDesdeSidebar() {
        return arrastrandoDesdeBarra;
    }

    public String getTipoComponenteArrastrado() {
        return tipoComponenteArrastrado;
    }

    public Vector2 getPosicionArrastre() {
        return posicionArrastre;
    }

    public Minijuego getMinijuego() {
        return minijuego;
    }

    public void setMinijuego(Minijuego minijuego) {
        this.minijuego = minijuego;
    }

    public OrthographicCamera getCamara() {
        return camara;
    }

    public void setCamara(OrthographicCamera camara) {
        this.camara = camara;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public VistaNivel getVista() {
        return vista;
    }

    public void setVista(VistaNivel vista) {
        this.vista = vista;
    }

    public Componente getComponenteSeleccionado() {
        return componenteSeleccionado;
    }

    public void setComponenteSeleccionado(Componente componenteSeleccionado) {
        this.componenteSeleccionado = componenteSeleccionado;
    }
}
