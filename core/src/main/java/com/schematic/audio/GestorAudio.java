package com.schematic.audio;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.schematic.JuegoPrincipal;

import java.util.EnumMap;
import java.util.Map;

public class GestorAudio implements Disposable {

    private final Map<TipoEfecto, EfectoSonoro> efectos;
    private float volumen;
    private boolean silenciado;

    public GestorAudio() {
        this.efectos = new EnumMap<>(TipoEfecto.class);
        this.volumen = 0.8f;
        this.silenciado = false;
        instanciarEfectosBase();
    }

    private void instanciarEfectosBase() {
        for (TipoEfecto tipo : TipoEfecto.values()) {
            efectos.put(tipo, new EfectoSonoro(tipo));
        }
    }

    public void inicializar() {
        // Pendiente para la siguiente parte: carga o generacion de efectos de sonido
    }

    public void reproducirEfecto(TipoEfecto tipo) {
        if (silenciado || tipo == null) {
            return;
        }
        EfectoSonoro efecto = efectos.get(tipo);
        if (efecto != null) {
            efecto.reproducir(volumen);
        }
    }

    public EfectoSonoro obtenerEfecto(TipoEfecto tipo) {
        return efectos.get(tipo);
    }

    public float getVolumen() {
        return volumen;
    }

    public void setVolumen(float volumen) {
        this.volumen = MathUtils.clamp(volumen, 0.0f, 1.0f);
    }

    public boolean isSilenciado() {
        return silenciado;
    }

    public void setSilenciado(boolean silenciado) {
        this.silenciado = silenciado;
    }

    public static GestorAudio obtenerDesde(Game juego) {
        if (juego instanceof JuegoPrincipal) {
            return ((JuegoPrincipal) juego).getGestorAudio();
        }
        return null;
    }

    @Override
    public void dispose() {
        for (EfectoSonoro efecto : efectos.values()) {
            efecto.dispose();
        }
    }
}
