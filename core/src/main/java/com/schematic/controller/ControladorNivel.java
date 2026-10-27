package com.schematic.controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.schematic.audio.GestorAudio;
import com.schematic.audio.TipoEfecto;
import com.schematic.model.componentes.Cable;
import com.schematic.model.componentes.Componente;
import com.schematic.model.componentes.FuenteAlimentacion;
import com.schematic.model.componentes.Switch;
import com.schematic.model.componentes.Terminal;
import com.schematic.model.niveles.Nivel;
import com.schematic.view.VistaNivel;

import java.util.List;

public class ControladorNivel extends InputAdapter {

    public static final float TAMANO_CELDA = 60f;

    private Nivel nivel;
    private OrthographicCamera camara;
    private Viewport viewport;
    private VistaNivel vista;
    private GestorAudio gestorAudio;
    private Componente componenteSeleccionado;

    private boolean arrastrandoDesdeBarra;
    private String tipoComponenteArrastrado;
    private Vector2 posicionArrastre;

    private float desfaseArrastreX;
    private float desfaseArrastreY;
    private float clickInicioX;
    private float clickInicioY;

    private Cable cableArrastrandoCodo;
    private boolean arrastrandoCodo;

    public ControladorNivel(Nivel nivel) {
        this(nivel, null, null, null);
    }

    public ControladorNivel(Nivel nivel, OrthographicCamera camara) {
        this(nivel, camara, null, null);
    }

    public ControladorNivel(Nivel nivel, OrthographicCamera camara, Viewport viewport, VistaNivel vista) {
        this.nivel = nivel;
        this.camara = camara;
        this.viewport = viewport;
        this.vista = vista;
        this.gestorAudio = (vista != null) ? vista.getGestorAudio() : null;
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
                Cable cableCodo = vista.obtenerCableCodoBajoRaton(mx, my, 22f);
                if (cableCodo != null) {
                    cableArrastrandoCodo = cableCodo;
                    arrastrandoCodo = true;
                    return true;
                }
            }

            if (nivel != null && mx >= 1645f && mx <= 1895f) {
                if (my >= 720f && my <= 905f) {
                    Nivel.CasillaInventario casilla = nivel.buscarCasilla("LED");
                    if (casilla != null && casilla.puedeUsar()) {
                        arrastrandoDesdeBarra = true;
                        tipoComponenteArrastrado = "LED";
                        posicionArrastre.set(mx, my);
                        sonar(TipoEfecto.CLICK_UI);
                        return true;
                    }
                } else if (my >= 515f && my <= 700f) {
                    Nivel.CasillaInventario casilla = nivel.buscarCasilla("RESISTENCIA_220");
                    if (casilla != null && casilla.puedeUsar()) {
                        arrastrandoDesdeBarra = true;
                        tipoComponenteArrastrado = "RESISTENCIA_220";
                        posicionArrastre.set(mx, my);
                        sonar(TipoEfecto.CLICK_UI);
                        return true;
                    }
                } else if (my >= 310f && my <= 495f) {
                    Nivel.CasillaInventario casilla = nivel.buscarCasilla("SWITCH");
                    if (casilla != null && casilla.puedeUsar()) {
                        arrastrandoDesdeBarra = true;
                        tipoComponenteArrastrado = "SWITCH";
                        posicionArrastre.set(mx, my);
                        sonar(TipoEfecto.CLICK_UI);
                        return true;
                    }
                } else if (my >= 105f && my <= 290f) {
                    Nivel.CasillaInventario casAnd = nivel.buscarCasilla("AND");
                    Nivel.CasillaInventario casOr = nivel.buscarCasilla("OR");
                    Nivel.CasillaInventario casGate = (casAnd != null) ? casAnd : casOr;
                    if (casGate != null && casGate.puedeUsar()) {
                        arrastrandoDesdeBarra = true;
                        tipoComponenteArrastrado = casGate.getTipo();
                        posicionArrastre.set(mx, my);
                        sonar(TipoEfecto.CLICK_UI);
                        return true;
                    }
                }
            }

            if (vista != null) {
                Terminal term = vista.obtenerTerminalBajoRaton(mx, my, 30f);
                if (term != null) {
                    vista.iniciarCable(term, mx, my);
                    return true;
                }

                Componente comp = vista.obtenerComponenteBajoRaton(mx, my);
                if (comp != null && !(comp instanceof FuenteAlimentacion)) {
                    componenteSeleccionado = comp;
                    clickInicioX = mx;
                    clickInicioY = my;
                    desfaseArrastreX = mx - comp.getPosicionX();
                    desfaseArrastreY = my - comp.getPosicionY();
                    sonar(TipoEfecto.CLICK_UI);
                    return true;
                }
            }
        } else if (button == Buttons.RIGHT) {
            if (vista != null && vista.estaMostrandoGuiaNivel()) {
                vista.cerrarGuiaNivel();
                return true;
            }

            if (arrastrandoDesdeBarra) {
                arrastrandoDesdeBarra = false;
                tipoComponenteArrastrado = null;
                return true;
            }

            if (vista != null && vista.estaCableEnProgreso()) {
                vista.cancelarCable();
                return true;
            }

            if (vista != null && nivel != null) {
                Terminal term = vista.obtenerTerminalBajoRaton(mx, my, 30f);
                if (term != null) {
                    List<Cable> conectados = nivel.buscarCablesConTerminal(term);
                    if (!conectados.isEmpty()) {
                        nivel.eliminarCable(conectados.get(conectados.size() - 1));
                        sonar(TipoEfecto.DESCONECTAR_CABLE);
                        return true;
                    }
                }

                Cable cableBajoRaton = vista.obtenerCableBajoRaton(mx, my, 24f);
                if (cableBajoRaton != null) {
                    nivel.eliminarCable(cableBajoRaton);
                    sonar(TipoEfecto.DESCONECTAR_CABLE);
                    return true;
                }

                Componente comp = vista.obtenerComponenteBajoRaton(mx, my);
                if (comp != null && !(comp instanceof FuenteAlimentacion)) {
                    nivel.devolverAlInventario(comp);
                    sonar(TipoEfecto.DESCONECTAR_CABLE);
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
            float maxAlto = 965f - componenteSeleccionado.getAlto();
            if (nuevaX < 310f) nuevaX = 310f;
            if (nuevaX > 1740f) nuevaX = 1740f;
            if (nuevaY < 118f) nuevaY = 118f;
            if (nuevaY > maxAlto) nuevaY = maxAlto;
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
                if (nivel != null && mx >= 300f && mx < 1620f && my >= 100f && my <= 960f) {
                    float w = 240f;
                    float h = 240f;
                    if ("LED".equals(tipoComponenteArrastrado)) {
                        w = 240f; h = 240f;
                    } else if ("RESISTENCIA_220".equals(tipoComponenteArrastrado)) {
                        w = 270f; h = 172f;
                    } else if ("AND".equals(tipoComponenteArrastrado) || "OR".equals(tipoComponenteArrastrado)) {
                        w = 280f; h = 280f;
                    } else if ("SWITCH".equals(tipoComponenteArrastrado)) {
                        boolean esDoble = (nivel.getNumeroNivel() >= 3);
                        w = esDoble ? 230f : 190f;
                        h = esDoble ? 180f : 110f;
                    }
                    float posX = Math.max(315f, Math.min(1600f - w, mx - w / 2f));
                    float posY = Math.max(118f, Math.min(965f - h, my - h / 2f));
                    Componente colocado = nivel.crearYColocarComponente(tipoComponenteArrastrado, posX, posY);
                    if (colocado != null) {
                        sonar(TipoEfecto.CONECTAR_CABLE);
                    }
                }
                arrastrandoDesdeBarra = false;
                tipoComponenteArrastrado = null;
                return true;
            }

            if (vista != null && vista.estaCableEnProgreso()) {
                Terminal destino = vista.obtenerTerminalBajoRaton(mx, my, 30f);
                vista.finalizarCable(destino);
                return true;
            }

            if (componenteSeleccionado != null) {
                float distArrastre = Vector2.dst(clickInicioX, clickInicioY, mx, my);
                if (mx >= 1620f) {
                    nivel.devolverAlInventario(componenteSeleccionado);
                    sonar(TipoEfecto.DESCONECTAR_CABLE);
                } else {
                    float maxX = 1605f - componenteSeleccionado.getAncho();
                    if (componenteSeleccionado.getPosicionX() > maxX) {
                        componenteSeleccionado.actualizarPosicion(maxX, componenteSeleccionado.getPosicionY());
                    }
                    if (componenteSeleccionado instanceof Switch && distArrastre < 15f) {
                        Switch sw = (Switch) componenteSeleccionado;
                        sw.conmutarSegunPosicionY(my);
                        nivel.actualizar(0f);
                        sonar(TipoEfecto.CONMUTAR_SWITCH);
                        if (vista != null) {
                            float contactX = sw.getPosicionX() + sw.getAncho() - 28f;
                            float contactY;
                            if (sw.isDoble()) {
                                boolean mitadSuperior = (my >= sw.getPosicionY() + sw.getAlto() * 0.5f);
                                contactY = sw.getPosicionY() + (mitadSuperior ? 136f : 44f);
                            } else {
                                contactY = sw.getPosicionY() + sw.getAlto() / 2f;
                            }
                            vista.generarChispasImpacto(contactX, contactY, 8);
                        }
                    } else if (nivel != null) {
                        nivel.actualizar(0f);
                    }
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
        if (keycode == Input.Keys.H) {
            if (vista != null) {
                vista.alternarGuiaNivel();
                return true;
            }
        } else if (keycode == Input.Keys.ESCAPE) {
            if (vista != null) {
                if (vista.estaMostrandoGuiaNivel()) {
                    vista.cerrarGuiaNivel();
                } else {
                    vista.volverAlMenu();
                }
                return true;
            }
        } else if (keycode == Input.Keys.R) {
            if (vista != null) {
                vista.reiniciarNivel();
                return true;
            }
        } else if (keycode == Input.Keys.SPACE) {
            if (vista != null) {
                if (vista.estaMostrandoGuiaNivel()) {
                    vista.cerrarGuiaNivel();
                } else {
                    vista.saltarIntro();
                }
                return true;
            }
        } else if (keycode == Input.Keys.ENTER) {
            if (vista != null) {
                if (vista.estaMostrandoGuiaNivel()) {
                    vista.cerrarGuiaNivel();
                    return true;
                }
                if (nivel != null && nivel.estaGanado()) {
                    vista.siguienteNivel();
                    return true;
                } else if (vista.estaEnModalDerrota()) {
                    vista.reiniciarNivel();
                    return true;
                }
            }
        }
        return false;
    }

    private void sonar(TipoEfecto efecto) {
        if (gestorAudio != null) {
            gestorAudio.reproducirEfecto(efecto);
        }
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

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
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
