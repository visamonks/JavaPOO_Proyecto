package com.schematic.controller;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.schematic.model.Cable;
import com.schematic.model.Componente;
import com.schematic.model.Minijuego;
import com.schematic.model.Switch;
import com.schematic.model.Terminal;
import java.util.ArrayList;
import java.util.List;

public class ControladorNivel extends InputAdapter {

    public static final float TAMANO_CELDA = 40f;
    public static final float ANCHO_GRID = 620f;
    public static final float ALTO_GRID = 530f;
    public static final float ANCHO_COMPONENTE = 90f;
    public static final float ALTO_COMPONENTE = 60f;

    private Minijuego minijuego;
    private OrthographicCamera camara;
    private Componente componenteSeleccionado;

    
    private boolean arrastrandoDeCinta;
    private float dragOffsetX;
    private float dragOffsetY;

    
    private boolean trazandoCable;
    private Terminal terminalOrigenCable;
    private List<Vector2> puntosCablePreview;

    public ControladorNivel(Minijuego minijuego) {
        this(minijuego, null);
    }

    public ControladorNivel(Minijuego minijuego, OrthographicCamera camara) {
        this.minijuego = minijuego;
        this.camara = camara;
        this.componenteSeleccionado = null;
        this.arrastrandoDeCinta = false;
        this.trazandoCable = false;
        this.terminalOrigenCable = null;
        this.puntosCablePreview = new ArrayList<>();
    }

    private Vector2 obtenerCoordenadasMundo(int screenX, int screenY) {
        if (camara != null) {
            Vector3 vec3 = camara.unproject(new Vector3(screenX, screenY, 0));
            return new Vector2(vec3.x, vec3.y);
        }
        return new Vector2(screenX, screenY);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;

        
        if (button == Buttons.RIGHT) {
            if (trazandoCable) {
                cancelarTrazoCable();
                return true;
            }
            
            for (Componente comp : new ArrayList<>(minijuego.getComponentes())) {
                if (comp.contienePunto(mx, my)) {
                    minijuego.eliminarComponente(comp);
                    minijuego.getComponentesCinta().add(comp);
                    return true;
                }
            }
            return false;
        }

        if (button != Buttons.LEFT) {
            return false;
        }

        
        for (Componente comp : minijuego.getComponentesCinta()) {
            if (comp.contienePunto(mx, my)) {
                componenteSeleccionado = comp;
                arrastrandoDeCinta = true;
                dragOffsetX = mx - comp.getPosicionX();
                dragOffsetY = my - comp.getPosicionY();
                return true;
            }
        }

        
        for (Componente comp : minijuego.getComponentes()) {
            
            Terminal term = comp.buscarTerminalCercano(mx, my, 20f);
            if (term != null) {
                trazandoCable = true;
                terminalOrigenCable = term;
                puntosCablePreview = Cable.calcularRutaOrtogonal(term.getPosicionAbsoluta(), new Vector2(mx, my));
                return true;
            }

            
            if (comp.contienePunto(mx, my)) {
                if (comp instanceof Switch) {
                    ((Switch) comp).conmutar();
                    return true;
                }
                componenteSeleccionado = comp;
                arrastrandoDeCinta = false;
                dragOffsetX = mx - comp.getPosicionX();
                dragOffsetY = my - comp.getPosicionY();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;

        
        if (componenteSeleccionado != null) {
            componenteSeleccionado.actualizarPosicion(mx - dragOffsetX, my - dragOffsetY);
            for (Cable c : minijuego.getCables()) {
                c.actualizarRuta();
            }
            return true;
        }

        
        if (trazandoCable && terminalOrigenCable != null) {
            puntosCablePreview = Cable.calcularRutaOrtogonal(terminalOrigenCable.getPosicionAbsoluta(), new Vector2(mx, my));
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector2 mundo = obtenerCoordenadasMundo(screenX, screenY);
        float mx = mundo.x;
        float my = mundo.y;

        
        if (componenteSeleccionado != null) {
            
            if (mx >= 0 && mx <= ANCHO_GRID && my >= 0 && my <= ALTO_GRID) {
                float snappedX = Math.round((mx - dragOffsetX) / TAMANO_CELDA) * TAMANO_CELDA;
                float snappedY = Math.round((my - dragOffsetY) / TAMANO_CELDA) * TAMANO_CELDA;

                
                if (snappedX < 10) snappedX = 10;
                if (snappedX > ANCHO_GRID - componenteSeleccionado.getAncho()) {
                    snappedX = ANCHO_GRID - componenteSeleccionado.getAncho();
                }
                if (snappedY < 10) snappedY = 10;
                if (snappedY > ALTO_GRID - componenteSeleccionado.getAlto()) {
                    snappedY = ALTO_GRID - componenteSeleccionado.getAlto();
                }

                componenteSeleccionado.actualizarPosicion(snappedX, snappedY);
                minijuego.agregarComponente(componenteSeleccionado);
            } else {
                
                if (!arrastrandoDeCinta) {
                    minijuego.eliminarComponente(componenteSeleccionado);
                    minijuego.getComponentesCinta().add(componenteSeleccionado);
                }
            }

            for (Cable c : minijuego.getCables()) {
                c.actualizarRuta();
            }
            componenteSeleccionado = null;
            arrastrandoDeCinta = false;
            return true;
        }

        
        if (trazandoCable && terminalOrigenCable != null) {
            Terminal terminalDestino = null;
            for (Componente comp : minijuego.getComponentes()) {
                Terminal encontrado = comp.buscarTerminalCercano(mx, my, 24f);
                if (encontrado != null && encontrado != terminalOrigenCable) {
                    terminalDestino = encontrado;
                    break;
                }
            }

            if (terminalDestino != null) {
                
                Terminal salida = null;
                Terminal entrada = null;

                if (!terminalOrigenCable.isEsEntrada() && terminalDestino.isEsEntrada()) {
                    salida = terminalOrigenCable;
                    entrada = terminalDestino;
                } else if (terminalOrigenCable.isEsEntrada() && !terminalDestino.isEsEntrada()) {
                    salida = terminalDestino;
                    entrada = terminalOrigenCable;
                }

                if (salida != null && entrada != null) {
                    Cable nuevoCable = new Cable(salida, entrada);
                    minijuego.agregarCable(nuevoCable);
                }
            }

            cancelarTrazoCable();
            return true;
        }

        return false;
    }

    private void cancelarTrazoCable() {
        trazandoCable = false;
        terminalOrigenCable = null;
        puntosCablePreview.clear();
    }

    public boolean isTrazandoCable() {
        return trazandoCable;
    }

    public List<Vector2> getPuntosCablePreview() {
        return puntosCablePreview;
    }

    public Componente getComponenteSeleccionado() {
        return componenteSeleccionado;
    }

    public void setComponenteSeleccionado(Componente componenteSeleccionado) {
        this.componenteSeleccionado = componenteSeleccionado;
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

    @Override
    public String toString() {
        return "ControladorNivel{" +
                "minijuego=" + minijuego +
                ", camara=" + camara +
                ", componenteSeleccionado=" + componenteSeleccionado +
                ", trazandoCable=" + trazandoCable +
                '}';
    }
}
