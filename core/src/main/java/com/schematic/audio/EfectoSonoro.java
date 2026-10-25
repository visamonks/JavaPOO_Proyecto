package com.schematic.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class EfectoSonoro implements Disposable {

    private final TipoEfecto tipo;
    private Sound sonido;
    private float volumenBase;

    public EfectoSonoro(TipoEfecto tipo) {
        this(tipo, tipo.getVolumenBase());
    }

    public EfectoSonoro(TipoEfecto tipo, float volumenBase) {
        this.tipo = tipo;
        this.volumenBase = volumenBase;
        this.sonido = null;
    }

    public void cargar() {
        if (Gdx.files == null || Gdx.audio == null) {
            return;
        }
        try {
            FileHandle archivo = Gdx.files.internal(tipo.getRuta());
            if (archivo.exists()) {
                this.sonido = Gdx.audio.newSound(archivo);
            }
        } catch (Exception ignored) {
        }
    }

    public void reproducir(float volumenGeneral) {
        if (sonido != null && volumenGeneral > 0f) {
            sonido.play(Math.min(1.0f, volumenBase * volumenGeneral));
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
