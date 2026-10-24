package com.schematic.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public class EfectoSonoro implements Disposable {

    private final TipoEfecto tipo;
    private Sound sonido;
    private float volumenBase;

    public EfectoSonoro(TipoEfecto tipo) {
        this(tipo, 1.0f);
    }

    public EfectoSonoro(TipoEfecto tipo, float volumenBase) {
        this.tipo = tipo;
        this.volumenBase = volumenBase;
        this.sonido = null;
    }

    public void reproducir(float volumenGeneral) {
        if (sonido != null) {
            sonido.play(volumenBase * volumenGeneral);
        }
    }

    public TipoEfecto getTipo() {
        return tipo;
    }

    public Sound getSonido() {
        return sonido;
    }

    public void setSonido(Sound sonido) {
        this.sonido = sonido;
    }

    public float getVolumenBase() {
        return volumenBase;
    }

    public void setVolumenBase(float volumenBase) {
        this.volumenBase = volumenBase;
    }

    @Override
    public void dispose() {
        if (sonido != null) {
            sonido.dispose();
            sonido = null;
        }
    }
}
