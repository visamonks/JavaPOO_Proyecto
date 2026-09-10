package com.schematic.controller;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.schematic.model.Componente;
import com.schematic.model.Minijuego;
import com.schematic.model.Switch;

public class ControladorNivel extends InputAdapter {

    public static final float TAMANO_CELDA = 60f;
    public static final float ANCHO_COMPONENTE = 90f;
    public static final float ALTO_COMPONENTE = 60f;

    private Minijuego minijuego;
    private OrthographicCamera camara;
    private Componente componenteSeleccionado;

    private float dragOffsetX;
    private float dragOffsetY;

    public ControladorNivel(Minijuego minijuego) {
        this(minijuego, null);
    }

    public ControladorNivel(Minijuego minijuego, OrthographicCamera camara) {
        this.minijuego = minijuego;
        this.camara = camara;
        this.componenteSeleccionado = null;
    }

    private Vector3 obtenerCoordenadasMundo(int screenX, int screenY) {
        Vector3 vec = new Vector3(screenX, screenY, 0);
        if (camara != null) {
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
            if (minijuego != null) {
                for (Componente comp : minijuego.getComponentes()) {
                    float cx = comp.getPosicionX();
                    float cy = comp.getPosicionY();

                    if (mx >= cx && mx <= cx + ANCHO_COMPONENTE &&
                        my >= cy && my <= cy + ALTO_COMPONENTE) {

                        if (comp instanceof Switch) {
                            ((Switch) comp).conmutar();
                        }

                        componenteSeleccionado = comp;
                        dragOffsetX = mx - cx;
                        dragOffsetY = my - cy;
                        return true;
                    }
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

        if (componenteSeleccionado != null) {
            componenteSeleccionado.actualizarPosicion(mx - dragOffsetX, my - dragOffsetY);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (componenteSeleccionado != null) {
            float xActual = componenteSeleccionado.getPosicionX();
            float yActual = componenteSeleccionado.getPosicionY();

            float snappedX = Math.round(xActual / TAMANO_CELDA) * TAMANO_CELDA;
            float snappedY = Math.round(yActual / TAMANO_CELDA) * TAMANO_CELDA;

            if (snappedX < 0) snappedX = 0;
            if (snappedY < 0) snappedY = 0;

            componenteSeleccionado.actualizarPosicion(snappedX, snappedY);
            componenteSeleccionado = null;
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

    public OrthographicCamera getCamara() {
        return camara;
    }

    public void setCamara(OrthographicCamera camara) {
        this.camara = camara;
    }

    public Componente getComponenteSeleccionado() {
        return componenteSeleccionado;
    }

    public void setComponenteSeleccionado(Componente componenteSeleccionado) {
        this.componenteSeleccionado = componenteSeleccionado;
    }

    @Override
    public String toString() {
        return "ControladorNivel{" +
                "minijuego=" + minijuego +
                ", camara=" + camara +
                ", componenteSeleccionado=" + componenteSeleccionado +
                '}';
    }
}

/*
Que hay ahorita:
Detecta cuando haces clic con el raton sobre una pieza, te deja arrastrarla por la pantalla y al soltarla la ajusta solita a la cuadricula. Si le das clic a un switch lo prende o apaga.

Que falta:
Poder sacar piezas desde la barra lateral y hacer que al dar clic en las esquinas de una pieza se dibuje el cable doblando en 90 grados.

Recomendaciones:
Manten aqui solo la interaccion del raton y no metas calculos pesados del juego para que el movimiento se sienta suave.
*/
